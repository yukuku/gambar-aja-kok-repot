package com.gambaraja.kokrepot.ui.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.gambaraja.kokrepot.model.Tool

val thicknesses = listOf(4f, 8f, 14f, 22f, 32f)

@Composable
fun RightToolbar(
    selectedThickness: Float,
    selectedTool: Tool,
    canUndo: Boolean,
    canRedo: Boolean,
    onThicknessSelected: (Float) -> Unit,
    onToolSelected: (Tool) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(56.dp)
            .background(Color(0xFFF0F0F0))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Thickness buttons
        thicknesses.forEach { thickness ->
            val isSelected = thickness == selectedThickness
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .scale(if (isSelected) 1.1f else 1f)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color(0xFFE3F2FD) else Color.Transparent,
                        CircleShape
                    )
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) Color(0xFF2196F3) else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onThicknessSelected(thickness) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size((thickness.coerceAtMost(28f)).dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp),
            color = Color.LightGray
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Stamp buttons
        StampButton(Icons.Filled.Favorite, "Heart", selectedTool == Tool.STAMP_HEART) {
            onToolSelected(Tool.STAMP_HEART)
        }
        StampButton(Icons.Filled.Star, "Star", selectedTool == Tool.STAMP_STAR) {
            onToolSelected(Tool.STAMP_STAR)
        }
        StampButton(Icons.Filled.AutoAwesome, "Spiral", selectedTool == Tool.STAMP_SPIRAL) {
            onToolSelected(Tool.STAMP_SPIRAL)
        }
        StampButton(Icons.Filled.SentimentSatisfiedAlt, "Smiley", selectedTool == Tool.STAMP_SMILEY) {
            onToolSelected(Tool.STAMP_SMILEY)
        }
        StampButton(Icons.Filled.CropSquare, "Square", selectedTool == Tool.STAMP_SQUARE) {
            onToolSelected(Tool.STAMP_SQUARE)
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp),
            color = Color.LightGray
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Undo
        IconButton(onClick = onUndo, enabled = canUndo) {
            Icon(
                Icons.AutoMirrored.Filled.Undo,
                contentDescription = "Undo",
                tint = if (canUndo) Color.DarkGray else Color.LightGray
            )
        }
        // Redo
        IconButton(onClick = onRedo, enabled = canRedo) {
            Icon(
                Icons.AutoMirrored.Filled.Redo,
                contentDescription = "Redo",
                tint = if (canRedo) Color.DarkGray else Color.LightGray
            )
        }
    }
}

@Composable
private fun StampButton(
    icon: ImageVector,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .scale(if (isSelected) 1.1f else 1f)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) Color(0xFFE3F2FD) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color(0xFF2196F3) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = if (isSelected) Color(0xFF2196F3) else Color.DarkGray,
            modifier = Modifier.size(24.dp)
        )
    }
}
