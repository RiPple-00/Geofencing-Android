package com.example.geofencing.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// 신규 이탈(violation) 확인 배지에 쓰는 "마지막으로 확인한 시각" 하나만 저장하는 최소
// 스코프 DataStore. 관계형 데이터가 필요 없는 단일 값이라 Room 대신 Preferences로 충분.
val Context.violationAckDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "violation_ack_prefs"
)
