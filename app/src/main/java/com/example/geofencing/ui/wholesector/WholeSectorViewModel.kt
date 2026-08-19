package com.example.geofencing.ui.wholesector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geofencing.data.model.SectorDetail
import com.example.geofencing.data.model.SiteSummary
import com.example.geofencing.data.repository.SectorRepository
import com.example.geofencing.data.repository.SelectedSiteRepository
import com.example.geofencing.data.repository.SiteRepository
import com.example.geofencing.ui.common.LoadState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// 선택된 사이트의 요약을 실 API로 관찰해 WholeSector UI state(LoadState)로 노출.
// GET /sites/{id}/summary(사이트 총계 + 섹터 지오펜스) + 섹터별 상세(카트 수)를 조합한다.
// retry()는 재조회한다(로드 실패 시 재시도 경로).
//
// 주의(API 갭): 이 스펙은 UI 일부를 채우지 못한다.
//   - disconnect 상태 개념 없음(violating/compliant + driving/idle 뿐) → 0으로 둠
//   - 위반 카트 "목록"과 각 카트 위치 미제공 → 빈 목록. TODO(후속): geofence-events로 채움
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WholeSectorViewModel @Inject constructor(
    private val siteRepository: SiteRepository,
    private val sectorRepository: SectorRepository,
    selectedSiteRepository: SelectedSiteRepository
) : ViewModel() {
    private val retry = MutableStateFlow(0)

    fun retry() {
        retry.value++
    }

    val state: StateFlow<LoadState<WholeSectorUiState>> =
        combine(selectedSiteRepository.selectedSiteId, retry) { siteId, _ -> siteId }
            .flatMapLatest { siteId ->
                flow {
                    emit(LoadState.Loading)
                    val summary = siteRepository.getSiteSummary(siteId)
                    // 섹터별 카트 수는 상세 엔드포인트에만 있어 병렬로 조회.
                    val details = coroutineScope {
                        summary.sectors
                            .map { sector -> async { sectorRepository.getSectorDetail(siteId, sector.id) } }
                            .awaitAll()
                    }.associateBy { it.id }
                    emit(LoadState.Success(wholeSectorUiStateFrom(summary, details)))
                }.catch { emit(LoadState.Error(it.message ?: "Failed to load site summary")) }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LoadState.Loading)
}

// SiteSummary(+ 섹터별 상세) → WholeSector UI state.
// API 미제공분(disconnect, 위반 카트 목록)은 0/빈 목록으로 둔다.
private fun wholeSectorUiStateFrom(
    summary: SiteSummary,
    details: Map<Int, SectorDetail>
): WholeSectorUiState {
    val cartSummary = summary.cartSummary
    return WholeSectorUiState(
        totalCarts = cartSummary.total,
        compliance = cartSummary.compliant,
        violation = cartSummary.violating,
        disconnect = 0,
        violations = emptyList(),
        disconnects = emptyList(),
        sectors = summary.sectors.map { sector ->
            val counts = details[sector.id]?.cartSummary
            SectorSummary(
                id = sector.id,
                name = sector.name,
                wholeCart = counts?.total ?: 0,
                violation = counts?.violating ?: 0,
                disconnect = 0,
                geofence = sector.geofence
            )
        }
    )
}
