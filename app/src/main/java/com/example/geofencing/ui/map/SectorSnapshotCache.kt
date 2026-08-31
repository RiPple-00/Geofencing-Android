package com.example.geofencing.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.asImageBitmap
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

// v9: geofence 캐시 키를 hashCode()에서 좌표 기반 SHA-256 다이제스트로 교체(구 v8 항목은 prune됨).
private const val SnapshotCacheVersion = "v9"

object SectorSnapshotCache {
    private val memory = mutableStateMapOf<String, SectorSnapshot>()

    fun keyOf(sectorId: Int, geofence: List<LatLng>, widthDp: Int, heightDp: Int): String =
        "sector_${sectorId}_${geofenceDigest(geofence)}_${widthDp}x${heightDp}_$SnapshotCacheVersion"

    // 좌표 기반 안정적 다이제스트(SHA-256 앞 8바이트 = 16 hex). hashCode()는 충돌 시 서로 다른
    // geofence가 같은 키가 되어 잘못된 스냅샷을 로드할 수 있어, 충돌 저항성 있는 digest를 쓴다.
    private fun geofenceDigest(geofence: List<LatLng>): String {
        val raw = geofence.joinToString(";") { "${it.latitude},${it.longitude}" }
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return digest.take(8).joinToString("") { "%02x".format(it) }
    }

    private fun dir(context: Context): File =
        File(context.cacheDir, "sector_snapshots").apply { mkdirs() }

    private fun pngFile(context: Context, key: String) = File(dir(context), "$key.png")
    private fun metaFile(context: Context, key: String) = File(dir(context), "$key.meta")

    fun peek(key: String): SectorSnapshot? = memory[key]

    fun isCached(context: Context, key: String): Boolean =
        memory.containsKey(key) || (pngFile(context, key).exists() && metaFile(context, key).exists())

    @Volatile
    private var pruned = false

    suspend fun pruneStale(context: Context) {
        if (pruned) return
        pruned = true
        withContext(Dispatchers.IO) {
            dir(context).listFiles()?.forEach { file ->
                if (!file.name.contains("_$SnapshotCacheVersion.")) file.delete()
            }
        }
    }

    suspend fun loadIntoMemory(context: Context, key: String) {
        if (memory.containsKey(key)) return
        val snap = withContext(Dispatchers.IO) {
            val png = pngFile(context, key)
            val meta = metaFile(context, key)
            if (!png.exists() || !meta.exists()) return@withContext null
            val parts = meta.readText().split(",")
            if (parts.size < 6) return@withContext null
            val bmp = BitmapFactory.decodeFile(png.absolutePath) ?: return@withContext null
            SectorSnapshot(
                bmp.asImageBitmap(),
                SnapshotProjection(
                    south = parts[0].toDouble(),
                    west = parts[1].toDouble(),
                    north = parts[2].toDouble(),
                    east = parts[3].toDouble(),
                    widthPx = parts[4].toInt(),
                    heightPx = parts[5].toInt()
                )
            )
        } ?: return
        memory[key] = snap
    }

    suspend fun store(context: Context, key: String, bitmap: Bitmap, projection: SnapshotProjection) {
        withContext(Dispatchers.IO) {
            pngFile(context, key).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            metaFile(context, key).writeText(
                "${projection.south},${projection.west},${projection.north}," +
                    "${projection.east},${projection.widthPx},${projection.heightPx}"
            )
        }
        memory[key] = SectorSnapshot(bitmap.asImageBitmap(), projection)
    }
}
