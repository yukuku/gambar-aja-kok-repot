import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import yuku.gambaraja.kokrepot.DrawingApp
import yuku.gambaraja.kokrepot.DrawingStorage
import yuku.gambaraja.kokrepot.DrawingViewModel

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val storage = DrawingStorage()
    val viewModel = DrawingViewModel(storage)

    // Flush on tab close so the latest drawing + pan offset survive a refresh.
    window.addEventListener("beforeunload", { viewModel.saveNow() })

    ComposeViewport(document.body!!) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White,
        ) {
            DrawingApp(remember { viewModel })
        }
    }
}
