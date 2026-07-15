package com.example.geofencing.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// 현재 선택된 siteId 하나만 저장하는 최소 스코프 DataStore.
// 로그인/사이트 선택 UI가 생기기 전까지는 기본값(1)만 읽힌다.
val Context.selectedSiteDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "selected_site_prefs"
)
