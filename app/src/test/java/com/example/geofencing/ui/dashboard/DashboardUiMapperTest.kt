package com.example.geofencing.ui.dashboard

import com.example.geofencing.ui.cart.cartUiStateFrom
import com.example.geofencing.ui.components.StatusKind
import com.example.geofencing.ui.sector.sectorUiStateFrom
import com.example.geofencing.ui.wholesector.wholeSectorUiStateFrom
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DashboardUiMapperTest {

    @Test
    fun `whole sector mapper aggregates dashboard sector counts`() {
        val state = wholeSectorUiStateFrom(listOf(sampleSector()))

        assertEquals(3, state.totalCarts)
        assertEquals(1, state.compliance)
        assertEquals(1, state.violation)
        assertEquals(1, state.disconnect)
        assertEquals("Sector A", state.sectors.single().name)
    }

    @Test
    fun `sector mapper keeps cart rows and map markers in the same order`() {
        val state = sectorUiStateFrom(sampleSector())

        assertEquals(listOf("Cart 1", "Cart 2", "Cart 3"), state.allCarts.map { it.cart })
        assertEquals(listOf("Cart 1", "Cart 2", "Cart 3"), state.carts.map { it.id })
    }

    @Test
    fun `cart mapper exposes violation details only for violating cart`() {
        val sector = sampleSector()

        val violation = cartUiStateFrom(sector, "Cart 1")!!
        val compliance = cartUiStateFrom(sector, "Cart 2")!!

        assertEquals("Cart 1", violation.cartName)
        assertEquals("3m 10s", violation.violation?.duration)
        assertNull(compliance.violation)
    }

    private fun sampleSector(): DashboardSector =
        DashboardSector(
            id = 7,
            name = "Sector A",
            address = "100 Test Way",
            totalCarts = 3,
            geofence = listOf(
                LatLng(37.0, 127.0),
                LatLng(37.0, 127.1),
                LatLng(37.1, 127.1)
            ),
            carts = listOf(
                DashboardCart(
                    id = "Cart 1",
                    position = LatLng(37.2, 127.2),
                    status = StatusKind.Violation,
                    drivingState = "Driving",
                    registeredId = "REG-1",
                    timestamp = "2026.08.31 10:00:00",
                    violationDuration = "3m 10s",
                    maxSpeed = "16 Km/h",
                    violationAtTime = "2026.08.31 09:56:50",
                    violationAtAddress = "Outside"
                ),
                DashboardCart(
                    id = "Cart 2",
                    position = LatLng(37.05, 127.05),
                    status = StatusKind.Compliance,
                    drivingState = "Idle",
                    registeredId = "REG-2",
                    timestamp = "2026.08.31 10:00:00"
                ),
                DashboardCart(
                    id = "Cart 3",
                    position = LatLng(37.06, 127.06),
                    status = StatusKind.Disconnect,
                    drivingState = "",
                    registeredId = "REG-3",
                    timestamp = "2026.08.31 10:00:00"
                )
            ),
            violationPoints = listOf(LatLng(37.2, 127.2))
        )
}
