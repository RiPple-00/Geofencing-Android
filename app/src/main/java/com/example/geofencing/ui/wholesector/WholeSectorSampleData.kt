package com.example.geofencing.ui.wholesector

import com.example.geofencing.ui.dashboard.SampleDashboardSectors

val SampleSectorNames = SampleDashboardSectors.map { it.name }

fun sampleWholeSectorState(): WholeSectorUiState =
    wholeSectorUiStateFrom(SampleDashboardSectors)
