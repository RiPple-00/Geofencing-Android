package com.example.geofencing.ui.cart

import com.example.geofencing.ui.components.StatusKind
import com.example.geofencing.ui.map.CartMarker
import com.example.geofencing.ui.mock.MockSector
import com.example.geofencing.ui.mock.MockSectors
import com.example.geofencing.ui.mock.mockSectorByName

// MockSector + 카트 id → 카트 상세. 목록/지도와 같은 carts를 봐 상태 일치.
// ViewModel(CartViewModel)이 Repository 데이터로 이 함수를 호출한다.
fun cartStateFrom(sector: MockSector, cartId: String): CartUiState {
    val cart = sector.carts.find { it.id == cartId } ?: sector.carts.first()
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

// 이름으로 조회하는 편의 함수(프리뷰/폴백용). 실 흐름은 CartViewModel이 Repository로 처리.
fun cartStateFor(sectorName: String, cartName: String): CartUiState =
    cartStateFrom(mockSectorByName(sectorName) ?: MockSectors.first(), cartName)

// 프리뷰용 3케이스(Sector #1: Cart #1=violation, #2=disconnect, #3=compliance).
fun sampleCartComplianceState() = cartStateFor("Sector #1", "Cart #3")
fun sampleCartViolationState() = cartStateFor("Sector #1", "Cart #1")
fun sampleCartDisconnectState() = cartStateFor("Sector #1", "Cart #2")
