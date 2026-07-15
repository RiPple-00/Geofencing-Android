package com.example.geofencing.ui.map

import androidx.lifecycle.viewModelScope
import com.example.geofencing.data.model.Cart
import com.example.geofencing.data.model.CartPage
import com.example.geofencing.data.model.CartSummary
import com.example.geofencing.data.model.GeofenceEventInfo
import com.example.geofencing.data.model.SectorDetail
import com.example.geofencing.data.model.SectorOverview
import com.example.geofencing.data.model.SectorSearchResult
import com.example.geofencing.data.model.SiteSummary
import com.example.geofencing.data.repository.CartRepository
import com.example.geofencing.data.repository.GeofenceEventRepository
import com.example.geofencing.data.repository.SectorRepository
import com.example.geofencing.data.repository.SelectedSiteRepository
import com.example.geofencing.data.repository.SiteRepository
import com.example.geofencing.data.repository.ViolationAckRepository
import com.example.geofencing.testutil.MainDispatcherRule
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

// 검색 debounce(MapViewModel.SEARCH_DEBOUNCE_MS)와 값이 같아야 한다. private companion이라
// 직접 참조할 수 없어 테스트에서 동일한 값을 별도로 둔다.
private const val SEARCH_DEBOUNCE_MS = 300L

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val siteRepository = FakeSiteRepository()
    private val sectorRepository = FakeSectorRepository()
    private val geofenceEventRepository = FakeGeofenceEventRepository()
    private val violationAckRepository = FakeViolationAckRepository()
    private val cartRepository = FakeCartRepository()
    private val selectedSiteRepository = FakeSelectedSiteRepository()

    private fun createViewModel() = MapViewModel(
        siteRepository = siteRepository,
        sectorRepository = sectorRepository,
        geofenceEventRepository = geofenceEventRepository,
        violationAckRepository = violationAckRepository,
        cartRepository = cartRepository,
        selectedSiteRepository = selectedSiteRepository
    )

    @Test
    fun `refreshSummary 성공 시 isRefreshing이 false로 돌아오고 데이터가 채워진다`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            runCurrent()

            assertFalse(viewModel.isRefreshing.value)
            assertNull(viewModel.refreshError.value)
            assertEquals(1, viewModel.sectorDetails.value.size)
            assertEquals(SAMPLE_CART_SUMMARY.total, viewModel.siteCartSummary.value?.total)

            viewModel.viewModelScope.cancel()
        }

    @Test
    fun `refreshSummary 실패 시 refreshError가 채워지고 기존 데이터는 유지된다`() =
        runTest(mainDispatcherRule.testDispatcher) {
            siteRepository.result = Result.failure(IllegalStateException("network down"))
            val viewModel = createViewModel()
            runCurrent()

            assertFalse(viewModel.isRefreshing.value)
            assertEquals("데이터를 불러오지 못했습니다", viewModel.refreshError.value)
            assertTrue(viewModel.sectorDetails.value.isEmpty())

            viewModel.viewModelScope.cancel()
        }

    @Test
    fun `실패 후 refresh 재시도가 성공하면 refreshError가 클리어된다`() =
        runTest(mainDispatcherRule.testDispatcher) {
            siteRepository.result = Result.failure(IllegalStateException("network down"))
            val viewModel = createViewModel()
            runCurrent()
            assertEquals("데이터를 불러오지 못했습니다", viewModel.refreshError.value)

            siteRepository.result = Result.success(SAMPLE_SITE_SUMMARY)
            viewModel.refresh()
            runCurrent()

            assertNull(viewModel.refreshError.value)
            assertEquals(1, viewModel.sectorDetails.value.size)

            viewModel.viewModelScope.cancel()
        }

    @Test
    fun `검색 실패 시 searchError가 true이고 새 검색어 입력 시 리셋된다`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            runCurrent()
            sectorRepository.searchResult = Result.failure(IllegalStateException("search down"))

            viewModel.onSearchQueryChanged("A동")
            advanceTimeBy(SEARCH_DEBOUNCE_MS + 1)
            runCurrent()

            assertTrue(viewModel.searchError.value)
            assertTrue(viewModel.searchResults.value.isEmpty())

            sectorRepository.searchResult = Result.success(listOf(SectorSearchResult(id = 1, name = "A동2")))
            viewModel.onSearchQueryChanged("A동2")
            advanceTimeBy(SEARCH_DEBOUNCE_MS + 1)
            runCurrent()

            assertFalse(viewModel.searchError.value)
            assertEquals(1, viewModel.searchResults.value.size)

            viewModel.viewModelScope.cancel()
        }

    @Test
    fun `refreshSummary 실행 중 선택된 사이트가 바뀌어도 같은 실행 내부 요청은 캡처된 siteId를 그대로 쓴다`() =
        runTest(mainDispatcherRule.testDispatcher) {
            selectedSiteRepository.setSiteId(1)
            // getSiteSummary 응답이 오는 시점에 선택된 사이트가 바뀌는 상황을 흉내낸다.
            siteRepository.onCalled = { selectedSiteRepository.setSiteId(2) }
            val viewModel = createViewModel()
            runCurrent()

            assertEquals(listOf(1), siteRepository.recordedSiteIds)
            assertTrue(sectorRepository.recordedSiteIds.all { it == 1 })
            assertTrue(cartRepository.recordedSiteIds.all { it == 1 })

            viewModel.viewModelScope.cancel()
        }
}

private val SAMPLE_SECTOR_OVERVIEW = SectorOverview(
    id = 1,
    name = "A동",
    address = "서울시 강남구",
    geofence = emptyList()
)
private val SAMPLE_CART_SUMMARY = CartSummary(total = 5, violating = 1, compliant = 4)
private val SAMPLE_SITE_SUMMARY = SiteSummary(
    siteId = 1,
    siteName = "테스트 사이트",
    sectorCount = 1,
    sectors = listOf(SAMPLE_SECTOR_OVERVIEW),
    cartSummary = SAMPLE_CART_SUMMARY
)
private val SAMPLE_SECTOR_DETAIL = SectorDetail(
    id = 1,
    name = "A동",
    address = "서울시 강남구",
    cartSummary = SAMPLE_CART_SUMMARY
)
private val SAMPLE_CART_PAGE = CartPage(
    carts = listOf(Cart(id = 1, name = "cart-1", violating = false)),
    page = 1,
    limit = 50,
    total = 1,
    totalPages = 1
)

private class FakeSiteRepository(
    var result: Result<SiteSummary> = Result.success(SAMPLE_SITE_SUMMARY)
) : SiteRepository {
    val recordedSiteIds = mutableListOf<Int>()
    var onCalled: (() -> Unit)? = null

    override suspend fun getSiteSummary(siteId: Int): SiteSummary {
        recordedSiteIds += siteId
        onCalled?.invoke()
        return result.getOrThrow()
    }
}

private class FakeSectorRepository(
    var detailResult: Result<SectorDetail> = Result.success(SAMPLE_SECTOR_DETAIL),
    var searchResult: Result<List<SectorSearchResult>> = Result.success(emptyList())
) : SectorRepository {
    val recordedSiteIds = mutableListOf<Int>()

    override suspend fun getSectorDetail(siteId: Int, sectorId: Int): SectorDetail {
        recordedSiteIds += siteId
        return detailResult.getOrThrow()
    }

    override suspend fun searchSectors(siteId: Int, query: String, limit: Int?): List<SectorSearchResult> {
        recordedSiteIds += siteId
        return searchResult.getOrThrow()
    }
}

private class FakeGeofenceEventRepository(
    var result: Result<List<GeofenceEventInfo>> = Result.success(emptyList())
) : GeofenceEventRepository {
    override suspend fun getRecentEvents(siteId: Int): List<GeofenceEventInfo> = result.getOrThrow()
}

private class FakeCartRepository(
    var result: Result<CartPage> = Result.success(SAMPLE_CART_PAGE)
) : CartRepository {
    val recordedSiteIds = mutableListOf<Int>()

    override suspend fun getCarts(siteId: Int, page: Int?, limit: Int?): CartPage {
        recordedSiteIds += siteId
        return result.getOrThrow()
    }
}

private class FakeViolationAckRepository : ViolationAckRepository {
    override val lastAcknowledgedAt: Flow<Instant> = MutableStateFlow(Instant.EPOCH)

    override suspend fun acknowledgeNow() = Unit
}

private class FakeSelectedSiteRepository(initialSiteId: Int = 1) : SelectedSiteRepository {
    private val siteIdFlow = MutableStateFlow(initialSiteId)
    override val selectedSiteId: Flow<Int> = siteIdFlow

    override suspend fun selectSite(siteId: Int) {
        siteIdFlow.value = siteId
    }

    fun setSiteId(siteId: Int) {
        siteIdFlow.value = siteId
    }
}
