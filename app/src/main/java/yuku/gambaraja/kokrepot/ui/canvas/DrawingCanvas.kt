package yuku.gambaraja.kokrepot.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import yuku.gambaraja.kokrepot.model.DrawingAction
import yuku.gambaraja.kokrepot.stamp.drawStamp

/**
 * Build a smoothed path through [points] using quadratic Bézier curves between
 * midpoints of adjacent samples. Each raw sample becomes a control point, and
 * the curve passes through the midpoints between consecutive samples. This
 * rounds off the polyline jaggies you get from `lineTo` alone, especially on
 * fast strokes where the sample spacing is wide.
 */
private fun smoothedStrokePath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points[0].x, points[0].y)
    if (points.size == 2) {
        path.lineTo(points[1].x, points[1].y)
        return path
    }
    // For each interior point, draw a quadratic curve using the point itself as
    // the control and the midpoint to the next point as the endpoint.
    for (i in 1 until points.size - 1) {
        val midX = (points[i].x + points[i + 1].x) / 2f
        val midY = (points[i].y + points[i + 1].y) / 2f
        path.quadraticTo(points[i].x, points[i].y, midX, midY)
    }
    // Finish with a line to the final point so the stroke actually ends
    // where the user's finger released.
    val last = points[points.size - 1]
    path.lineTo(last.x, last.y)
    return path
}

@Composable
fun DrawingCanvas(
    actions: List<DrawingAction>,
    panOffset: Offset,
    currentStrokePoints: List<Offset>,
    currentColor: Color,
    currentThickness: Float,
    isEraser: Boolean,
    isStampTool: Boolean,
    stampSize: Float,
    onDrawStart: (Offset) -> Unit,
    onDrawMove: (Offset) -> Unit,
    onDrawEnd: () -> Unit,
    onDrawCancel: () -> Unit,
    onTap: (Offset) -> Unit,
    onPanDelta: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentPanOffset by rememberUpdatedState(panOffset)
    val currentIsStampTool by rememberUpdatedState(isStampTool)
    val density = LocalDensity.current
    val stampMinDistance = remember(stampSize, density) {
        stampSize * 2 + with(density) { 10.dp.toPx() }
    }
    val currentStampMinDistance by rememberUpdatedState(stampMinDistance)
    val haptic = LocalHapticFeedback.current
    val currentHaptic by rememberUpdatedState(haptic)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val downEvent = awaitPointerEvent()
                        if (downEvent.type != PointerEventType.Press) continue

                        val firstDown = downEvent.changes.firstOrNull { it.pressed } ?: continue
                        var maxPointerCount = downEvent.changes.count { it.pressed }
                        var isPanning = false
                        var hasMoved = false
                        val startScreenPos = firstDown.position
                        val startWorldPos = Offset(
                            startScreenPos.x - currentPanOffset.x,
                            startScreenPos.y - currentPanOffset.y
                        )

                        var lastStampPos: Offset? = null

                        if (maxPointerCount < 3) {
                            if (currentIsStampTool) {
                                onTap(startWorldPos)
                                // Tactile "thunk" when a stamp lands — toddlers love it.
                                currentHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                lastStampPos = startWorldPos
                            } else {
                                onDrawStart(startWorldPos)
                            }
                        }

                        var lastPanCentroid = if (maxPointerCount >= 3) {
                            isPanning = true
                            onDrawCancel()
                            val activeChanges = downEvent.changes.filter { it.pressed }
                            Offset(
                                activeChanges.map { it.position.x }.average().toFloat(),
                                activeChanges.map { it.position.y }.average().toFloat()
                            )
                        } else {
                            Offset.Zero
                        }

                        firstDown.consume()

                        var gestureEnded = false
                        while (!gestureEnded) {
                            val event = awaitPointerEvent()
                            val activeChanges = event.changes.filter { it.pressed }
                            val currentPointerCount = activeChanges.size
                            maxPointerCount = maxOf(maxPointerCount, currentPointerCount)

                            if (maxPointerCount >= 3 && !isPanning) {
                                isPanning = true
                                onDrawCancel()
                                if (currentPointerCount >= 2) {
                                    lastPanCentroid = Offset(
                                        activeChanges.map { it.position.x }.average().toFloat(),
                                        activeChanges.map { it.position.y }.average().toFloat()
                                    )
                                }
                            }

                            when {
                                currentPointerCount == 0 -> {
                                    if (!isPanning && !currentIsStampTool) {
                                        if (!hasMoved) {
                                            onDrawCancel()
                                            onTap(startWorldPos)
                                        } else {
                                            onDrawEnd()
                                        }
                                    }
                                    gestureEnded = true
                                }
                                isPanning && currentPointerCount >= 2 -> {
                                    val centroid = Offset(
                                        activeChanges.map { it.position.x }.average().toFloat(),
                                        activeChanges.map { it.position.y }.average().toFloat()
                                    )
                                    if (lastPanCentroid != Offset.Zero) {
                                        onPanDelta(centroid - lastPanCentroid)
                                    }
                                    lastPanCentroid = centroid
                                }
                                !isPanning && currentPointerCount >= 1 -> {
                                    val pos = activeChanges.first().position
                                    val worldPos = Offset(
                                        pos.x - currentPanOffset.x,
                                        pos.y - currentPanOffset.y
                                    )
                                    if (currentIsStampTool) {
                                        val last = lastStampPos
                                        if (last != null) {
                                            val distance = (worldPos - last).getDistance()
                                            if (distance >= currentStampMinDistance) {
                                                onTap(worldPos)
                                                // Soft tick for each sprinkled stamp during drag.
                                                currentHaptic.performHapticFeedback(
                                                    HapticFeedbackType.TextHandleMove
                                                )
                                                lastStampPos = worldPos
                                            }
                                        }
                                    } else {
                                        val dx = pos.x - startScreenPos.x
                                        val dy = pos.y - startScreenPos.y
                                        if (dx * dx + dy * dy > 64f) {
                                            hasMoved = true
                                        }
                                        onDrawMove(worldPos)
                                    }
                                }
                            }
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // White background
        drawRect(Color.White)

        // Viewport rect in world coordinates for culling
        val viewportRect = Rect(
            left = -panOffset.x,
            top = -panOffset.y,
            right = -panOffset.x + canvasWidth,
            bottom = -panOffset.y + canvasHeight
        )

        translate(left = panOffset.x, top = panOffset.y) {
            // Draw committed actions (only those in viewport)
            for (action in actions) {
                if (!action.bounds.overlaps(viewportRect)) continue

                when (action) {
                    is DrawingAction.Stroke -> {
                        if (action.points.size >= 2) {
                            drawPath(
                                path = smoothedStrokePath(action.points),
                                color = if (action.isEraser) Color.White else Color(action.color),
                                style = Stroke(
                                    width = action.thickness,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        } else if (action.points.size == 1) {
                            // Single dot
                            drawCircle(
                                color = if (action.isEraser) Color.White else Color(action.color),
                                radius = action.thickness / 2f,
                                center = action.points[0]
                            )
                        }
                    }
                    is DrawingAction.Stamp -> {
                        drawStamp(
                            center = action.center,
                            stampType = action.stampType,
                            color = Color(action.color),
                            size = action.size
                        )
                    }
                }
            }

            // Draw in-progress stroke overlay
            if (currentStrokePoints.size >= 2) {
                drawPath(
                    path = smoothedStrokePath(currentStrokePoints),
                    color = if (isEraser) Color.White else currentColor,
                    style = Stroke(
                        width = currentThickness,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            } else if (currentStrokePoints.size == 1) {
                drawCircle(
                    color = if (isEraser) Color.White else currentColor,
                    radius = currentThickness / 2f,
                    center = currentStrokePoints[0]
                )
            }
        }
    }
}
