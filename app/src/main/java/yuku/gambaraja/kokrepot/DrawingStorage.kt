package yuku.gambaraja.kokrepot

import android.content.Context
import android.util.Log
import androidx.compose.ui.geometry.Offset
import yuku.gambaraja.kokrepot.model.DrawingAction
import yuku.gambaraja.kokrepot.model.StampType
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File

/**
 * Persisted drawing snapshot: the committed actions and the current pan offset.
 */
data class DrawingSnapshot(
    val actions: List<DrawingAction>,
    val panOffset: Offset,
)

/**
 * Binary persistence of the drawing so a toddler's masterpiece survives the app
 * being closed, backgrounded, or killed. No dialogs, no save button — the drawing
 * is always implicitly saved and always restored on launch.
 *
 * File format (little-endian via DataOutputStream — big-endian actually, but it
 * doesn't matter as long as reader/writer match):
 *
 *   magic:    4 bytes "GAKR"
 *   version:  int
 *   panX:     float
 *   panY:     float
 *   count:    int   (number of actions)
 *   actions:  repeated
 *     type: byte (0 = stroke, 1 = stamp)
 *     stroke:
 *       color: int
 *       thickness: float
 *       isEraser: byte (0/1)
 *       pointCount: int
 *       points: pointCount * (float x, float y)
 *     stamp:
 *       cx: float, cy: float
 *       stampType: byte (ordinal)
 *       color: int
 *       size: float
 */
class DrawingStorage(private val context: Context) {

    companion object {
        private const val TAG = "DrawingStorage"
        private const val FILE_NAME = "drawing.bin"
        private const val MAGIC_0 = 'G'.code.toByte()
        private const val MAGIC_1 = 'A'.code.toByte()
        private const val MAGIC_2 = 'K'.code.toByte()
        private const val MAGIC_3 = 'R'.code.toByte()
        private const val FORMAT_VERSION = 1
        private const val TYPE_STROKE: Byte = 0
        private const val TYPE_STAMP: Byte = 1
    }

    private val file: File get() = File(context.filesDir, FILE_NAME)
    private val tempFile: File get() = File(context.filesDir, "$FILE_NAME.tmp")

    fun load(): DrawingSnapshot? {
        val f = file
        if (!f.exists()) return null
        return try {
            DataInputStream(f.inputStream().buffered()).use { input ->
                val m0 = input.readByte()
                val m1 = input.readByte()
                val m2 = input.readByte()
                val m3 = input.readByte()
                if (m0 != MAGIC_0 || m1 != MAGIC_1 || m2 != MAGIC_2 || m3 != MAGIC_3) {
                    Log.w(TAG, "Bad magic in saved drawing")
                    return@use null
                }
                val version = input.readInt()
                if (version != FORMAT_VERSION) {
                    Log.w(TAG, "Unknown saved drawing version: $version")
                    return@use null
                }
                val panX = input.readFloat()
                val panY = input.readFloat()
                val count = input.readInt()
                if (count < 0 || count > 1_000_000) {
                    Log.w(TAG, "Suspicious action count: $count")
                    return@use null
                }
                val actions = ArrayList<DrawingAction>(count)
                val stampTypes = StampType.values()
                for (i in 0 until count) {
                    when (val type = input.readByte()) {
                        TYPE_STROKE -> {
                            val color = input.readInt()
                            val thickness = input.readFloat()
                            val isEraser = input.readByte().toInt() != 0
                            val pointCount = input.readInt()
                            if (pointCount < 0 || pointCount > 10_000_000) {
                                Log.w(TAG, "Suspicious point count: $pointCount")
                                return@use null
                            }
                            val points = ArrayList<Offset>(pointCount)
                            for (p in 0 until pointCount) {
                                val x = input.readFloat()
                                val y = input.readFloat()
                                points.add(Offset(x, y))
                            }
                            actions.add(
                                DrawingAction.Stroke(
                                    points = points,
                                    color = color,
                                    thickness = thickness,
                                    isEraser = isEraser,
                                )
                            )
                        }
                        TYPE_STAMP -> {
                            val cx = input.readFloat()
                            val cy = input.readFloat()
                            val stampTypeIdx = input.readByte().toInt()
                            val color = input.readInt()
                            val size = input.readFloat()
                            if (stampTypeIdx < 0 || stampTypeIdx >= stampTypes.size) {
                                Log.w(TAG, "Unknown stamp type: $stampTypeIdx")
                                return@use null
                            }
                            actions.add(
                                DrawingAction.Stamp(
                                    center = Offset(cx, cy),
                                    stampType = stampTypes[stampTypeIdx],
                                    color = color,
                                    size = size,
                                )
                            )
                        }
                        else -> {
                            Log.w(TAG, "Unknown action type: $type")
                            return@use null
                        }
                    }
                }
                DrawingSnapshot(actions, Offset(panX, panY))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load drawing", e)
            null
        }
    }

    fun save(snapshot: DrawingSnapshot) {
        val tmp = tempFile
        try {
            DataOutputStream(tmp.outputStream().buffered()).use { out ->
                out.writeByte(MAGIC_0.toInt())
                out.writeByte(MAGIC_1.toInt())
                out.writeByte(MAGIC_2.toInt())
                out.writeByte(MAGIC_3.toInt())
                out.writeInt(FORMAT_VERSION)
                out.writeFloat(snapshot.panOffset.x)
                out.writeFloat(snapshot.panOffset.y)
                out.writeInt(snapshot.actions.size)
                for (action in snapshot.actions) {
                    when (action) {
                        is DrawingAction.Stroke -> {
                            out.writeByte(TYPE_STROKE.toInt())
                            out.writeInt(action.color)
                            out.writeFloat(action.thickness)
                            out.writeByte(if (action.isEraser) 1 else 0)
                            out.writeInt(action.points.size)
                            for (p in action.points) {
                                out.writeFloat(p.x)
                                out.writeFloat(p.y)
                            }
                        }
                        is DrawingAction.Stamp -> {
                            out.writeByte(TYPE_STAMP.toInt())
                            out.writeFloat(action.center.x)
                            out.writeFloat(action.center.y)
                            out.writeByte(action.stampType.ordinal)
                            out.writeInt(action.color)
                            out.writeFloat(action.size)
                        }
                    }
                }
                out.flush()
            }
            // Atomic-ish rename so a crash mid-write leaves the old file intact.
            if (!tmp.renameTo(file)) {
                // Fall back to copy-then-delete if rename fails across filesystems.
                tmp.copyTo(file, overwrite = true)
                tmp.delete()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save drawing", e)
            tmp.delete()
        }
    }
}
