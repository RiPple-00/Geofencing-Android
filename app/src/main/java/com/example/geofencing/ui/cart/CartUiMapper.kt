package com.example.geofencing.ui.cart

import com.example.geofencing.ui.components.StatusKind
import com.example.geofencing.ui.dashboard.DashboardSector
import com.example.geofencing.ui.map.CartMarker

// cartId에 해당하는 카트가 없으면 null (호출부에서 LoadState.Error로 처리). 예전엔 carts.first()로
// 폴백해 빈 carts에서 크래시하거나 엉뚱한 카트를 조용히 보여줄 수 있었다.
fun cartUiStateFrom(sector: DashboardSector, cartId: String): CartUiState? {
    val cart = sector.carts.find { it.id == cartId } ?: return null
    return CartUiState(
        sectorName = sector.name,
        cartName = cart.id,
        registeredId = cart.registeredId,
        operationStatus = if (cart.status == StatusKind.Disconnect) "Disconnect" else cart.drivingState,
        geofenceStatus = cart.status,
        timestamp = cart.timestamp,
        address = sector.address,
        violation = cart.violationDuration?.let {
            ViolationDetail(
                duration = it,
                maxSpeed = cart.maxSpeed ?: "",
                atTime = cart.violationAtTime ?: "",
                atAddress = cart.violationAtAddress ?: ""
            )
        },
        geofence = sector.geofence,
        carts = sector.carts.map { CartMarker(it.id, it.position, it.status) }
    )
}
