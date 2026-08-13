package com.example.geofencing.ui.sector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

// 선택된 섹터(select(name))의 데이터를 Repository에서 관찰해 SectorPage UI state로 노출.
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SectorViewModel @Inject constructor(
    repository: DashboardRepository
) : ViewModel() {
    private val selectedName = MutableStateFlow<String?>(null)

    fun select(name: String) {
        selectedName.value = name
    }

    val state: StateFlow<SectorUiState?> = selectedName
        .flatMapLatest { name ->
            if (name == null) flowOf(null)
            else repository.observeSector(name).map { it?.let(::sectorStateFrom) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
