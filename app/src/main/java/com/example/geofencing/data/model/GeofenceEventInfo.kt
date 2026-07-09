package com.example.geofencing.data.model

import com.google.android.gms.maps.model.LatLng
import java.time.Instant

// GET /sites/{siteId}/geofence-events 응답의 도메인 표현.
// 이탈 값만을 다룸. (진입 이벤트는 필요 없으므로 API에서 제외)
// occurredAt은 UTC Instant로 보관 - KST 등 로컬 표시 변환은 UI(포맷팅) 단에서 담당.
data class GeofenceEventInfo(
    val id: Int,
    val cartId: Int,
    val sectorId: Int,
    val occurredAt: Instant,
    val location: LatLng
)
