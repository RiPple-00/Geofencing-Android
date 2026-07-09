package com.example.geofencing.data.repository

import com.example.geofencing.data.model.SiteSummary

interface SiteRepository {
    // GET /sites/{siteId}/summary
    suspend fun getSiteSummary(siteId: Int): SiteSummary
}
