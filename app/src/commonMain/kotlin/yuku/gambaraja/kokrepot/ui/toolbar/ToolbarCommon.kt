package yuku.gambaraja.kokrepot.ui.toolbar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Fixed, non-changing toolbar background. */
val ToolbarBackground: Color = Color(0xFFF0F0F0)

/** The dark-grey used by the selection indicator. */
val SelectionIndicatorColor: Color = Color(0xFF555555)

/** Light-grey border used when the tool color is dark. */
val BorderForDarkTool: Color = Color(0xFFBDBDBD)

/** Dark-grey border used when the tool color is light. */
val BorderForLightTool: Color = Color(0xFF616161)

/** Return a 1dp border color that keeps the given tool color visible on the toolbar. */
fun borderForToolColor(toolColor: Color): Color {
    val luminance = 0.2126f * toolColor.red + 0.7152f * toolColor.green + 0.0722f * toolColor.blue
    return if (luminance > 0.5f) BorderForLightTool else BorderForDarkTool
}

@Composable
fun AnimatedToolButton(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    onPressedChange: ((Boolean) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val blinkAlpha = remember { Animatable(if (isSelected) 1f else 0f) }
    var firstPass by remember { mutableStateOf(true) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(isSelected) {
        if (firstPass) {
            firstPass = false
            blinkAlpha.snapTo(if (isSelected) 1f else 0f)
            return@LaunchedEffect
        }
        if (isSelected) {
            blinkAlpha.snapTo(1f)
            repeat(5) {
                blinkAlpha.animateTo(0.2f, tween(100))
                blinkAlpha.animateTo(1f, tween(100))
            }
        } else {
            blinkAlpha.snapTo(0f)
        }
    }

    // A shared interaction source lets callers that care about press state
    // observe it without interfering with `clickable`. Used for the secret
    // two-finger combo that reveals the settings cog.
    val interactionSource = remember { MutableInteractionSource() }
    if (onPressedChange != null) {
        val isPressed by interactionSource.collectIsPressedAsState()
        LaunchedEffect(isPressed) { onPressedChange(isPressed) }
    }

    val shape = RoundedCornerShape(8.dp)
    val indicator = SelectionIndicatorColor.copy(alpha = blinkAlpha.value)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(indicator, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
