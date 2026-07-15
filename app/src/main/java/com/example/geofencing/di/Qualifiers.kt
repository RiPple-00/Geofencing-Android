package com.example.geofencing.di

import javax.inject.Qualifier

// DataStore<Preferences> 바인딩이 여러 개(violationAck, selectedSite)라 타입만으로는
// Hilt가 구분할 수 없어 각 용도별로 한정자를 둔다.
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ViolationAckPreferences

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SelectedSitePreferences
