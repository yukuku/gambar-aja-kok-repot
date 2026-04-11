package yuku.gambaraja.kokrepot.stamp

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import yuku.gambaraja.kokrepot.model.StampType
import kotlin.math.cos
import kotlin.math.sin

fun DrawScope.drawStamp(
    center: Offset,
    stampType: StampType,
    color: Color,
    size: Float
) {
    when (stampType) {
        StampType.HEART -> drawHeart(center, color, size)
        StampType.STAR -> drawStar(center, color, size)
        StampType.SPIRAL -> drawSpiral(center, color, size)
        StampType.SMILEY -> drawSmiley(center, color, size)
        StampType.SQUARE -> drawSquare(center, color, size)
    }
}

private fun DrawScope.drawHeart(center: Offset, color: Color, size: Float) {
    val path = Path().apply {
        val cx = center.x
        val cy = center.y
        val s = size
        moveTo(cx, cy + s * 0.3f)
        cubicTo(cx - s * 1.0f, cy - s * 0.5f, cx - s * 0.5f, cy - s * 1.0f, cx, cy - s * 0.4f)
        cubicTo(cx + s * 0.5f, cy - s * 1.0f, cx + s * 1.0f, cy - s * 0.5f, cx, cy + s * 0.3f)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawStar(center: Offset, color: Color, size: Float) {
    val path = Path()
    val outerRadius = size
    val innerRadius = size * 0.4f
    for (i in 0 until 10) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = Math.toRadians((i * 36.0 - 90.0))
        val x = center.x + radius * cos(angle).toFloat()
        val y = center.y + radius * sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}

private fun DrawScope.drawSpiral(center: Offset, color: Color, size: Float) {
    val path = Path()
    val turns = 3
    val points = 100
    for (i in 0..points) {
        val t = i.toFloat() / points
        val angle = turns * 2 * Math.PI * t
        val radius = size * t
        val x = center.x + radius * cos(angle).toFloat()
        val y = center.y + radius * sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = size * 0.12f)
    )
}

private fun DrawScope.drawSmiley(center: Offset, color: Color, size: Float) {
    val strokeWidth = size * 0.1f
    // Face outline
    drawCircle(
        color = color,
        radius = size,
        center = center,
        style = Stroke(width = strokeWidth)
    )
    // Left eye
    drawCircle(
        color = color,
        radius = size * 0.12f,
        center = Offset(center.x - size * 0.35f, center.y - size * 0.25f)
    )
    // Right eye
    drawCircle(
        color = color,
        radius = size * 0.12f,
        center = Offset(center.x + size * 0.35f, center.y - size * 0.25f)
    )
    // Mouth arc
    val mouthPath = Path().apply {
        val mouthRect = Rect(
            left = center.x - size * 0.5f,
            top = center.y - size * 0.1f,
            right = center.x + size * 0.5f,
            bottom = center.y + size * 0.6f
        )
        arcTo(mouthRect, 0f, 180f, false)
    }
    drawPath(
        path = mouthPath,
        color = color,
        style = Stroke(width = strokeWidth)
    )
}

private fun DrawScope.drawSquare(center: Offset, color: Color, size: Float) {
    val halfSize = size * 0.7f
    val path = Path().apply {
        moveTo(center.x - halfSize, center.y - halfSize)
        lineTo(center.x + halfSize, center.y - halfSize)
        lineTo(center.x + halfSize, center.y + halfSize)
        lineTo(center.x - halfSize, center.y + halfSize)
        close()
    }
    drawPath(path, color)
}
