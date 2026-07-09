package com.example.geofencing.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geofencing.data.model.CartSummary
import com.example.geofencing.data.model.GeofenceEventInfo
import com.example.geofencing.data.model.MapMarkerInfo
import com.example.geofencing.data.model.Sector
import com.example.geofencing.data.model.SectorDetail
import com.example.geofencing.data.model.SectorOverview
import com.example.geofencing.data.model.SectorSearchResult
import com.example.geofencing.data.repository.GeofenceEventRepository
import com.example.geofencing.data.repository.SectorRepository
import com.example.geofencing.data.repository.SiteRepository
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class MapViewModel @Inject constructor(
    private val siteRepository: SiteRepository,
    private val sectorRepository: SectorRepository,
    private val geofenceEventRepository: GeofenceEventRepository
) : ViewModel() {

    // TODO: 로그인/사이트 선택 플로우가 생기면 고정값 대신 실제 선택된 siteId로 교체.
    private val siteId = 1

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

    private val _lastRefreshedAt = MutableStateFlow<Instant?>(null)
    val lastRefreshedAt: StateFlow<Instant?> = _lastRefreshedAt.asStateFlow()

    // 맨 처음 데이터가 들어왔을 때 모든 섹터가 한 화면에 보이도록 카메라를 맞추기 위한
    // 경계값. 최초 1회만 채우고 이후 폴링(10초)에서는 다시 갱신하지 않는다 - 매번
    // 갱신하면 사용자가 지도를 보고 있는 도중에 시점이 계속 리셋되어 버린다.
    private val _initialCameraBounds = MutableStateFlow<LatLngBounds?>(null)
    val initialCameraBounds: StateFlow<LatLngBounds?> = _initialCameraBounds.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    private val _searchResults = MutableStateFlow<List<SectorSearchResult>>(emptyList())
    val searchResults: StateFlow<List<SectorSearchResult>> = _searchResults.asStateFlow()

    init {
        // 카트는 10초 간격으로 위치를 보고하므로, 사이트 요약/섹터 상세도 같은 주기로 폴링한다.
        viewModelScope.launch {
            while (true) {
                refreshSummary()
                delay(POLL_INTERVAL_MS)
            }
        }
        // 지오펜스 이탈 이벤트 - 명세대로 10초 주기 폴링.
        viewModelScope.launch {
            while (true) {
                runCatching { geofenceEventRepository.getRecentEvents(siteId) }
                    .onSuccess { _geofenceEvents.value = it }
                delay(POLL_INTERVAL_MS)
            }
        }
        // 글자 하나 입력할 때마다 바로 검색 API를 호출한다(디바운스 없음) - itemList가
        // 매 입력마다 즉시 리렌더링되어야 하므로. collectLatest라 이전 호출은 새 입력이
        // 오면 자동으로 취소된다.
        viewModelScope.launch {
            // StateFlow는 이미 동일 값 연속 방출을 걸러주므로 distinctUntilChanged가 불필요.
            searchQuery
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _searchResults.value = emptyList()
                        return@collectLatest
                    }
                    runCatching { sectorRepository.searchSectors(siteId, query) }
                        .onSuccess { _searchResults.value = it }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    // RefreshStatusRow의 새로고침 버튼에서 호출 - 폴링을 기다리지 않고 즉시 갱신.
    fun refresh() {
        viewModelScope.launch { refreshSummary() }
    }

    private suspend fun refreshSummary() {
        runCatching {
            val summary = siteRepository.getSiteSummary(siteId)
            val details = coroutineScope {
                summary.sectors
                    .map { overview ->
                        async { runCatching { sectorRepository.getSectorDetail(siteId, overview.id) }.getOrNull() }
                    }
                    .map { it.await() }
            }.filterNotNull()
            summary to details
        }.onSuccess { (summary, details) ->
            _siteCartSummary.value = summary.cartSummary
            _sectorDetails.value = details
            _sectorOverviews.value = summary.sectors
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
        }
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
        val FALLBACK_POSITION = LatLng(0.0, 0.0)
    }
}
