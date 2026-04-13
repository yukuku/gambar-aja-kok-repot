package yuku.gambaraja.kokrepot.ui.toolbar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
            .background(ToolbarBackground)
            .systemBarsPadding()
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
                AnimatedToolButton(
                    isSelected = isSelected,
                    onClick = { onColorSelected(color) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(color, CircleShape)
                            .border(1.dp, borderForToolColor(color), CircleShape)
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            color = Color.Black.copy(alpha = 0.15f)
        )

        // Eraser button
        AnimatedToolButton(
            isSelected = isEraserSelected,
            onClick = onEraserSelected
        ) {
            EraserIcon(modifier = Modifier.size(30.dp))
        }

        Box(modifier = Modifier.padding(bottom = 4.dp))
    }
}

@Composable
private fun EraserIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val rectLeft = w * 0.1f
        val rectRight = w * 0.9f
        val rectTop = h * 0.28f
        val rectBottom = h * 0.78f
        val split = rectTop + (rectBottom - rectTop) * 0.58f
        val cornerR = (rectRight - rectLeft) * 0.18f

        // Pink top body (classic school-eraser look) with top-rounded corners.
        val topPath = Path().apply {
            addRoundRect(
                RoundRect(
                    Rect(rectLeft, rectTop, rectRight, split),
                    topLeft = CornerRadius(cornerR, cornerR),
                    topRight = CornerRadius(cornerR, cornerR),
                    bottomLeft = CornerRadius.Zero,
                    bottomRight = CornerRadius.Zero
                )
            )
        }
        drawPath(topPath, Color(0xFFF06292))

        // Blue bottom band with bottom-rounded corners.
        val bottomPath = Path().apply {
            addRoundRect(
                RoundRect(
                    Rect(rectLeft, split, rectRight, rectBottom),
                    topLeft = CornerRadius.Zero,
                    topRight = CornerRadius.Zero,
                    bottomLeft = CornerRadius(cornerR, cornerR),
                    bottomRight = CornerRadius(cornerR, cornerR)
                )
            )
        }
        drawPath(bottomPath, Color(0xFF64B5F6))

        // Outline
        drawRoundRect(
            color = Color(0xFF424242),
            topLeft = Offset(rectLeft, rectTop),
            size = Size(rectRight - rectLeft, rectBottom - rectTop),
            cornerRadius = CornerRadius(cornerR, cornerR),
            style = Stroke(width = 1.5f)
        )
    }
}
