package com.example.geofencing.ui.wholesector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geofencing.ui.common.LoadState
import com.example.geofencing.ui.mock.DashboardRepository
import com.example.geofencing.ui.mock.MockSector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// Repository(도메인) → WholeSector UI state(LoadState 래핑). UI는 이 state만 구독하면 된다.
// retry()는 소스를 재구독한다(현재 mock은 실패가 없지만, API 교체 시 오류/재시도 경로가 살아있게).
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WholeSectorViewModel @Inject constructor(
    repository: DashboardRepository
) : ViewModel() {
    private val retry = MutableStateFlow(0)

    fun retry() {
        retry.value++
    }

    val state: StateFlow<LoadState<WholeSectorUiState>> = retry
        .flatMapLatest {
            repository.observeSectors()
                .map<List<MockSector>, LoadState<WholeSectorUiState>> {
                    LoadState.Success(wholeSectorStateFrom(it))
                }
                .catch { emit(LoadState.Error(it.message ?: "Failed to load sectors")) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LoadState.Loading)
}
