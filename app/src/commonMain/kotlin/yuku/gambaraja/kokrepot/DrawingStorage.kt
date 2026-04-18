package yuku.gambaraja.kokrepot

import androidx.compose.ui.geometry.Offset
import yuku.gambaraja.kokrepot.model.DrawingAction

/**
 * Persisted drawing snapshot: the committed actions and the current pan offset.
 */
data class DrawingSnapshot(
    val actions: List<DrawingAction>,
    val panOffset: Offset,
)

/**
 * Binary persistence of the drawing so a toddler's masterpiece survives the app
 * being closed or backgrounded. Implementations are platform-specific:
 *   - Android uses the app's internal files directory.
 *   - Web uses localStorage.
 */
expect class DrawingStorage {
    fun load(): DrawingSnapshot?
    fun save(snapshot: DrawingSnapshot)
}
