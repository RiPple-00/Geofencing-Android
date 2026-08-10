package com.example.geofencing.ui.cart

import com.example.geofencing.ui.components.StatusKind
import com.example.geofencing.ui.map.CartMarker
import com.example.geofencing.ui.mock.MockSectors
import com.example.geofencing.ui.mock.mockSectorByName

// (sector, cart) → 카트 상세. 단일 mock 소스(MockSectors)에서 파생 → 목록/지도와 상태 일치.
fun cartStateFor(sectorName: String, cartName: String): CartUiState {
    val sector = mockSectorByName(sectorName) ?: MockSectors.first()
    val cart = sector.carts.find { it.id == cartName } ?: sector.carts.first()
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

// 프리뷰용 3케이스(Sector #1: Cart #1=violation, #2=disconnect, #3=compliance).
fun sampleCartComplianceState() = cartStateFor("Sector #1", "Cart #3")
fun sampleCartViolationState() = cartStateFor("Sector #1", "Cart #1")
fun sampleCartDisconnectState() = cartStateFor("Sector #1", "Cart #2")
