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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

val toddlerColors = listOf(
    Color(0xFFFF0000),
    Color(0xFFFF8C00),
    Color(0xFFFFD700),
    Color(0xFF32CD32),
    Color(0xFF008000),
    Color(0xFF00BFFF),
    Color(0xFF0000FF),
    Color(0xFF8A2BE2),
    Color(0xFFFF69B4),
    Color(0xFF8B4513),
    Color(0xFF000000),
    Color(0xFF808080),
)

/** The color whose button press is part of the hidden settings gesture. */
val SecretGestureColor: Color = Color(0xFF808080)

@Composable
fun LeftToolbar(
    selectedColor: Color,
    isEraserSelected: Boolean,
    onColorSelected: (Color) -> Unit,
    onEraserSelected: () -> Unit,
    modifier: Modifier = Modifier,
    onSecretColorPressedChange: ((Boolean) -> Unit)? = null,
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
                    onClick = { onColorSelected(color) },
                    onPressedChange = if (color == SecretGestureColor) onSecretColorPressedChange else null,
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
        val cx = w / 2f
        val cy = h / 2f

        val outlineColor = Color(0xFF2F2F3A)
        val redColor = Color(0xFFE57373)
        val yellowColor = Color(0xFFFFE082)
        val strokePx = (w * 0.055f).coerceAtLeast(1.5f)

        rotate(degrees = -35f, pivot = Offset(cx, cy)) {
            val bodyW = w * 0.85f
            val bodyH = h * 0.32f
            val left = cx - bodyW / 2f
            val right = cx + bodyW / 2f
            val top = cy - bodyH / 2f
            val bottom = cy + bodyH / 2f
            val corner = bodyH * 0.22f

            val capRight = left + bodyW * 0.30f
            val stripeTop = top + bodyH * 0.36f
            val stripeBottom = bottom - bodyH * 0.36f

            val capPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        Rect(left, top, capRight, bottom),
                        topLeft = CornerRadius(corner, corner),
                        bottomLeft = CornerRadius(corner, corner),
                        topRight = CornerRadius.Zero,
                        bottomRight = CornerRadius.Zero,
                    )
                )
            }
            drawPath(capPath, Color.White)

            val bodyPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        Rect(capRight, top, right, bottom),
                        topLeft = CornerRadius.Zero,
                        bottomLeft = CornerRadius.Zero,
                        topRight = CornerRadius(corner, corner),
                        bottomRight = CornerRadius(corner, corner),
                    )
                )
            }
            drawPath(bodyPath, redColor)

            drawRect(
                color = yellowColor,
                topLeft = Offset(capRight, stripeTop),
                size = Size(right - capRight, stripeBottom - stripeTop),
            )

            drawRoundRect(
                color = outlineColor,
                topLeft = Offset(left, top),
                size = Size(bodyW, bodyH),
                cornerRadius = CornerRadius(corner, corner),
                style = Stroke(width = strokePx),
            )
            drawLine(
                color = outlineColor,
                start = Offset(capRight, top),
                end = Offset(capRight, bottom),
                strokeWidth = strokePx,
            )
            drawLine(
                color = outlineColor,
                start = Offset(capRight, stripeTop),
                end = Offset(right, stripeTop),
                strokeWidth = strokePx,
            )
            drawLine(
                color = outlineColor,
                start = Offset(capRight, stripeBottom),
                end = Offset(right, stripeBottom),
                strokeWidth = strokePx,
            )
        }
    }
}
