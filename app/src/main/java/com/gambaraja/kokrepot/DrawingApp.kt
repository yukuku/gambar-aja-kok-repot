package com.gambaraja.kokrepot

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gambaraja.kokrepot.model.Tool
import com.gambaraja.kokrepot.ui.canvas.DrawingCanvas
import com.gambaraja.kokrepot.ui.toolbar.LeftToolbar
import com.gambaraja.kokrepot.ui.toolbar.RightToolbar

@Composable
fun DrawingApp(viewModel: DrawingViewModel = viewModel()) {
    // Read actionsVersion to subscribe to action list changes
    val version = viewModel.actionsVersion

    Row(modifier = Modifier.fillMaxSize()) {
        LeftToolbar(
            selectedColor = viewModel.selectedColor,
            isEraserSelected = viewModel.selectedTool == Tool.ERASER,
            onColorSelected = { viewModel.selectColor(it) },
            onEraserSelected = { viewModel.selectTool(Tool.ERASER) }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            DrawingCanvas(
                actions = viewModel.actions,
                panOffset = viewModel.panOffset,
                currentStrokePoints = viewModel.currentStrokePoints,
                currentColor = if (viewModel.selectedTool == Tool.ERASER) Color.White else viewModel.selectedColor,
                currentThickness = viewModel.selectedThickness,
                isEraser = viewModel.selectedTool == Tool.ERASER,
                onDrawStart = { viewModel.onDrawStart(it) },
                onDrawMove = { viewModel.onDrawMove(it) },
                onDrawEnd = { viewModel.onDrawEnd() },
                onDrawCancel = { viewModel.onDrawCancel() },
                onTap = { viewModel.onTap(it) },
                onPanDelta = { viewModel.onPanDelta(it) }
            )
        }

        RightToolbar(
            selectedThickness = viewModel.selectedThickness,
            selectedTool = viewModel.selectedTool,
            canUndo = viewModel.canUndo,
            canRedo = viewModel.canRedo,
            onThicknessSelected = { viewModel.selectThickness(it) },
            onToolSelected = { viewModel.selectTool(it) },
            onUndo = { viewModel.undo() },
            onRedo = { viewModel.redo() }
        )
    }
}
