package com.gambaraja.kokrepot.ui.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val toddlerColors = listOf(
    Color(0xFFFF0000),   // Red
    Color(0xFFFF8C00),   // Orange
    Color(0xFFFFD700),   // Yellow
    Color(0xFF32CD32),   // Lime Green
    Color(0xFF008000),   // Green
    Color(0xFF00BFFF),   // Sky Blue
    Color(0xFF0000FF),   // Blue
    Color(0xFF8A2BE2),   // Purple
    Color(0xFFFF69B4),   // Hot Pink
    Color(0xFF8B4513),   // Brown
    Color(0xFF000000),   // Black
    Color(0xFF808080),   // Gray
)

@Composable
fun LeftToolbar(
    selectedColor: Color,
    isEraserSelected: Boolean,
    onColorSelected: (Color) -> Unit,
    onEraserSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(56.dp)
            .background(Color(0xFFF0F0F0))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(toddlerColors) { color ->
                val isSelected = color == selectedColor && !isEraserSelected
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .scale(if (isSelected) 1.15f else 1f)
                        .clip(CircleShape)
                        .background(color, CircleShape)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) Color.DarkGray else Color.LightGray,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(color) }
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            color = Color.LightGray
        )

        // Eraser button
        Box(
            modifier = Modifier
                .size(40.dp)
                .scale(if (isEraserSelected) 1.15f else 1f)
                .clip(CircleShape)
                .background(Color.White, CircleShape)
                .border(
                    width = if (isEraserSelected) 3.dp else 1.dp,
                    color = if (isEraserSelected) Color(0xFF2196F3) else Color.LightGray,
                    shape = CircleShape
                )
                .clickable { onEraserSelected() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.LayersClear,
                contentDescription = "Eraser",
                tint = Color.Gray,
                modifier = Modifier.size(22.dp)
            )
        }

        Box(modifier = Modifier.padding(bottom = 4.dp))
    }
}
