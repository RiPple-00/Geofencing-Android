package com.example.geofencing.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geofencing.data.model.Cart
import com.example.geofencing.data.model.CartSummary
import com.example.geofencing.data.model.GeofenceEventInfo
import com.example.geofencing.data.model.MapMarkerInfo
import com.example.geofencing.data.model.Sector
import com.example.geofencing.data.model.SectorDetail
import com.example.geofencing.data.model.SectorOverview
import com.example.geofencing.data.model.SectorSearchResult
import com.example.geofencing.data.repository.CartRepository
import com.example.geofencing.data.repository.GeofenceEventRepository
import com.example.geofencing.data.repository.SectorRepository
import com.example.geofencing.data.repository.SelectedSiteRepository
import com.example.geofencing.data.repository.SiteRepository
import com.example.geofencing.data.repository.ViolationAckRepository
import com.example.geofencing.util.poleOfInaccessibilityOrElse
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// 연속 실패 횟수를 계산하는 순수 함수 - 코루틴/딜레이 루프와 분리해서 단위 테스트한다.
// succeeded면 0으로 리셋, 아니면 이전 값에서 1 증가.
internal fun nextConsecutivePollFailures(previous: Int, succeeded: Boolean): Int =
    if (succeeded) 0 else previous + 1

@HiltViewModel
class MapViewModel @Inject constructor(
    private val siteRepository: SiteRepository,
    private val sectorRepository: SectorRepository,
    private val geofenceEventRepository: GeofenceEventRepository,
    private val violationAckRepository: ViolationAckRepository,
    private val cartRepository: CartRepository,
    private val selectedSiteRepository: SelectedSiteRepository
) : ViewModel() {

    private val _markers = MutableStateFlow<List<MapMarkerInfo>>(emptyList())
    val markers: StateFlow<List<MapMarkerInfo>> = _markers.asStateFlow()

    private val _siteCartSummary = MutableStateFlow<CartSummary?>(null)
    val siteCartSummary: StateFlow<CartSummary?> = _siteCartSummary.asStateFlow()

    // Sector 탭 캐러셀/리스트 카드가 그대로 쓰는 섹터별 상세(이름/주소/카트 현황).
    private val _sectorDetails = MutableStateFlow<List<SectorDetail>>(emptyList())
    val sectorDetails: StateFlow<List<SectorDetail>> = _sectorDetails.asStateFlow()

    // 지도에 지오펜스 폴리곤을 그리기 위한 원본 경계 좌표(섹터별).
    private val _sectorOverviews = MutableStateFlow<List<SectorOverview>>(emptyList())
    val sectorOverviews: StateFlow<List<SectorOverview>> = _sectorOverviews.asStateFlow()

    private val _geofenceEvents = MutableStateFlow<List<GeofenceEventInfo>>(emptyList())
    val geofenceEvents: StateFlow<List<GeofenceEventInfo>> = _geofenceEvents.asStateFlow()

    // Cart 탭 리스트(필터/페이지네이션은 UI에서 처리) - 전체 사이트 카트 목록.
    private val _cartItems = MutableStateFlow<List<Cart>>(emptyList())
    val cartItems: StateFlow<List<Cart>> = _cartItems.asStateFlow()

    // 마지막으로 확인한 시각 이후에 발생한 이탈 이벤트가 하나라도 있으면 true -
    // Violation 탭 버튼(필터/상단 Cart 탭)에 ic_warning 배지를 띄우는 데 쓰인다.
    val hasUnseenViolation: StateFlow<Boolean> = combine(
        _geofenceEvents,
        violationAckRepository.lastAcknowledgedAt
    ) { events, ackAt ->
        events.any { it.occurredAt.isAfter(ackAt) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), false)

    private val _lastRefreshedAt = MutableStateFlow<Instant?>(null)
    val lastRefreshedAt: StateFlow<Instant?> = _lastRefreshedAt.asStateFlow()

    // 요약/섹터/카트 갱신(refreshSummary) 진행 상태 - 새로고침 버튼 중복 클릭 방지 등에 쓰인다.
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // refreshSummary 실패 시 노출할 메시지. 다음 시도 시작 또는 성공 시 null로 클리어된다.
    // 최초 로드 실패와 수동 새로고침 실패를 구분하는 별도 상태는 두지 않고 이 값 하나로 처리한다.
    private val _refreshError = MutableStateFlow<String?>(null)
    val refreshError: StateFlow<String?> = _refreshError.asStateFlow()

    // 현재 검색어에 대한 검색 실패 여부. 새 검색어 입력이나 성공 시 false로 리셋된다.
    // 디바운스가 다음 입력에서 자연스럽게 재시도하므로 별도 retry 함수는 두지 않는다.
    private val _searchError = MutableStateFlow(false)
    val searchError: StateFlow<Boolean> = _searchError.asStateFlow()

    // 지오펜스 이벤트 폴링이 연속으로 실패 중인지 - 아직 UI에는 연결하지 않고 내부 기록용으로만 둔다.
    private var consecutivePollFailures = 0
    private val _hasPollFailure = MutableStateFlow(false)
    val hasPollFailure: StateFlow<Boolean> = _hasPollFailure.asStateFlow()

    // 맨 처음 데이터가 들어왔을 때 모든 섹터가 한 화면에 보이도록 카메라를 맞추기 위한
    // 경계값. 최초 1회만 채우고 이후 새로고침(refresh())에서는 다시 갱신하지 않는다 - 매번
    // 갱신하면 사용자가 지도를 보고 있는 도중에 시점이 계속 리셋되어 버린다.
    private val _initialCameraBounds = MutableStateFlow<LatLngBounds?>(null)
    val initialCameraBounds: StateFlow<LatLngBounds?> = _initialCameraBounds.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    private val _searchResults = MutableStateFlow<List<SectorSearchResult>>(emptyList())
    val searchResults: StateFlow<List<SectorSearchResult>> = _searchResults.asStateFlow()

    init {
        // 사이트 요약/섹터 상세는 폴링하지 않는다 - 최초 1회만 불러오고, 이후에는
        // RefreshStatusRow의 새로고침 버튼(refresh())을 눌렀을 때만 갱신한다.
        viewModelScope.launch { refreshSummary() }
        // 지오펜스 이탈 이벤트 - 명세대로 10초 주기 폴링. siteId는 반복마다 한 번씩 새로 읽는다.
        viewModelScope.launch {
            while (true) {
                val siteId = selectedSiteRepository.selectedSiteId.first()
                val result = runCatching { geofenceEventRepository.getRecentEvents(siteId) }
                result.onSuccess { _geofenceEvents.value = it }
                consecutivePollFailures = nextConsecutivePollFailures(consecutivePollFailures, result.isSuccess)
                _hasPollFailure.value = consecutivePollFailures > 0
                delay(POLL_INTERVAL_MS)
            }
        }
        // 디바운스: 타이핑이 300ms 이상 멈췄을 때만 검색 API를 호출
        // collectLatest라 디바운스 대기 중 새 입력이 오면 이전 대기/호출은 자동으로 취소된다.
        viewModelScope.launch {
            // StateFlow는 이미 동일 값 연속 방출을 걸러주므로 distinctUntilChanged가 불필요.
            searchQuery
                .debounce(SEARCH_DEBOUNCE_MS)
                .collectLatest { query -> performSearch(query) }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    // RefreshStatusRow의 새로고침 버튼에서 호출 - 폴링을 기다리지 않고 즉시 갱신.
    // 최초 로드 실패 후 재시도할 때도 이 함수를 그대로 재사용한다.
    fun refresh() {
        viewModelScope.launch { refreshSummary() }
    }

    // 사용자가 Violation 필터를 실제로 열어봤을 때 호출 - 이 시점 이후의 이탈 이벤트만
    // 다시 "미확인"으로 취급한다.
    fun acknowledgeViolations() {
        viewModelScope.launch { violationAckRepository.acknowledgeNow() }
    }

    private suspend fun performSearch(query: String) {
        _searchError.value = false
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        val siteId = selectedSiteRepository.selectedSiteId.first()
        runCatching { sectorRepository.searchSectors(siteId, query) }
            .onSuccess {
                _searchResults.value = it
                _searchError.value = false
            }
            .onFailure { _searchError.value = true }
    }

    private suspend fun refreshSummary() {
        _isRefreshing.value = true
        _refreshError.value = null
        // 이 실행 안의 모든 요청(요약/섹터 상세/카트)이 동일한 siteId를 쓰도록 시작 시점에 한 번만 읽는다 -
        // 실행 도중 선택된 사이트가 바뀌어도 한 사이트의 데이터끼리만 섞이게 한다.
        val siteId = selectedSiteRepository.selectedSiteId.first()
        runCatching {
            val summary = siteRepository.getSiteSummary(siteId)
            val details = coroutineScope {
                summary.sectors
                    .map { overview ->
                        async { runCatching { sectorRepository.getSectorDetail(siteId, overview.id) }.getOrNull() }
                    }
                    .map { it.await() }
            }.filterNotNull()
            val carts = fetchAllCarts(siteId)
            Triple(summary, details, carts)
        }.onSuccess { (summary, details, carts) ->
            _siteCartSummary.value = summary.cartSummary
            _sectorDetails.value = details
            _sectorOverviews.value = summary.sectors
            _cartItems.value = carts
            _markers.value = summary.sectors.map { overview ->
                val detail = details.find { it.id == overview.id }
                Sector(
                    id = overview.id.toString(),
                    name = overview.name,
                    address = overview.address,
                    cartCount = detail?.cartSummary?.total ?: 0,
                    violatingCount = detail?.cartSummary?.violating ?: 0,
                    position = overview.geofence.poleOfInaccessibilityOrElse(FALLBACK_POSITION)
                )
            }
            if (_initialCameraBounds.value == null) {
                _initialCameraBounds.value = buildBoundsOrNull(summary.sectors.flatMap { it.geofence })
            }
            _lastRefreshedAt.value = Instant.now()
        }.onFailure {
            _refreshError.value = REFRESH_ERROR_MESSAGE
        }
        _isRefreshing.value = false
    }

    // Cart 탭은 서버 페이지네이션 대신 클라이언트에서 필터링 후 7개씩 다시 나눠 보여주므로,
    // 여기서는 전체 페이지를 순회해 사이트의 모든 카트를 한 번에 모은다.
    private suspend fun fetchAllCarts(siteId: Int): List<Cart> {
        val firstPage = cartRepository.getCarts(siteId, page = 1, limit = CART_FETCH_LIMIT)
        if (firstPage.totalPages <= 1) return firstPage.carts
        val restPages = coroutineScope {
            (2..firstPage.totalPages)
                .map { page -> async { cartRepository.getCarts(siteId, page = page, limit = CART_FETCH_LIMIT) } }
                .map { it.await() }
        }
        return firstPage.carts + restPages.flatMap { it.carts }
    }

    // 모든 섹터의 지오펜스 폴리곤 꼭짓점을 다 포함하는 경계 - 핀 위치가 아니라 경계
    // 전체를 기준으로 잡아야 섹터 영역이 화면에서 잘리지 않는다.
    private fun buildBoundsOrNull(points: List<LatLng>): LatLngBounds? {
        if (points.isEmpty()) return null
        val builder = LatLngBounds.Builder()
        points.forEach { builder.include(it) }
        return runCatching { builder.build() }.getOrNull()
    }

    private companion object {
        const val POLL_INTERVAL_MS = 10_000L
        const val SEARCH_DEBOUNCE_MS = 300L
        const val CART_FETCH_LIMIT = 50
        const val REFRESH_ERROR_MESSAGE = "데이터를 불러오지 못했습니다"
        val FALLBACK_POSITION = LatLng(0.0, 0.0)
    }
}
