package yuku.gambaraja.kokrepot.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

sealed class DrawingAction {
    abstract val bounds: Rect

    data class Stroke(
        val points: List<Offset>,
        val color: Int,
        val thickness: Float,
        val isEraser: Boolean = false
    ) : DrawingAction() {
        override val bounds: Rect by lazy {
            if (points.isEmpty()) return@lazy Rect.Zero
            var minX = Float.MAX_VALUE
            var minY = Float.MAX_VALUE
            var maxX = Float.MIN_VALUE
            var maxY = Float.MIN_VALUE
            for (p in points) {
                if (p.x < minX) minX = p.x
                if (p.y < minY) minY = p.y
                if (p.x > maxX) maxX = p.x
                if (p.y > maxY) maxY = p.y
            }
            val pad = thickness / 2f
            Rect(minX - pad, minY - pad, maxX + pad, maxY + pad)
        }
    }

    data class Stamp(
        val center: Offset,
        val stampType: StampType,
        val color: Int,
        val size: Float
    ) : DrawingAction() {
        override val bounds: Rect by lazy {
            Rect(
                center.x - size,
                center.y - size,
                center.x + size,
                center.y + size
            )
        }
    }
}
