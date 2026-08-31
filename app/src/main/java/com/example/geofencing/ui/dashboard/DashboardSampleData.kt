package com.example.geofencing.ui.dashboard

import com.example.geofencing.ui.components.StatusKind
import com.google.android.gms.maps.model.LatLng

private const val SAMPLE_ADDRESS = "1776 Terminal Dr, Richland, WA 99354"

private val sector1Geofence = listOf(
    LatLng(37.5683, 126.9758), LatLng(37.5681, 126.9804),
    LatLng(37.5661, 126.9810), LatLng(37.5646, 126.9788), LatLng(37.5653, 126.9756)
)

private val sector2Geofence = listOf(
    LatLng(37.5722, 126.9810), LatLng(37.5724, 126.9834), LatLng(37.5710, 126.9852),
    LatLng(37.5690, 126.9844), LatLng(37.5687, 126.9814), LatLng(37.5703, 126.9800)
)

private val sector3Geofence = listOf(
    LatLng(37.5646, 126.9722), LatLng(37.5649, 126.9750), LatLng(37.5641, 126.9768),
    LatLng(37.5645, 126.9782), LatLng(37.5629, 126.9780), LatLng(37.5617, 126.9762),
    LatLng(37.5613, 126.9744), LatLng(37.5621, 126.9728), LatLng(37.5633, 126.9718)
)

private fun sampleDashboardCarts(
    geofence: List<LatLng>,
    violation: Int,
    disconnect: Int,
    count: Int = 8
): List<DashboardCart> {
    val lats = geofence.map { it.latitude }
    val lngs = geofence.map { it.longitude }
    val cLat = lats.average()
    val cLng = lngs.average()
    val halfLat = (lats.maxOrNull()!! - lats.minOrNull()!!) / 2
    val halfLng = (lngs.maxOrNull()!! - lngs.minOrNull()!!) / 2
    val insideFracs = listOf(
        0.0 to 0.0, 0.25 to 0.3, -0.3 to 0.2, 0.2 to -0.3,
        -0.25 to -0.2, 0.35 to 0.1, -0.15 to 0.35, 0.1 to -0.4
    )
    val outsideFracs = listOf(
        1.3 to 0.2, -0.2 to 1.3, 1.2 to -0.9, -1.1 to -0.7
    )
    return (0 until count).map { i ->
        val status = when {
            i < violation -> StatusKind.Violation
            i < violation + disconnect -> StatusKind.Disconnect
            else -> StatusKind.Compliance
        }
        val isViolation = status == StatusKind.Violation
        val (fLat, fLng) = if (isViolation) {
            outsideFracs[i % outsideFracs.size]
        } else {
            insideFracs[i % insideFracs.size]
        }
        DashboardCart(
            id = "Cart #${i + 1}",
            position = LatLng(cLat + fLat * halfLat, cLng + fLng * halfLng),
            status = status,
            drivingState = if (status == StatusKind.Disconnect) "" else "Driving",
            registeredId = "DeltaX-T%02d".format(i + 1),
            timestamp = "2026.07.15 15:05:00",
            violationDuration = if (isViolation) "8m 45s" else null,
            maxSpeed = if (isViolation) "16 Km/h" else null,
            violationAtTime = if (isViolation) "2026.07.20 15:02:30" else null,
            violationAtAddress = if (isViolation) SAMPLE_ADDRESS else null
        )
    }
}

private fun violationPointsNear(geofence: List<LatLng>): List<LatLng> {
    val cLat = geofence.map { it.latitude }.average()
    val cLng = geofence.map { it.longitude }.average()
    return listOf(
        LatLng(cLat + 0.0006, cLng + 0.0007), LatLng(cLat + 0.0004, cLng + 0.0005),
        LatLng(cLat + 0.0007, cLng + 0.0008), LatLng(cLat + 0.0005, cLng + 0.0004),
        LatLng(cLat + 0.0003, cLng + 0.0006), LatLng(cLat + 0.0008, cLng + 0.0006),
        LatLng(cLat - 0.0004, cLng - 0.0003), LatLng(cLat - 0.0002, cLng - 0.0005),
        LatLng(cLat - 0.0006, cLng - 0.0002)
    )
}

val SampleDashboardSectors: List<DashboardSector> = listOf(
    DashboardSector(
        1,
        "Sector #1",
        SAMPLE_ADDRESS,
        47,
        sector1Geofence,
        sampleDashboardCarts(sector1Geofence, violation = 1, disconnect = 1),
        violationPointsNear(sector1Geofence)
    ),
    DashboardSector(
        2,
        "Sector #2",
        SAMPLE_ADDRESS,
        8,
        sector2Geofence,
        sampleDashboardCarts(sector2Geofence, violation = 0, disconnect = 1),
        violationPointsNear(sector2Geofence)
    ),
    DashboardSector(
        3,
        "Sector #3",
        SAMPLE_ADDRESS,
        12,
        sector3Geofence,
        sampleDashboardCarts(sector3Geofence, violation = 2, disconnect = 0),
        violationPointsNear(sector3Geofence)
    )
)

fun sampleDashboardSectorByName(name: String): DashboardSector? =
    SampleDashboardSectors.find { it.name == name }
