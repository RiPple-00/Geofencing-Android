package com.example.geofencing.ui.map

// Cloud 기반 지도 스타일용 Map ID. 콘솔에서 이 Map ID에 스타일이 연결돼 있어야 한다.
// Map ID를 지정하면 JSON 스타일(R.raw.map_style_dark, MapProperties.mapStyleOptions)은 무시된다.
// (Style ID 81550174fda52b86eef15d24 는 콘솔 연결용이라 코드에선 쓰지 않음.)
const val GeofenceMapId = "11c571aab1bba3a3d771c9de"

// Minimum zoom allowed for expanded Sector maps. This prevents the map from shrinking too far out.
internal const val HeatmapMinZoom = 15f

internal const val DefaultFitZoom = HeatmapMinZoom

// Baseline zoom used as the map's visual "100%" scale.
internal const val MapZoom100Percent = 17f

// Maximum zoom allowed for expanded Sector maps.
internal const val HeatmapMaxZoom = 19f
