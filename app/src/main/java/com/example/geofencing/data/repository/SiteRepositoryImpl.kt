package com.example.geofencing.data.repository

import com.example.geofencing.data.model.SiteSummary
import com.example.geofencing.data.remote.GeofencingApi
import com.example.geofencing.data.remote.toDomain
import com.example.geofencing.data.remote.unwrap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SiteRepositoryImpl @Inject constructor(
    private val api: GeofencingApi
) : SiteRepository {

    override suspend fun getSiteSummary(siteId: Int): SiteSummary =
        api.getSiteSummary(siteId).unwrap().toDomain()
}
