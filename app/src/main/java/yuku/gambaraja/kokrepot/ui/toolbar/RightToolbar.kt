package yuku.gambaraja.kokrepot.ui.toolbar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import yuku.gambaraja.kokrepot.model.StampType
import yuku.gambaraja.kokrepot.model.Tool
import yuku.gambaraja.kokrepot.stamp.drawStamp

val thicknesses = listOf(4f, 8f, 14f, 22f, 32f)

fun toolbarBackgroundColor(selectedColor: Color): Color {
    val luminance = 0.2126f * selectedColor.red + 0.7152f * selectedColor.green + 0.0722f * selectedColor.blue
    return if (luminance > 0.5f) Color(0xFF3A3A3A) else Color(0xFFF0F0F0)
}

fun contrastingColor(color: Color): Color {
    val luminance = 0.2126f * color.red + 0.7152f * color.green + 0.0722f * color.blue
    return if (luminance > 0.5f) Color.White else Color.Black
}

@Composable
fun RightToolbar(
    selectedThickness: Float,
    selectedTool: Tool,
    selectedColor: Color,
    canUndo: Boolean,
    canRedo: Boolean,
    onThicknessSelected: (Float) -> Unit,
    onToolSelected: (Tool) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val toolbarBg = toolbarBackgroundColor(selectedColor)
    val isStampMode = selectedTool.isStamp
    val contentColor = contrastingColor(toolbarBg)
    val dividerColor = contentColor.copy(alpha = 0.2f)

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(56.dp)
            .background(toolbarBg)
            .systemBarsPadding()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Thickness buttons — unselected when a stamp tool is active
        thicknesses.forEach { thickness ->
            val isSelected = thickness == selectedThickness && !isStampMode
            AnimatedToolButton(
                isSelected = isSelected,
                selectedColor = selectedColor,
                onClick = { onThicknessSelected(thickness) }
            ) { iconColor ->
                Box(
                    modifier = Modifier
                        .size((thickness.coerceAtMost(28f)).dp)
                        .clip(CircleShape)
                        .background(iconColor, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp),
            color = dividerColor
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Stamp buttons — use actual stamp rendering from v1.0.2
        AnimatedToolButton(
            isSelected = selectedTool == Tool.STAMP_HEART,
            selectedColor = selectedColor,
            onClick = { onToolSelected(Tool.STAMP_HEART) }
        ) { iconColor ->
            Canvas(modifier = Modifier.size(24.dp)) {
                drawStamp(Offset(size.width / 2, size.height / 2), StampType.HEART, iconColor, size.minDimension / 2.5f)
            }
        }
        AnimatedToolButton(
            isSelected = selectedTool == Tool.STAMP_STAR,
            selectedColor = selectedColor,
            onClick = { onToolSelected(Tool.STAMP_STAR) }
        ) { iconColor ->
            Canvas(modifier = Modifier.size(24.dp)) {
                drawStamp(Offset(size.width / 2, size.height / 2), StampType.STAR, iconColor, size.minDimension / 2.5f)
            }
        }
        AnimatedToolButton(
            isSelected = selectedTool == Tool.STAMP_SPIRAL,
            selectedColor = selectedColor,
            onClick = { onToolSelected(Tool.STAMP_SPIRAL) }
        ) { iconColor ->
            Canvas(modifier = Modifier.size(24.dp)) {
                drawStamp(Offset(size.width / 2, size.height / 2), StampType.SPIRAL, iconColor, size.minDimension / 2.5f)
            }
        }
        AnimatedToolButton(
            isSelected = selectedTool == Tool.STAMP_SMILEY,
            selectedColor = selectedColor,
            onClick = { onToolSelected(Tool.STAMP_SMILEY) }
        ) { iconColor ->
            Canvas(modifier = Modifier.size(24.dp)) {
                drawStamp(Offset(size.width / 2, size.height / 2), StampType.SMILEY, iconColor, size.minDimension / 2.5f)
            }
        }
        AnimatedToolButton(
            isSelected = selectedTool == Tool.STAMP_SQUARE,
            selectedColor = selectedColor,
            onClick = { onToolSelected(Tool.STAMP_SQUARE) }
        ) { iconColor ->
            Canvas(modifier = Modifier.size(24.dp)) {
                drawStamp(Offset(size.width / 2, size.height / 2), StampType.SQUARE, iconColor, size.minDimension / 2.5f)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp),
            color = dividerColor
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Undo
        IconButton(onClick = onUndo, enabled = canUndo) {
            Icon(
                Icons.AutoMirrored.Filled.Undo,
                contentDescription = "Undo",
                tint = if (canUndo) contentColor else contentColor.copy(alpha = 0.3f)
            )
        }
        // Redo
        IconButton(onClick = onRedo, enabled = canRedo) {
            Icon(
                Icons.AutoMirrored.Filled.Redo,
                contentDescription = "Redo",
                tint = if (canRedo) contentColor else contentColor.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun AnimatedToolButton(
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    content: @Composable (Color) -> Unit
) {
    val blinkAlpha = remember { Animatable(1f) }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            blinkAlpha.snapTo(1f)
            repeat(5) {
                blinkAlpha.animateTo(0.2f, tween(100))
                blinkAlpha.animateTo(1f, tween(100))
            }
        }
    }

    val bgColor = if (isSelected) selectedColor.copy(alpha = blinkAlpha.value) else Color.Transparent
    val iconColor = if (isSelected) contrastingColor(selectedColor) else selectedColor

    Box(
        modifier = Modifier
            .size(40.dp)
            .scale(if (isSelected) 1.1f else 1f)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content(iconColor)
    }
}
