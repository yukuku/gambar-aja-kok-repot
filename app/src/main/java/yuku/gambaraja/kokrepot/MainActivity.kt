package yuku.gambaraja.kokrepot

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {

    private val viewModel: DrawingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep the screen on while the app is open — a toddler's drawing session
        // shouldn't be interrupted by the screen locking mid-stroke.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Sticky immersive fullscreen: hide status + nav bars so toddlers can't
        // accidentally pull notifications, navigate away, or tap system UI.
        // Bars transiently reappear on edge swipe for parents who need them.
        applyImmersiveMode()

        // Swallow the back gesture entirely. No "are you sure?" dialog (that would
        // violate the no-modals rule), no way for a toddler to accidentally exit
        // the app mid-masterpiece.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Intentionally do nothing.
            }
        })

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White
            ) {
                DrawingApp(viewModel)
            }
        }
    }

    override fun onPause() {
        // Flush the current drawing (including pan offset, which doesn't trigger
        // per-action auto-save) so that it survives the app being killed.
        viewModel.saveNow()
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Re-apply immersive mode in case the system showed the bars
            // (e.g., after an orientation change or a transient swipe).
            applyImmersiveMode()
        }
    }

    private fun applyImmersiveMode() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
