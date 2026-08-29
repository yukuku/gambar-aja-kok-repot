package yuku.gambaraja.kokrepot.ui.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import yuku.gambaraja.kokrepot.model.DrawingAction
import yuku.gambaraja.kokrepot.model.FillSpan
import kotlin.math.ceil
import kotlin.math.floor

/**
 * A truly infinite canvas has no natural boundary to flood-fill against, so the
 * fill is scoped to what's currently on screen. Capped further as a sanity
 * limit in case a huge/zero-sized viewport ever reaches this code.
 */
private const val MAX_FLOOD_FILL_DIMENSION = 4096

/**
 * Rasterizes the visible viewport (one world unit == one pixel, matching how
 * [DrawingCanvas] itself renders) into an offscreen bitmap, then runs a
 * stack-based scanline flood fill starting at [startWorldPos]. The filled
 * pixels are packed into row spans and returned as a new [DrawingAction.Fill],
 * or null if the tap was out of bounds or nothing was filled.
 */
fun computeFloodFill(
    actions: List<DrawingAction>,
    viewportWorldRect: Rect,
    startWorldPos: Offset,
    fillColor: Color,
): DrawingAction.Fill? {
    val left = floor(viewportWorldRect.left).toInt()
    val top = floor(viewportWorldRect.top).toInt()
    val width = ceil(viewportWorldRect.right).toInt() - left
    val height = ceil(viewportWorldRect.bottom).toInt() - top
    if (width <= 0 || height <= 0) return null
    if (width > MAX_FLOOD_FILL_DIMENSION || height > MAX_FLOOD_FILL_DIMENSION) return null

    val startX = floor(startWorldPos.x).toInt() - left
    val startY = floor(startWorldPos.y).toInt() - top
    if (startX < 0 || startX >= width || startY < 0 || startY >= height) return null

    val bitmap = ImageBitmap(width, height)
    val canvas = Canvas(bitmap)
    val rasterRect = Rect(left.toFloat(), top.toFloat(), (left + width).toFloat(), (top + height).toFloat())
    CanvasDrawScope().draw(
        density = Density(1f),
        layoutDirection = LayoutDirection.Ltr,
        canvas = canvas,
        size = Size(width.toFloat(), height.toFloat())
    ) {
        drawRect(Color.White)
        translate(left = -left.toFloat(), top = -top.toFloat()) {
            for (action in actions) {
                if (!action.bounds.overlaps(rasterRect)) continue
                drawAction(action)
            }
        }
    }

    val pixels = IntArray(width * height)
    bitmap.readPixels(pixels, 0, 0, width, height, 0, width)

    val startIndex = startY * width + startX
    val target = pixels[startIndex]
    val fillArgb = fillColor.toArgb()
    if (target == fillArgb) return null

    val filled = BooleanArray(pixels.size)

    fun matches(x: Int, y: Int) = pixels[y * width + x] == target && !filled[y * width + x]

    val stack = ArrayDeque<Int>()
    stack.addLast(startIndex)
    while (stack.isNotEmpty()) {
        val idx = stack.removeLast()
        val startXOfRow = idx % width
        val y = idx / width
        if (filled[idx] || pixels[idx] != target) continue

        var xLeft = startXOfRow
        while (xLeft - 1 >= 0 && matches(xLeft - 1, y)) xLeft--
        var xRight = startXOfRow
        while (xRight + 1 < width && matches(xRight + 1, y)) xRight++

        var spanAboveOpen = false
        var spanBelowOpen = false
        for (xi in xLeft..xRight) {
            filled[y * width + xi] = true
            if (y > 0) {
                val above = matches(xi, y - 1)
                if (above && !spanAboveOpen) {
                    stack.addLast((y - 1) * width + xi)
                    spanAboveOpen = true
                } else if (!above) {
                    spanAboveOpen = false
                }
            }
            if (y < height - 1) {
                val below = matches(xi, y + 1)
                if (below && !spanBelowOpen) {
                    stack.addLast((y + 1) * width + xi)
                    spanBelowOpen = true
                } else if (!below) {
                    spanBelowOpen = false
                }
            }
        }
    }

    var minX = width
    var maxX = -1
    var minY = height
    var maxY = -1
    val rawSpans = mutableListOf<Triple<Int, Int, Int>>()
    for (y in 0 until height) {
        var x = 0
        while (x < width) {
            if (filled[y * width + x]) {
                val spanStart = x
                while (x < width && filled[y * width + x]) x++
                val spanEnd = x - 1
                rawSpans.add(Triple(y, spanStart, spanEnd))
                if (spanStart < minX) minX = spanStart
                if (spanEnd > maxX) maxX = spanEnd
                if (y < minY) minY = y
                if (y > maxY) maxY = y
            } else {
                x++
            }
        }
    }
    if (rawSpans.isEmpty()) return null

    val spans = rawSpans.map { (y, x0, x1) ->
        FillSpan(row = y - minY, x0 = x0 - minX, x1 = x1 - minX)
    }
    return DrawingAction.Fill(
        originX = (left + minX).toFloat(),
        originY = (top + minY).toFloat(),
        width = maxX - minX + 1,
        height = maxY - minY + 1,
        color = fillArgb,
        spans = spans,
    )
}
