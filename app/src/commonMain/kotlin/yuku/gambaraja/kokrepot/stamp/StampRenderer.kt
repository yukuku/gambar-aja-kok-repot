package yuku.gambaraja.kokrepot.stamp

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import yuku.gambaraja.kokrepot.model.StampType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draw a stamp. When [borderColor] is supplied, an outline in that color is drawn
 * around/underneath the filled shape, so the stamp shape stays visible even when
 * [color] is close to the surrounding background color. The outline always follows
 * the stamp's own silhouette — it is never a bounding-box rectangle.
 */
fun DrawScope.drawStamp(
    center: Offset,
    stampType: StampType,
    color: Color,
    size: Float,
    borderColor: Color? = null,
) {
    when (stampType) {
        StampType.HEART -> drawHeart(center, color, size, borderColor)
        StampType.STAR -> drawStar(center, color, size, borderColor)
        StampType.SPIRAL -> drawSpiral(center, color, size, borderColor)
        StampType.SMILEY -> drawSmiley(center, color, size, borderColor)
        StampType.SQUARE -> drawSquare(center, color, size, borderColor)
    }
}

private fun heartPath(center: Offset, size: Float): Path {
    val cx = center.x
    val cy = center.y
    val s = size
    val tipY = cy + s * 0.75f
    val valleyY = cy - s * 0.25f
    val lobeX = s * 0.95f
    val lobeTopY = cy - s * 0.8f
    val controlLowY = cy + s * 0.15f
    return Path().apply {
        moveTo(cx, tipY)
        cubicTo(cx - lobeX, controlLowY, cx - lobeX, lobeTopY, cx, valleyY)
        cubicTo(cx + lobeX, lobeTopY, cx + lobeX, controlLowY, cx, tipY)
        close()
    }
}

private fun DrawScope.drawHeart(center: Offset, color: Color, size: Float, borderColor: Color?) {
    val path = heartPath(center, size)
    if (borderColor != null) {
        drawPath(path, borderColor, style = Stroke(width = size * 0.3f))
    }
    drawPath(path, color)
}

private fun starPath(center: Offset, size: Float): Path {
    val path = Path()
    val outerRadius = size
    val innerRadius = size * 0.4f
    for (i in 0 until 10) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = (i * 36.0 - 90.0) * PI / 180.0
        val x = center.x + radius * cos(angle).toFloat()
        val y = center.y + radius * sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private fun DrawScope.drawStar(center: Offset, color: Color, size: Float, borderColor: Color?) {
    val path = starPath(center, size)
    if (borderColor != null) {
        drawPath(path, borderColor, style = Stroke(width = size * 0.3f))
    }
    drawPath(path, color)
}

private fun spiralPath(center: Offset, size: Float): Path {
    val path = Path()
    val turns = 3
    val points = 100
    for (i in 0..points) {
        val t = i.toFloat() / points
        val angle = turns * 2 * PI * t
        val radius = size * t
        val x = center.x + radius * cos(angle).toFloat()
        val y = center.y + radius * sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    return path
}

private fun DrawScope.drawSpiral(center: Offset, color: Color, size: Float, borderColor: Color?) {
    val path = spiralPath(center, size)
    if (borderColor != null) {
        drawPath(path, borderColor, style = Stroke(width = size * 0.22f))
    }
    drawPath(path, color, style = Stroke(width = size * 0.12f))
}

private fun DrawScope.drawSmiley(center: Offset, color: Color, size: Float, borderColor: Color?) {
    val strokeWidth = size * 0.1f
    val leftEyeCenter = Offset(center.x - size * 0.35f, center.y - size * 0.25f)
    val rightEyeCenter = Offset(center.x + size * 0.35f, center.y - size * 0.25f)
    val eyeRadius = size * 0.12f
    val mouthPath = Path().apply {
        val mouthRect = Rect(
            left = center.x - size * 0.5f,
            top = center.y - size * 0.1f,
            right = center.x + size * 0.5f,
            bottom = center.y + size * 0.6f
        )
        arcTo(mouthRect, 0f, 180f, false)
    }

    if (borderColor != null) {
        drawCircle(
            color = borderColor,
            radius = size,
            center = center,
            style = Stroke(width = strokeWidth * 2.2f)
        )
        drawCircle(
            color = borderColor,
            radius = eyeRadius + strokeWidth * 0.6f,
            center = leftEyeCenter
        )
        drawCircle(
            color = borderColor,
            radius = eyeRadius + strokeWidth * 0.6f,
            center = rightEyeCenter
        )
        drawPath(
            path = mouthPath,
            color = borderColor,
            style = Stroke(width = strokeWidth * 2.2f)
        )
    }
    drawCircle(
        color = color,
        radius = size,
        center = center,
        style = Stroke(width = strokeWidth)
    )
    drawCircle(
        color = color,
        radius = eyeRadius,
        center = leftEyeCenter
    )
    drawCircle(
        color = color,
        radius = eyeRadius,
        center = rightEyeCenter
    )
    drawPath(
        path = mouthPath,
        color = color,
        style = Stroke(width = strokeWidth)
    )
}

private fun squarePath(center: Offset, size: Float): Path {
    val halfSize = size * 0.7f
    return Path().apply {
        moveTo(center.x - halfSize, center.y - halfSize)
        lineTo(center.x + halfSize, center.y - halfSize)
        lineTo(center.x + halfSize, center.y + halfSize)
        lineTo(center.x - halfSize, center.y + halfSize)
        close()
    }
}

private fun DrawScope.drawSquare(center: Offset, color: Color, size: Float, borderColor: Color?) {
    val path = squarePath(center, size)
    if (borderColor != null) {
        drawPath(path, borderColor, style = Stroke(width = size * 0.3f))
    }
    drawPath(path, color)
}
