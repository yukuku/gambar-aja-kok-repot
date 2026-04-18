package yuku.gambaraja.kokrepot

import androidx.compose.ui.geometry.Offset
import kotlinx.browser.localStorage
import yuku.gambaraja.kokrepot.model.DrawingAction
import yuku.gambaraja.kokrepot.model.StampType

/**
 * Web persistence via localStorage. Stored as a textual format (one field per
 * line, space-separated per action) rather than raw binary because localStorage
 * is a string store. Uses a magic + version header for forward compatibility.
 */
actual class DrawingStorage {

    private companion object {
        const val KEY = "gambar-aja-kok-repot.drawing.v1"
        const val MAGIC = "GAKR"
        const val FORMAT_VERSION = 1
    }

    actual fun load(): DrawingSnapshot? {
        val raw = try {
            localStorage.getItem(KEY)
        } catch (e: Throwable) {
            null
        } ?: return null

        return try {
            val lines = raw.split('\n')
            if (lines.isEmpty()) return null
            val header = lines[0].split(' ')
            if (header.size < 5 || header[0] != MAGIC) return null
            if (header[1].toInt() != FORMAT_VERSION) return null
            val panX = header[2].toFloat()
            val panY = header[3].toFloat()
            val count = header[4].toInt()
            if (count < 0 || count > 1_000_000) return null

            val stampTypes = StampType.entries
            val actions = ArrayList<DrawingAction>(count)
            var i = 1
            repeat(count) {
                if (i >= lines.size) return null
                val parts = lines[i].split(' ')
                i++
                when (parts[0]) {
                    "s" -> {
                        // s <color> <thickness> <isEraser> <pointCount> <x1> <y1> <x2> <y2> ...
                        val color = parts[1].toInt()
                        val thickness = parts[2].toFloat()
                        val isEraser = parts[3] == "1"
                        val pointCount = parts[4].toInt()
                        if (pointCount < 0 || pointCount > 10_000_000) return null
                        val points = ArrayList<Offset>(pointCount)
                        var k = 5
                        repeat(pointCount) {
                            if (k + 1 >= parts.size) return null
                            points.add(Offset(parts[k].toFloat(), parts[k + 1].toFloat()))
                            k += 2
                        }
                        actions.add(
                            DrawingAction.Stroke(
                                points = points,
                                color = color,
                                thickness = thickness,
                                isEraser = isEraser,
                            )
                        )
                    }
                    "t" -> {
                        // t <cx> <cy> <stampTypeOrdinal> <color> <size>
                        val cx = parts[1].toFloat()
                        val cy = parts[2].toFloat()
                        val stampIdx = parts[3].toInt()
                        val color = parts[4].toInt()
                        val size = parts[5].toFloat()
                        if (stampIdx < 0 || stampIdx >= stampTypes.size) return null
                        actions.add(
                            DrawingAction.Stamp(
                                center = Offset(cx, cy),
                                stampType = stampTypes[stampIdx],
                                color = color,
                                size = size,
                            )
                        )
                    }
                    else -> return null
                }
            }
            DrawingSnapshot(actions, Offset(panX, panY))
        } catch (e: Throwable) {
            null
        }
    }

    actual fun save(snapshot: DrawingSnapshot) {
        try {
            val sb = StringBuilder()
            sb.append(MAGIC).append(' ')
                .append(FORMAT_VERSION).append(' ')
                .append(snapshot.panOffset.x).append(' ')
                .append(snapshot.panOffset.y).append(' ')
                .append(snapshot.actions.size).append('\n')
            for (action in snapshot.actions) {
                when (action) {
                    is DrawingAction.Stroke -> {
                        sb.append("s ")
                            .append(action.color).append(' ')
                            .append(action.thickness).append(' ')
                            .append(if (action.isEraser) "1" else "0").append(' ')
                            .append(action.points.size)
                        for (p in action.points) {
                            sb.append(' ').append(p.x).append(' ').append(p.y)
                        }
                        sb.append('\n')
                    }
                    is DrawingAction.Stamp -> {
                        sb.append("t ")
                            .append(action.center.x).append(' ')
                            .append(action.center.y).append(' ')
                            .append(action.stampType.ordinal).append(' ')
                            .append(action.color).append(' ')
                            .append(action.size).append('\n')
                    }
                }
            }
            localStorage.setItem(KEY, sb.toString())
        } catch (e: Throwable) {
            // Ignore — the toddler will not see an error dialog, and the drawing
            // is still kept in memory. If localStorage is full or unavailable,
            // we simply skip persistence.
        }
    }
}
