package yuku.gambaraja.kokrepot.ui.toolbar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import yuku.gambaraja.kokrepot.model.StampType
import yuku.gambaraja.kokrepot.model.Tool
import yuku.gambaraja.kokrepot.stamp.drawStamp

val thicknesses = listOf(4f, 8f, 14f, 22f, 32f)

private val stampTools = listOf(
    Tool.STAMP_HEART to StampType.HEART,
    Tool.STAMP_STAR to StampType.STAR,
    Tool.STAMP_SPIRAL to StampType.SPIRAL,
    Tool.STAMP_SMILEY to StampType.SMILEY,
    Tool.STAMP_SQUARE to StampType.SQUARE,
)

@Composable
fun RightToolbar(
    selectedThickness: Float,
    selectedTool: Tool,
    selectedColor: Color,
    isEraserSelected: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onThicknessSelected: (Float) -> Unit,
    onToolSelected: (Tool) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isStampMode = selectedTool.isStamp
    val iconColor = if (isEraserSelected) Color.White else selectedColor
    val iconBorderColor = borderForToolColor(iconColor)
    val dividerColor = Color.Black.copy(alpha = 0.15f)

    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(56.dp)
            .background(ToolbarBackground)
            .systemBarsPadding()
            .verticalScroll(scrollState)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        thicknesses.forEach { thickness ->
            val isSelected = thickness == selectedThickness && !isStampMode
            AnimatedToolButton(
                isSelected = isSelected,
                onClick = { onThicknessSelected(thickness) }
            ) {
                val dotSize = thickness.coerceAtMost(28f).dp
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(iconColor, CircleShape)
                        .border(1.dp, iconBorderColor, CircleShape)
                )
            }
        }

        if (!isEraserSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 8.dp),
                color = dividerColor
            )
            Spacer(modifier = Modifier.height(4.dp))

            stampTools.forEach { (tool, stampType) ->
                StampToolButton(tool, stampType, selectedTool, iconColor, iconBorderColor, onToolSelected)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp),
            color = dividerColor
        )
        Spacer(modifier = Modifier.height(4.dp))

        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onUndo()
            },
            enabled = canUndo,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Undo,
                contentDescription = "Undo",
                tint = if (canUndo) Color.Black else Color.Black.copy(alpha = 0.3f)
            )
        }
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onRedo()
            },
            enabled = canRedo,
        ) {
            if (canRedo) {
                Icon(
                    Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo",
                    tint = Color.Black
                )
            }
        }
    }
}

@Composable
private fun StampToolButton(
    tool: Tool,
    stampType: StampType,
    selectedTool: Tool,
    iconColor: Color,
    borderColor: Color,
    onToolSelected: (Tool) -> Unit,
) {
    AnimatedToolButton(
        isSelected = selectedTool == tool,
        onClick = { onToolSelected(tool) }
    ) {
        Canvas(modifier = Modifier.size(26.dp)) {
            drawStamp(
                center = Offset(size.width / 2, size.height / 2),
                stampType = stampType,
                color = iconColor,
                size = size.minDimension / 2.5f,
                borderColor = borderColor,
            )
        }
    }
}
