package com.example.geofencing.ui.map

import androidx.lifecycle.ViewModel
import com.example.geofencing.data.model.MapMarkerInfo
import com.example.geofencing.data.repository.SectorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    repository: SectorRepository
) : ViewModel() {

    private val _markers = MutableStateFlow<List<MapMarkerInfo>>(repository.getSectors())
    val markers: StateFlow<List<MapMarkerInfo>> = _markers
}
