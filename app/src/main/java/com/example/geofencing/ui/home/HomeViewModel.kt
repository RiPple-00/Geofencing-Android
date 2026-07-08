package com.example.geofencing.ui.home

import androidx.lifecycle.ViewModel
import com.example.geofencing.data.model.Geofence
import com.example.geofencing.data.repository.GeofenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: GeofenceRepository
) : ViewModel() {

    private val _geofences = MutableStateFlow<List<Geofence>>(repository.getGeofences())
    val geofences: StateFlow<List<Geofence>> = _geofences
}
