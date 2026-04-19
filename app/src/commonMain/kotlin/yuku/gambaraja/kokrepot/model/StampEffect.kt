package yuku.gambaraja.kokrepot.model

import kotlin.time.TimeMark

/**
 * Ephemeral visual effect emitted when a stamp is placed. Not persisted.
 * The effect references the underlying [stamp] so the canvas can render the
 * bouncing scaled version in place of the action's default rendering while
 * the animation is active.
 */
data class StampEffect(
    val id: Long,
    val stamp: DrawingAction.Stamp,
    val particles: List<Particle>,
    val startMark: TimeMark,
)

data class Particle(
    val angleRad: Float,
    val distance: Float,
    val color: Int,
    val baseRadius: Float,
)
