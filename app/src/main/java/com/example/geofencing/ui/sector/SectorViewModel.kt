package com.example.geofencing.ui.sector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geofencing.ui.common.LoadState
import com.example.geofencing.ui.mock.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// 선택된 섹터(select(name))를 공유 캐시에서 관찰해 SectorPage UI state로 노출.
// 캐시가 이미 로드돼 있으면 섹터 전환은 즉시(재요청 없음). 없는 섹터는 Error.
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SectorViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {
    private val selectedName = MutableStateFlow<String?>(null)

    fun select(name: String) {
        selectedName.value = name
    }

    fun retry() = repository.refresh()

    val state: StateFlow<LoadState<SectorUiState>> = selectedName
        .flatMapLatest { name ->
            if (name == null) flowOf<LoadState<SectorUiState>>(LoadState.Loading)
            else repository.observeSector(name).map { state ->
                when (state) {
                    LoadState.Loading -> LoadState.Loading
                    is LoadState.Error -> state
                    is LoadState.Success ->
                        state.data?.let { LoadState.Success(sectorStateFrom(it)) }
                            ?: LoadState.Error("Sector not found: $name")
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LoadState.Loading)
}
