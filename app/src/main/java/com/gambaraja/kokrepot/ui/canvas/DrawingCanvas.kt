package com.gambaraja.kokrepot.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import com.gambaraja.kokrepot.model.DrawingAction
import com.gambaraja.kokrepot.stamp.drawStamp

@Composable
fun DrawingCanvas(
    actions: List<DrawingAction>,
    panOffset: Offset,
    currentStrokePoints: List<Offset>,
    currentColor: Color,
    currentThickness: Float,
    isEraser: Boolean,
    onDrawStart: (Offset) -> Unit,
    onDrawMove: (Offset) -> Unit,
    onDrawEnd: () -> Unit,
    onDrawCancel: () -> Unit,
    onTap: (Offset) -> Unit,
    onPanDelta: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
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
                            startScreenPos.x - panOffset.x,
                            startScreenPos.y - panOffset.y
                        )

                        if (maxPointerCount < 3) {
                            onDrawStart(startWorldPos)
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
                                    if (!isPanning) {
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
                                        pos.x - panOffset.x,
                                        pos.y - panOffset.y
                                    )
                                    val dx = pos.x - startScreenPos.x
                                    val dy = pos.y - startScreenPos.y
                                    if (dx * dx + dy * dy > 64f) {
                                        hasMoved = true
                                    }
                                    onDrawMove(worldPos)
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
                            val path = Path().apply {
                                moveTo(action.points[0].x, action.points[0].y)
                                for (i in 1 until action.points.size) {
                                    lineTo(action.points[i].x, action.points[i].y)
                                }
                            }
                            drawPath(
                                path = path,
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
                val path = Path().apply {
                    moveTo(currentStrokePoints[0].x, currentStrokePoints[0].y)
                    for (i in 1 until currentStrokePoints.size) {
                        lineTo(currentStrokePoints[i].x, currentStrokePoints[i].y)
                    }
                }
                drawPath(
                    path = path,
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
