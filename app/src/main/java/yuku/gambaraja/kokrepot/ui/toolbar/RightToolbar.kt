package yuku.gambaraja.kokrepot.ui.toolbar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import yuku.gambaraja.kokrepot.model.StampType
import yuku.gambaraja.kokrepot.model.Tool
import yuku.gambaraja.kokrepot.stamp.drawStamp

val thicknesses = listOf(4f, 8f, 14f, 22f, 32f)

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
    // When the eraser is active, thickness/stamp icons render white so they
    // read as "the eraser paints white".
    val iconColor = if (isEraserSelected) Color.White else selectedColor
    val iconBorderColor = borderForToolColor(iconColor)
    val dividerColor = Color.Black.copy(alpha = 0.15f)

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(56.dp)
            .background(ToolbarBackground)
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

        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp),
            color = dividerColor
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Stamp buttons
        StampToolButton(Tool.STAMP_HEART, StampType.HEART, selectedTool, iconColor, iconBorderColor, onToolSelected)
        StampToolButton(Tool.STAMP_STAR, StampType.STAR, selectedTool, iconColor, iconBorderColor, onToolSelected)
        StampToolButton(Tool.STAMP_SPIRAL, StampType.SPIRAL, selectedTool, iconColor, iconBorderColor, onToolSelected)
        StampToolButton(Tool.STAMP_SMILEY, StampType.SMILEY, selectedTool, iconColor, iconBorderColor, onToolSelected)
        StampToolButton(Tool.STAMP_SQUARE, StampType.SQUARE, selectedTool, iconColor, iconBorderColor, onToolSelected)

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp),
            color = dividerColor
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Undo — always black when enabled.
        IconButton(onClick = onUndo, enabled = canUndo) {
            Icon(
                Icons.AutoMirrored.Filled.Undo,
                contentDescription = "Undo",
                tint = if (canUndo) Color.Black else Color.Black.copy(alpha = 0.3f)
            )
        }
        // Redo — if disabled, hide the icon entirely (keep the slot so layout is stable).
        IconButton(onClick = onRedo, enabled = canRedo) {
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
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, borderColor, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(24.dp)) {
                drawStamp(
                    Offset(size.width / 2, size.height / 2),
                    stampType,
                    iconColor,
                    size.minDimension / 2.5f
                )
            }
        }
    }
}
