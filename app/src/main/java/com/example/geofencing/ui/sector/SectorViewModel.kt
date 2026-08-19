package com.example.geofencing.ui.sector

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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// 선택된 섹터(select(name))를 Repository에서 관찰해 SectorPage UI state(LoadState)로 노출.
// select 전(name==null)이나 retry() 시 재구독하며, 없는 섹터는 Error로 내려보낸다.
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SectorViewModel @Inject constructor(
    repository: DashboardRepository
) : ViewModel() {
    private val selectedName = MutableStateFlow<String?>(null)
    private val retry = MutableStateFlow(0)

    fun select(name: String) {
        selectedName.value = name
    }

    fun retry() {
        retry.value++
    }

    val state: StateFlow<LoadState<SectorUiState>> =
        combine(selectedName, retry) { name, _ -> name }
            .flatMapLatest { name ->
                if (name == null) flowOf<LoadState<SectorUiState>>(LoadState.Loading)
                else repository.observeSector(name)
                    .map<MockSector?, LoadState<SectorUiState>> { sector ->
                        if (sector == null) LoadState.Error("Sector not found: $name")
                        else LoadState.Success(sectorStateFrom(sector))
                    }
                    .catch { emit(LoadState.Error(it.message ?: "Failed to load sector")) }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LoadState.Loading)
}
