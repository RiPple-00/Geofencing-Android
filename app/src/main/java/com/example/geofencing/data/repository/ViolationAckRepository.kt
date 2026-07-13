package com.example.geofencing.data.repository

import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface ViolationAckRepository {
    // 사용자가 마지막으로 Violation 탭을 확인한 시각 - 이보다 나중에 발생한 이탈 이벤트가
    // 있으면 아직 확인하지 않은 신규 violation으로 간주한다.
    val lastAcknowledgedAt: Flow<Instant>

    suspend fun acknowledgeNow()
}
