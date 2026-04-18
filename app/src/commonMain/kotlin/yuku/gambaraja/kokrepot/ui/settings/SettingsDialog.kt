package yuku.gambaraja.kokrepot.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import yuku.gambaraja.kokrepot.BuildInfo

/**
 * A minimal, dismiss-on-tap-outside info dialog with the version, git hash,
 * and build timestamp. Shown only behind the hidden two-finger combo, so the
 * styling doesn't need to match the toddler-facing UI.
 */
@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 260.dp, max = 420.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Gambar Aja Kok Repot",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF222222),
                )
                Spacer(Modifier.height(4.dp))
                LabelValue("Version", "${BuildInfo.VERSION_NAME}.${BuildInfo.GIT_HASH}")
                LabelValue("Built", formatBuildTime(BuildInfo.BUILD_UNIX_SECONDS))
            }
        }
    }
}

@Composable
private fun LabelValue(label: String, value: String) {
    Column {
        Text(
            label,
            fontSize = 11.sp,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Medium,
        )
        Text(
            value,
            fontSize = 14.sp,
            color = Color(0xFF111111),
            fontFamily = FontFamily.Monospace,
        )
    }
}

/**
 * Format the build unix seconds into the viewer's local time with a "YYYY-MM-DD
 * HH:mm:ss ZONE" layout. Uses the runtime's current system time zone so the
 * value reflects wherever the page is being viewed from.
 */
private fun formatBuildTime(unixSeconds: Long): String {
    val tz = TimeZone.currentSystemDefault()
    val dt = Instant.fromEpochSeconds(unixSeconds).toLocalDateTime(tz)
    val date = "${dt.year}-${two(dt.monthNumber)}-${two(dt.dayOfMonth)}"
    val time = "${two(dt.hour)}:${two(dt.minute)}:${two(dt.second)}"
    return "$date $time ${tz.id}"
}

private fun two(n: Int): String = if (n < 10) "0$n" else n.toString()
