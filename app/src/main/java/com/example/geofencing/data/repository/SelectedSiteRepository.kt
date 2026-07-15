package com.example.geofencing.data.repository

import kotlinx.coroutines.flow.Flow

interface SelectedSiteRepository {
    // 현재 선택된 사이트 id. 로그인/사이트 선택 화면이 생기기 전까지는 항상 기본값을 반환한다.
    val selectedSiteId: Flow<Int>

    suspend fun selectSite(siteId: Int)
}
