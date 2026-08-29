package yuku.gambaraja.kokrepot.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path

/** One contiguous horizontal run of filled pixels, relative to [DrawingAction.Fill]'s origin. */
data class FillSpan(val row: Int, val x0: Int, val x1: Int)

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

    /**
     * A flood-filled (paint bucket) region, produced by rasterizing the visible
     * viewport and running a scanline flood fill from the tap point. [spans] are
     * the filled pixel runs, one world unit == one pixel, relative to
     * ([originX], [originY]).
     */
    data class Fill(
        val originX: Float,
        val originY: Float,
        val width: Int,
        val height: Int,
        val color: Int,
        val spans: List<FillSpan>
    ) : DrawingAction() {
        override val bounds: Rect by lazy {
            Rect(originX, originY, originX + width, originY + height)
        }

        val path: Path by lazy {
            Path().apply {
                for (span in spans) {
                    addRect(
                        Rect(
                            originX + span.x0,
                            originY + span.row,
                            originX + span.x1 + 1f,
                            originY + span.row + 1f
                        )
                    )
                }
            }
        }
    }
}
