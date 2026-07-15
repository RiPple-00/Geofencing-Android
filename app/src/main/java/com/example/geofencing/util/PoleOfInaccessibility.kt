package com.example.geofencing.util

import com.google.android.gms.maps.model.LatLng
import java.util.PriorityQueue
import kotlin.math.min
import kotlin.math.sqrt

private const val SQRT2 = 1.4142135623730951

// Mapbox의 polylabel 알고리즘(Pole of Inaccessibility) 포팅 - 폴리곤 "안에서 경계로부터
// 가장 먼 점"을 찾아, 오목(ㄷ/L자)한 골프장 경계에서도 centroid와 달리 항상 폴리곤 내부의
// 점을 보장한다. 좁은 범위라 경위도를 평면 x/y로 근사해도 오차는 무시할 수준.
// precision 기본값 1e-6도(약 0.11m)는 핀 위치 용도로 충분히 정밀하다.
fun List<LatLng>.poleOfInaccessibilityOrElse(fallback: LatLng, precision: Double = 1e-6): LatLng {
    if (size < 3) return firstOrNull() ?: fallback

    // longitude를 x, latitude를 y로 취급.
    val ring = map { doubleArrayOf(it.longitude, it.latitude) }

    var minX = Double.MAX_VALUE
    var minY = Double.MAX_VALUE
    var maxX = -Double.MAX_VALUE
    var maxY = -Double.MAX_VALUE
    for (p in ring) {
        minX = min(minX, p[0])
        minY = min(minY, p[1])
        maxX = maxOf(maxX, p[0])
        maxY = maxOf(maxY, p[1])
    }

    val width = maxX - minX
    val height = maxY - minY
    val cellSize = min(width, height)
    if (cellSize == 0.0) return LatLng(minY, minX)

    val initialH = cellSize / 2.0
    val cellQueue = PriorityQueue<Cell>(compareByDescending { it.max })

    var x = minX
    while (x < maxX) {
        var y = minY
        while (y < maxY) {
            cellQueue.add(Cell(x + initialH, y + initialH, initialH, ring))
            y += cellSize
        }
        x += cellSize
    }

    var bestCell = getCentroidCell(ring)
    val bboxCell = Cell(minX + width / 2, minY + height / 2, 0.0, ring)
    if (bboxCell.d > bestCell.d) bestCell = bboxCell

    while (cellQueue.isNotEmpty()) {
        val cell = cellQueue.poll() ?: break
        if (cell.d > bestCell.d) bestCell = cell
        // 이 셀에서 나올 수 있는 최댓값이 지금까지 찾은 최선보다 나을 가능성이 없으면 버린다.
        if (cell.max - bestCell.d <= precision) continue

        val half = cell.h / 2
        cellQueue.add(Cell(cell.x - half, cell.y - half, half, ring))
        cellQueue.add(Cell(cell.x + half, cell.y - half, half, ring))
        cellQueue.add(Cell(cell.x - half, cell.y + half, half, ring))
        cellQueue.add(Cell(cell.x + half, cell.y + half, half, ring))
    }

    return LatLng(bestCell.y, bestCell.x)
}

private class Cell(val x: Double, val y: Double, val h: Double, ring: List<DoubleArray>) {
    // 셀 중심에서 폴리곤 경계까지의 signed distance (내부면 양수, 외부면 음수).
    val d: Double = pointToPolygonDist(x, y, ring)
    // 이 셀에서 나올 수 있는 이론적 최댓값(중심 거리 + 셀 반지름의 대각선).
    val max: Double = d + h * SQRT2
}

private fun getCentroidCell(ring: List<DoubleArray>): Cell {
    var area = 0.0
    var cx = 0.0
    var cy = 0.0
    var i = 0
    var j = ring.size - 1
    while (i < ring.size) {
        val a = ring[i]
        val b = ring[j]
        val f = a[0] * b[1] - b[0] * a[1]
        cx += (a[0] + b[0]) * f
        cy += (a[1] + b[1]) * f
        area += f * 3
        j = i
        i++
    }
    if (area == 0.0) return Cell(ring[0][0], ring[0][1], 0.0, ring)
    return Cell(cx / area, cy / area, 0.0, ring)
}

// 점 (x,y)에서 폴리곤 경계까지의 최단 거리에 부호를 붙인 값(내부:+, 외부:-).
private fun pointToPolygonDist(x: Double, y: Double, ring: List<DoubleArray>): Double {
    var inside = false
    var minDistSq = Double.MAX_VALUE

    var i = 0
    var j = ring.size - 1
    while (i < ring.size) {
        val a = ring[i]
        val b = ring[j]

        if ((a[1] > y) != (b[1] > y) &&
            x < (b[0] - a[0]) * (y - a[1]) / (b[1] - a[1]) + a[0]
        ) {
            inside = !inside
        }

        minDistSq = min(minDistSq, segmentDistSq(x, y, a, b))
        j = i
        i++
    }

    val dist = sqrt(minDistSq)
    return if (inside) dist else -dist
}

private fun segmentDistSq(px: Double, py: Double, a: DoubleArray, b: DoubleArray): Double {
    var x = a[0]
    var y = a[1]
    var dx = b[0] - x
    var dy = b[1] - y

    if (dx != 0.0 || dy != 0.0) {
        val t = ((px - x) * dx + (py - y) * dy) / (dx * dx + dy * dy)
        if (t > 1) {
            x = b[0]
            y = b[1]
        } else if (t > 0) {
            x += dx * t
            y += dy * t
        }
    }

    dx = px - x
    dy = py - y
    return dx * dx + dy * dy
}
