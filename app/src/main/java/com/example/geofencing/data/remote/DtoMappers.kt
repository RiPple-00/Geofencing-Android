package com.example.geofencing.data.remote

import com.example.geofencing.data.model.CartSummary
import com.example.geofencing.data.model.GeofenceEventInfo
import com.example.geofencing.data.model.SectorDetail
import com.example.geofencing.data.model.SectorOverview
import com.example.geofencing.data.model.SectorSearchResult
import com.example.geofencing.data.model.SiteSummary
import com.example.geofencing.data.remote.dto.CartSummaryDto
import com.example.geofencing.data.remote.dto.GeofenceEventDto
import com.example.geofencing.data.remote.dto.SectorDetailDto
import com.example.geofencing.data.remote.dto.SectorSearchResultDto
import com.example.geofencing.data.remote.dto.SectorSummaryItemDto
import com.example.geofencing.data.remote.dto.SiteSummaryDto
import com.google.android.gms.maps.model.LatLng
import java.time.Instant

// GeoJSON은 [lng, lat] 순서, Android LatLng는 (lat, lng) 생성자 순서 - 항상 뒤집어야 한다.
private fun List<Double>.toLatLng(): LatLng = LatLng(this[1], this[0])

fun CartSummaryDto.toDomain(): CartSummary = CartSummary(
    total = total,
    violating = geofenceStatus.violating,
    compliant = geofenceStatus.compliant,
    driving = drivingStatus?.driving ?: 0,
    idle = drivingStatus?.idle ?: 0
)

fun SectorSummaryItemDto.toDomain(): SectorOverview = SectorOverview(
    id = id,
    name = name,
    address = address,
    geofence = geofence.outerRing.map { it.toLatLng() }
)

fun SiteSummaryDto.toDomain(): SiteSummary = SiteSummary(
    siteId = site.id,
    siteName = site.name,
    sectorCount = sectorCount,
    sectors = sectors.map { it.toDomain() },
    cartSummary = cartSummary.toDomain()
)

fun SectorDetailDto.toDomain(): SectorDetail = SectorDetail(
    id = id,
    name = name,
    address = address,
    cartSummary = cartSummary.toDomain()
)

fun SectorSearchResultDto.toDomain(): SectorSearchResult = SectorSearchResult(id = id, name = name)

fun GeofenceEventDto.toDomain(): GeofenceEventInfo = GeofenceEventInfo(
    id = id,
    cartId = cartId,
    sectorId = sectorId,
    occurredAt = Instant.parse(occurredAt),
    location = location.coordinates.toLatLng()
)
