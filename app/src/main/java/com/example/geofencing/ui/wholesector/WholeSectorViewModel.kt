package com.example.geofencing.ui.wholesector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geofencing.ui.mock.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// Repository(도메인) → WholeSector UI state. UI는 이 state만 구독하면 되고, API 교체 시 Repository만 바뀐다.
@HiltViewModel
class WholeSectorViewModel @Inject constructor(
    repository: DashboardRepository
) : ViewModel() {
    val state: StateFlow<WholeSectorUiState> =
        repository.observeSectors()
            .map { wholeSectorStateFrom(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = wholeSectorStateFrom(emptyList())
            )
}
