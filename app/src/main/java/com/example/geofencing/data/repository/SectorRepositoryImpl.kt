package com.example.geofencing.data.repository

import com.example.geofencing.data.model.SectorDetail
import com.example.geofencing.data.model.SectorSearchResult
import com.example.geofencing.data.remote.GeofencingApi
import com.example.geofencing.data.remote.toDomain
import com.example.geofencing.data.remote.unwrap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SectorRepositoryImpl @Inject constructor(
    private val api: GeofencingApi
) : SectorRepository {

    override suspend fun getSectorDetail(siteId: Int, sectorId: Int): SectorDetail =
        api.getSectorDetail(siteId, sectorId).unwrap().toDomain()

    override suspend fun searchSectors(siteId: Int, query: String, limit: Int?): List<SectorSearchResult> =
        api.searchSectors(siteId, query, limit).unwrap().results.map { it.toDomain() }
}
