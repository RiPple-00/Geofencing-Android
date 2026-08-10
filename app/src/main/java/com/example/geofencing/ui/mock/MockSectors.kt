package com.example.geofencing.ui.mock

import com.example.geofencing.ui.components.StatusKind
import com.google.android.gms.maps.model.LatLng

// ── 단일 mock 소스 ──────────────────────────────────────────────────────────
// WholeSector / Sector / Cart 세 화면이 전부 여기서 파생한다. 카트의 상태·위치·위반상세가 한 곳에만
// 있어 어느 화면에서 봐도 일치한다. 섹터마다 각자 carts를 가지므로 페이지는 서로 다르다.
// TODO(실데이터): API/ViewModel 붙이면 이 파일(과 파생 함수)을 대체.

data class MockCart(
    val id: String,                  // "Cart #1"
    val position: LatLng,
    val status: StatusKind,
    val drivingState: String,        // Disconnect면 ""
    val registeredId: String,
    val timestamp: String,
    // 위반(Violation)일 때만 채워짐.
    val violationDuration: String? = null,
    val maxSpeed: String? = null,
    val violationAtTime: String? = null,
    val violationAtAddress: String? = null
)

data class MockSector(
    val id: Int,
    val name: String,
    val address: String,
    val totalCarts: Int,             // 총 카트 수(stat·pagination용). carts는 지도/목록에 보이는 집합.
    val geofence: List<LatLng>,
    val carts: List<MockCart>,
    val violationPoints: List<LatLng>
)

private const val SAMPLE_ADDRESS = "1776 Terminal Dr, Richland, WA 99354"

// 섹터별 geofence(모양이 다 다름).
private val sector1Geofence = listOf( // 오각형
    LatLng(37.5683, 126.9758), LatLng(37.5681, 126.9804),
    LatLng(37.5661, 126.9810), LatLng(37.5646, 126.9788), LatLng(37.5653, 126.9756)
)
private val sector2Geofence = listOf( // 육각형(세로로 긴)
    LatLng(37.5722, 126.9810), LatLng(37.5724, 126.9834), LatLng(37.5710, 126.9852),
    LatLng(37.5690, 126.9844), LatLng(37.5687, 126.9814), LatLng(37.5703, 126.9800)
)
private val sector3Geofence = listOf( // 불규칙 9각형(가로로 긴)
    LatLng(37.5646, 126.9722), LatLng(37.5649, 126.9750), LatLng(37.5641, 126.9768),
    LatLng(37.5645, 126.9782), LatLng(37.5629, 126.9780), LatLng(37.5617, 126.9762),
    LatLng(37.5613, 126.9744), LatLng(37.5621, 126.9728), LatLng(37.5633, 126.9718)
)

// geofence 바운딩박스 중심 기준으로 카트 배치(span 비율 스케일 → 모양 무관).
// 앞에서부터 violation → disconnect → compliance 순으로 상태 부여(섹터 카운트와 일치).
// Violation(지오펜스 이탈) 카트는 경계 "밖"(|비율|>1 = 바운딩박스 바깥), 나머지는 "안쪽".
private fun mockCarts(
    geofence: List<LatLng>,
    violation: Int,
    disconnect: Int,
    count: Int = 8
): List<MockCart> {
    val lats = geofence.map { it.latitude }
    val lngs = geofence.map { it.longitude }
    val cLat = lats.average()
    val cLng = lngs.average()
    val halfLat = (lats.maxOrNull()!! - lats.minOrNull()!!) / 2
    val halfLng = (lngs.maxOrNull()!! - lngs.minOrNull()!!) / 2
    // 안쪽(|비율|≤0.4) / 바깥(|비율|>1, 경계 살짝 밖)
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
        MockCart(
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

// 위반 발생 위치(히트맵용). 대략 centroid 주변 클러스터.
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

// 섹터별 데이터. violation/disconnect 카운트를 다르게 줘 화면이 뚜렷이 구분된다.
// 합계: 총 67, violation 1+0+2=3, disconnect 1+1+0=2 → compliance 62 (WholeSector 헤드라인과 일치).
val MockSectors: List<MockSector> = listOf(
    MockSector(1, "Sector #1", SAMPLE_ADDRESS, 47, sector1Geofence,
        mockCarts(sector1Geofence, violation = 1, disconnect = 1), violationPointsNear(sector1Geofence)),
    MockSector(2, "Sector #2", SAMPLE_ADDRESS, 8, sector2Geofence,
        mockCarts(sector2Geofence, violation = 0, disconnect = 1), violationPointsNear(sector2Geofence)),
    MockSector(3, "Sector #3", SAMPLE_ADDRESS, 12, sector3Geofence,
        mockCarts(sector3Geofence, violation = 2, disconnect = 0), violationPointsNear(sector3Geofence))
)

fun mockSectorByName(name: String): MockSector? = MockSectors.find { it.name == name }
