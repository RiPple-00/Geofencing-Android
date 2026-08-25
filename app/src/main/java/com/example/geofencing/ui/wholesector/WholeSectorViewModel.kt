package com.example.geofencing.ui.wholesector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geofencing.ui.common.LoadState
import com.example.geofencing.ui.common.map
import com.example.geofencing.ui.mock.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// Repository(공유 캐시) → WholeSector UI state. 로딩/에러/성공은 Repository의 LoadState를 그대로 옮긴다.
// retry()는 소스를 재조회(refresh)한다.
@HiltViewModel
class WholeSectorViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    fun retry() = repository.refresh()

    val state: StateFlow<LoadState<WholeSectorUiState>> =
        repository.observeSectors()
            .map { it.map(::wholeSectorStateFrom) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LoadState.Loading)
}
