package com.kd.creavity.presentation.canvas

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import com.kd.creavity.domain.model.Stroke

/**
 * Rendered geometry (vector path + culling bounds) for strokes, owned in one place so a cached
 * shape can never outlive the shape it describes.
 *
 * The key is derived from the stroke's geometry rather than from its id alone:
 *
 * - a peer's stroke streams in under a constant id while its point list grows;
 * - a dragged stroke changes coordinates without changing its point count.
 *
 * Both mutate the drawn shape under a stable id, so anything that changes the shape must change
 * the key. [geometryFingerprint] covers point count, endpoints, tool and width.
 */
class StrokeGeometryCache {

    private data class Key(val strokeId: String, val isMoving: Boolean, val geometry: Int)

    private val paths = HashMap<Key, Path>()
    private val bounds = HashMap<Key, Rect>()
    private val liveKey = HashMap<String, Key>()

    fun pathFor(stroke: Stroke, isMoving: Boolean, build: () -> Path): Path {
        val key = keyFor(stroke, isMoving)
        return paths.getOrPut(key) { build() }
    }

    fun boundsFor(stroke: Stroke, isMoving: Boolean, build: () -> Rect): Rect {
        val key = keyFor(stroke, isMoving)
        return bounds.getOrPut(key) { build() }
    }

    /** Drops every cached entry for a stroke that no longer exists (erased, deleted, page switch). */
    fun removeStroke(strokeId: String) {
        liveKey.remove(strokeId)
        paths.keys.removeAll { it.strokeId == strokeId }
        bounds.keys.removeAll { it.strokeId == strokeId }
    }

    fun clear() {
        paths.clear()
        bounds.clear()
        liveKey.clear()
    }

    internal fun pathCount(): Int = paths.size

    internal fun boundsCount(): Int = bounds.size

    private fun keyFor(stroke: Stroke, isMoving: Boolean): Key {
        val key = Key(stroke.id, isMoving, geometryFingerprint(stroke))
        val superseded = liveKey.put(stroke.id, key)
        if (superseded != null && superseded != key) {
            paths.remove(superseded)
            bounds.remove(superseded)
        }
        return key
    }

    private fun geometryFingerprint(stroke: Stroke): Int {
        var hash = stroke.tool.ordinal
        hash = 31 * hash + stroke.strokeWidth.toRawBits()
        hash = 31 * hash + stroke.points.size
        val first = stroke.points.firstOrNull() ?: return hash
        val last = stroke.points.lastOrNull() ?: return hash
        hash = 31 * hash + first.x.toRawBits()
        hash = 31 * hash + first.y.toRawBits()
        hash = 31 * hash + last.x.toRawBits()
        hash = 31 * hash + last.y.toRawBits()
        return hash
    }
}
