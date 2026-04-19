package yuku.gambaraja.kokrepot.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
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
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import yuku.gambaraja.kokrepot.model.DrawingAction
import yuku.gambaraja.kokrepot.model.StampEffect
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
    stampEffects: List<StampEffect>,
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

    // Drives per-frame recomposition while any stamp effect is active. The value
    // itself is unused — reading it in the draw block creates the snapshot
    // dependency that forces a redraw each animation frame. When the effects
    // list empties, the keyed LaunchedEffect cancels and the ticker stops.
    var animationFrameTick by remember { mutableStateOf(0L) }
    val hasEffects = stampEffects.isNotEmpty()
    LaunchedEffect(hasEffects) {
        if (hasEffects) {
            while (true) {
                withFrameNanos { animationFrameTick = it }
            }
        }
    }

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

        drawRect(Color.White)

        val viewportRect = Rect(
            left = -panOffset.x,
            top = -panOffset.y,
            right = -panOffset.x + canvasWidth,
            bottom = -panOffset.y + canvasHeight
        )

        // Reading the tick here gives the draw block a snapshot dependency on
        // the frame ticker, so it redraws every frame while effects run.
        @Suppress("UNUSED_VARIABLE")
        val tick = animationFrameTick

        val animatingStamps: Set<DrawingAction.Stamp> =
            if (stampEffects.isEmpty()) emptySet() else stampEffects.mapTo(HashSet()) { it.stamp }

        translate(left = panOffset.x, top = panOffset.y) {
            for (action in actions) {
                if (!action.bounds.overlaps(viewportRect)) continue
                if (action is DrawingAction.Stamp && action in animatingStamps) continue

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
                }
            }

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

            for (effect in stampEffects) {
                drawStampEffect(effect)
            }
        }
    }
}

private const val STAMP_BOUNCE_DURATION_MS = 350f
private const val PARTICLE_DURATION_MS = 550f

private fun easeOutCubic(t: Float): Float {
    val x = 1f - t.coerceIn(0f, 1f)
    return 1f - x * x * x
}

/**
 * Scale curve for the stamp itself: overshoots past 1.0 then settles.
 * - 0..0.55: grows from 0.35 to 1.22 (ease-out)
 * - 0.55..1.0: settles from 1.22 back to 1.0 (ease-out)
 */
private fun stampBounceScale(progress: Float): Float {
    val p = progress.coerceIn(0f, 1f)
    return if (p < 0.55f) {
        0.35f + (1.22f - 0.35f) * easeOutCubic(p / 0.55f)
    } else {
        1.22f - (1.22f - 1.0f) * easeOutCubic((p - 0.55f) / 0.45f)
    }
}

private fun DrawScope.drawStampEffect(effect: StampEffect) {
    val elapsedMs = effect.startMark.elapsedNow().inWholeMilliseconds.toFloat()
    val stamp = effect.stamp
    val center = stamp.center

    val stampProgress = (elapsedMs / STAMP_BOUNCE_DURATION_MS).coerceIn(0f, 1f)
    val scaledSize = stamp.size * stampBounceScale(stampProgress)
    drawStamp(
        center = center,
        stampType = stamp.stampType,
        color = Color(stamp.color),
        size = scaledSize,
    )

    val particleProgress = (elapsedMs / PARTICLE_DURATION_MS).coerceIn(0f, 1f)
    if (particleProgress >= 1f) return
    val outward = easeOutCubic(particleProgress)
    val alpha = (1f - particleProgress).coerceIn(0f, 1f)
    val shrink = 1f - particleProgress * 0.6f
    for (p in effect.particles) {
        val traveled = p.distance * outward
        val px = center.x + cos(p.angleRad) * traveled
        val py = center.y + sin(p.angleRad) * traveled
        val offset = Offset(px, py)
        val radius = p.baseRadius * shrink
        drawCircle(
            color = Color(p.color).copy(alpha = alpha * 0.85f),
            radius = radius,
            center = offset,
        )
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = radius * 0.45f,
            center = offset,
        )
    }
}
