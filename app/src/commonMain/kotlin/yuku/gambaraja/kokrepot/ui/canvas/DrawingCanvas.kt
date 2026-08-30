package yuku.gambaraja.kokrepot.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import yuku.gambaraja.kokrepot.model.DrawingAction
import yuku.gambaraja.kokrepot.stamp.drawStamp

/**
 * Build a smoothed path through [points] using quadratic Bézier curves between
 * midpoints of adjacent samples.
 */
private fun smoothedStrokePath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points[0].x, points[0].y)
    if (points.size == 2) {
        path.lineTo(points[1].x, points[1].y)
        return path
    }
    for (i in 1 until points.size - 1) {
        val midX = (points[i].x + points[i + 1].x) / 2f
        val midY = (points[i].y + points[i + 1].y) / 2f
        path.quadraticTo(points[i].x, points[i].y, midX, midY)
    }
    val last = points[points.size - 1]
    path.lineTo(last.x, last.y)
    return path
}

private class PointerGestureState(
    val startScreenPos: Offset,
    var hasMoved: Boolean = false,
    var lastStampPos: Offset? = null,
    var committedFill: DrawingAction.Fill? = null,
)

private const val PAN_FINGER_COUNT = 3

/**
 * Draws a single committed [DrawingAction]. Shared between the on-screen render
 * loop and the offscreen rasterizer used by flood fill, so both see identical
 * pixels.
 */
internal fun DrawScope.drawAction(action: DrawingAction) {
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
        is DrawingAction.Fill -> {
            drawPath(path = action.path, color = Color(action.color))
        }
    }
}

@Composable
fun DrawingCanvas(
    actions: List<DrawingAction>,
    panOffset: Offset,
    currentStrokes: Map<Long, List<Offset>>,
    currentColor: Color,
    currentThickness: Float,
    isEraser: Boolean,
    isStampTool: Boolean,
    isFloodFillTool: Boolean,
    stampSize: Float,
    onDrawStart: (Long, Offset) -> Unit,
    onDrawMove: (Long, Offset) -> Unit,
    onDrawEnd: (Long) -> Unit,
    onDrawCancel: (Long) -> Unit,
    onDrawCancelAll: () -> Unit,
    onTap: (Offset) -> Unit,
    onPanDelta: (Offset) -> Unit,
    onFloodFill: (DrawingAction.Fill) -> Unit,
    onFloodFillCancel: (DrawingAction.Fill) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentPanOffset by rememberUpdatedState(panOffset)
    val currentIsStampTool by rememberUpdatedState(isStampTool)
    val currentIsFloodFillTool by rememberUpdatedState(isFloodFillTool)
    val currentActions by rememberUpdatedState(actions)
    val currentFillColor by rememberUpdatedState(currentColor)
    val currentOnFloodFill by rememberUpdatedState(onFloodFill)
    val currentOnFloodFillCancel by rememberUpdatedState(onFloodFillCancel)
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
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
            .onSizeChanged { canvasSize = it }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    val pointerStates = mutableMapOf<PointerId, PointerGestureState>()
                    // Latest known position of every pointer currently pressed,
                    // keyed by id — kept current from every event's `changes` for
                    // the whole gesture (not just while panning). Individual events
                    // aren't guaranteed to report every still-pressed pointer's
                    // status: when several fingers land within the same frame, one
                    // event can report only the finger(s) that changed in that
                    // specific tick. Counting fingers from a single event's
                    // `changes` alone can therefore undercount fingers that landed
                    // moments earlier — missing the pan threshold entirely, or
                    // (during an active pan) averaging in stale positions and
                    // making the centroid jump. Accumulating instead fixes both.
                    //
                    // Scoped to a single gesture: cleared whenever every pointer is
                    // up. That reset is driven by a fresh per-event count (below),
                    // never by this map's own size, so a release this map happens
                    // to miss can't leave it stuck non-empty and leak into a later,
                    // unrelated gesture.
                    val trackedPointerPositions = mutableMapOf<PointerId, Offset>()
                    var isPanning = false
                    var lastPanCentroid: Offset? = null

                    fun centroidOf(positions: Collection<Offset>) = Offset(
                        positions.map { it.x }.average().toFloat(),
                        positions.map { it.y }.average().toFloat()
                    )

                    while (true) {
                        val event = awaitPointerEvent()
                        val activeChanges = event.changes.filter { it.pressed }
                        val activeCount = activeChanges.size

                        for (change in event.changes) {
                            if (change.pressed) {
                                trackedPointerPositions[change.id] = change.position
                            } else {
                                trackedPointerPositions.remove(change.id)
                            }
                        }

                        // Switch to pan as soon as the threshold number of fingers
                        // is on the screen. Cancel any in-progress strokes — stamps
                        // already placed via onTap stay committed.
                        if (!isPanning && trackedPointerPositions.size >= PAN_FINGER_COUNT) {
                            isPanning = true
                            if (!currentIsStampTool) {
                                onDrawCancelAll()
                            }
                            for (state in pointerStates.values) {
                                state.committedFill?.let { currentOnFloodFillCancel(it) }
                            }
                            pointerStates.clear()
                            lastPanCentroid = centroidOf(trackedPointerPositions.values)
                        }

                        if (isPanning) {
                            if (trackedPointerPositions.size >= 2) {
                                val centroid = centroidOf(trackedPointerPositions.values)
                                lastPanCentroid?.let { onPanDelta(centroid - it) }
                                lastPanCentroid = centroid
                            } else {
                                // Fewer than 2 fingers — pause panning until either
                                // more fingers return or all are released.
                                lastPanCentroid = null
                            }
                        } else {
                            // Newly-pressed pointers: start a stroke or place a stamp.
                            for (change in event.changes) {
                                if (!change.previousPressed && change.pressed) {
                                    val worldPos = Offset(
                                        change.position.x - currentPanOffset.x,
                                        change.position.y - currentPanOffset.y
                                    )
                                    val state = PointerGestureState(change.position)
                                    pointerStates[change.id] = state
                                    if (currentIsStampTool) {
                                        onTap(worldPos)
                                        currentHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        state.lastStampPos = worldPos
                                    } else if (currentIsFloodFillTool) {
                                        val viewportWorldRect = Rect(
                                            left = -currentPanOffset.x,
                                            top = -currentPanOffset.y,
                                            right = -currentPanOffset.x + canvasSize.width,
                                            bottom = -currentPanOffset.y + canvasSize.height
                                        )
                                        val fill = computeFloodFill(
                                            actions = currentActions,
                                            viewportWorldRect = viewportWorldRect,
                                            startWorldPos = worldPos,
                                            fillColor = currentFillColor,
                                        )
                                        if (fill != null) {
                                            currentOnFloodFill(fill)
                                            currentHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            state.committedFill = fill
                                        }
                                    } else {
                                        onDrawStart(change.id.value, worldPos)
                                    }
                                }
                            }

                            // Active pointers: extend stroke or sprinkle stamps.
                            for (change in event.changes) {
                                if (!change.pressed || !change.previousPressed) continue
                                val state = pointerStates[change.id] ?: continue
                                val worldPos = Offset(
                                    change.position.x - currentPanOffset.x,
                                    change.position.y - currentPanOffset.y
                                )
                                if (currentIsStampTool) {
                                    val last = state.lastStampPos
                                    if (last != null) {
                                        val distance = (worldPos - last).getDistance()
                                        if (distance >= currentStampMinDistance) {
                                            onTap(worldPos)
                                            currentHaptic.performHapticFeedback(
                                                HapticFeedbackType.TextHandleMove
                                            )
                                            state.lastStampPos = worldPos
                                        }
                                    }
                                } else if (currentIsFloodFillTool) {
                                    // Flood fill fires once on touch-down; dragging
                                    // the same finger doesn't repeat it.
                                } else {
                                    val dx = change.position.x - state.startScreenPos.x
                                    val dy = change.position.y - state.startScreenPos.y
                                    if (dx * dx + dy * dy > 64f) {
                                        state.hasMoved = true
                                    }
                                    onDrawMove(change.id.value, worldPos)
                                }
                            }

                            // Released pointers: commit the stroke, or emit a dot
                            // if the finger never moved (brush/eraser only).
                            for (change in event.changes) {
                                if (!change.previousPressed || change.pressed) continue
                                val state = pointerStates.remove(change.id) ?: continue
                                if (!currentIsStampTool && !currentIsFloodFillTool) {
                                    if (!state.hasMoved) {
                                        onDrawCancel(change.id.value)
                                        val worldPos = Offset(
                                            state.startScreenPos.x - currentPanOffset.x,
                                            state.startScreenPos.y - currentPanOffset.y
                                        )
                                        onTap(worldPos)
                                    } else {
                                        onDrawEnd(change.id.value)
                                    }
                                }
                            }
                        }

                        // Once every finger is gone, reset for the next gesture.
                        if (activeCount == 0) {
                            pointerStates.clear()
                            trackedPointerPositions.clear()
                            isPanning = false
                            lastPanCentroid = null
                        }

                        event.changes.forEach { it.consume() }
                    }
                }
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        drawRect(Color.White)

        val viewportRect = Rect(
            left = -panOffset.x,
            top = -panOffset.y,
            right = -panOffset.x + canvasWidth,
            bottom = -panOffset.y + canvasHeight
        )

        translate(left = panOffset.x, top = panOffset.y) {
            for (action in actions) {
                if (!action.bounds.overlaps(viewportRect)) continue
                drawAction(action)
            }

            val liveColor = if (isEraser) Color.White else currentColor
            for (points in currentStrokes.values) {
                if (points.size >= 2) {
                    drawPath(
                        path = smoothedStrokePath(points),
                        color = liveColor,
                        style = Stroke(
                            width = currentThickness,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                } else if (points.size == 1) {
                    drawCircle(
                        color = liveColor,
                        radius = currentThickness / 2f,
                        center = points[0]
                    )
                }
            }
        }
    }
}
