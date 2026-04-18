package yuku.gambaraja.kokrepot

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import yuku.gambaraja.kokrepot.model.Tool
import yuku.gambaraja.kokrepot.ui.canvas.DrawingCanvas
import yuku.gambaraja.kokrepot.ui.toolbar.LeftToolbar
import yuku.gambaraja.kokrepot.ui.toolbar.RightToolbar

@Composable
fun DrawingApp(viewModel: DrawingViewModel) {
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
            modifier = Modifier.align(Alignment.CenterStart)
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
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}
