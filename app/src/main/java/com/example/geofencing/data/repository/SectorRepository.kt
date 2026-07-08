package com.example.geofencing.data.repository

import com.example.geofencing.data.model.Sector

interface SectorRepository {
    fun getSectors(): List<Sector>
}
