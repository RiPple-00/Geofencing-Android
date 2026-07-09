package com.example.geofencing.data.repository

import com.example.geofencing.data.model.SectorDetail
import com.example.geofencing.data.model.SectorSearchResult

interface SectorRepository {
    // GET /sites/{siteId}/sectors/{sectorId}
    suspend fun getSectorDetail(siteId: Int, sectorId: Int): SectorDetail

    // GET /sites/{siteId}/sectors/search
    suspend fun searchSectors(siteId: Int, query: String, limit: Int? = null): List<SectorSearchResult>
}
