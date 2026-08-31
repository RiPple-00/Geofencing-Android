package com.example.geofencing.ui.map

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.sin

data class SnapshotProjection(
    val south: Double,
    val west: Double,
    val north: Double,
    val east: Double,
    val widthPx: Int,
    val heightPx: Int
)

data class SectorSnapshot(val bitmap: ImageBitmap, val projection: SnapshotProjection)

class StoredProjector(private val p: SnapshotProjection) : MapProjector {
    private fun worldX(lng: Double) = (lng + 180.0) / 360.0

    private fun worldY(lat: Double): Double {
        val s = sin(lat * PI / 180.0).coerceIn(-0.9999, 0.9999)
        return 0.5 - ln((1 + s) / (1 - s)) / (4 * PI)
    }

    private val x0 = worldX(p.west)
    private val x1 = worldX(p.east)
    private val y0 = worldY(p.north)
    private val y1 = worldY(p.south)

    override fun project(latLng: LatLng): Offset {
        val fx = (worldX(latLng.longitude) - x0) / (x1 - x0)
        val fy = (worldY(latLng.latitude) - y0) / (y1 - y0)
        return Offset((fx * p.widthPx).toFloat(), (fy * p.heightPx).toFloat())
    }
}

data class SectorSnapshotRequest(val sectorId: Int, val geofence: List<LatLng>)

val GeofenceFitMaxWidth = 208.dp
val GeofenceFitMaxHeight = 110.dp
