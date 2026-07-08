package com.example.geofencing.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.geofencing.data.model.Geofence
import com.example.geofencing.data.repository.GeofenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: GeofenceRepository
) : ViewModel() {

    val geofence: Geofence? = savedStateHandle.get<String>("geofenceId")
        ?.let { repository.getGeofenceById(it) }
}
