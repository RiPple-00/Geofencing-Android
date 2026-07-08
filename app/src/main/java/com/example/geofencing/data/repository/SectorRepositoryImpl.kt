package com.example.geofencing.data.repository

import com.example.geofencing.data.model.Sector
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SectorRepositoryImpl @Inject constructor() : SectorRepository {

    // TODO: 실제 데이터 소스(서버 API 등)로 교체. 제목/부제 필드가 확정되면 Sector 모델도 함께 조정.
    private val sectors = listOf(
        Sector(id = "1", name = "Sector #1", cartCount = 16, position = LatLng(51.4967, 0.1058)),
        Sector(id = "2", name = "Sector #2", cartCount = 16, position = LatLng(51.5423, 0.1638), isCritical = true)
    )

    override fun getSectors(): List<Sector> = sectors
}
