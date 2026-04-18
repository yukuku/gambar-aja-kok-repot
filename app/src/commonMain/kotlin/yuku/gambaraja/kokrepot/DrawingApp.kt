package yuku.gambaraja.kokrepot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import yuku.gambaraja.kokrepot.model.Tool
import yuku.gambaraja.kokrepot.ui.canvas.DrawingCanvas
import yuku.gambaraja.kokrepot.ui.settings.SettingsDialog
import yuku.gambaraja.kokrepot.ui.toolbar.LeftToolbar
import yuku.gambaraja.kokrepot.ui.toolbar.RightToolbar

private const val COG_VISIBLE_MS = 1000L

@Composable
fun DrawingApp(viewModel: DrawingViewModel) {
    // Hidden settings gesture: both the grey color button and the smiley stamp
    // button must be physically held down at the same time. When that happens a
    // cog appears at top-center for one second; tapping it opens the dialog.
    // Designed so random toddler button-mashing cannot open it.
    var greyPressed by remember { mutableStateOf(false) }
    var smileyPressed by remember { mutableStateOf(false) }
    var cogVisible by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    val comboActive = greyPressed && smileyPressed
    LaunchedEffect(comboActive) {
        if (comboActive) {
            cogVisible = true
            delay(COG_VISIBLE_MS)
            cogVisible = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DrawingCanvas(
            actions = viewModel.actions,
            panOffset = viewModel.panOffset,
            currentStrokePoints = viewModel.currentStrokePoints,
            currentColor = if (viewModel.selectedTool == Tool.ERASER) Color.White else viewModel.selectedColor,
            currentThickness = viewModel.selectedThickness,
            isEraser = viewModel.selectedTool == Tool.ERASER,
            isStampTool = viewModel.selectedTool.isStamp,
            stampSize = DrawingViewModel.STAMP_FIXED_SIZE,
            onDrawStart = { viewModel.onDrawStart(it) },
            onDrawMove = { viewModel.onDrawMove(it) },
            onDrawEnd = { viewModel.onDrawEnd() },
            onDrawCancel = { viewModel.onDrawCancel() },
            onTap = { viewModel.onTap(it) },
            onPanDelta = { viewModel.onPanDelta(it) },
            modifier = Modifier.fillMaxSize()
        )

        LeftToolbar(
            selectedColor = viewModel.selectedColor,
            isEraserSelected = viewModel.selectedTool == Tool.ERASER,
            onColorSelected = { viewModel.selectColor(it) },
            onEraserSelected = { viewModel.selectTool(Tool.ERASER) },
            modifier = Modifier.align(Alignment.CenterStart),
            onSecretColorPressedChange = { greyPressed = it },
        )

        RightToolbar(
            selectedThickness = viewModel.selectedThickness,
            selectedTool = viewModel.selectedTool,
            selectedColor = viewModel.selectedColor,
            isEraserSelected = viewModel.selectedTool == Tool.ERASER,
            canUndo = viewModel.canUndo,
            canRedo = viewModel.canRedo,
            onThicknessSelected = { viewModel.selectThickness(it) },
            onToolSelected = { viewModel.selectTool(it) },
            onUndo = { viewModel.undo() },
            onRedo = { viewModel.redo() },
            modifier = Modifier.align(Alignment.CenterEnd),
            onSecretStampPressedChange = { smileyPressed = it },
        )

        if (cogVisible) {
            CogOverlay(
                onClick = {
                    cogVisible = false
                    showSettings = true
                },
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }

        if (showSettings) {
            SettingsDialog(onDismiss = { showSettings = false })
        }
    }
}

@Composable
private fun CogOverlay(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .systemBarsPadding()
            .padding(top = 56.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Filled.Settings,
            contentDescription = "Settings",
            tint = Color.White,
            modifier = Modifier.size(24.dp),
        )
    }
}
