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
 * Binary on-disk persistence using the app's internal files directory. Format
 * is unchanged from previous versions, so existing users keep their drawings.
 *
 *   magic:    4 bytes "GAKR"
 *   version:  int
 *   panX, panY: float
 *   count:    int
 *   actions:  repeated
 *     type: byte (0 = stroke, 1 = stamp)
 *     stroke: color:int, thickness:float, isEraser:byte, pointCount:int, points:(float x, float y)*
 *     stamp: cx:float, cy:float, stampType:byte, color:int, size:float
 */
actual class DrawingStorage(private val context: Context) {

    private companion object {
        const val TAG = "DrawingStorage"
        const val FILE_NAME = "drawing.bin"
        const val MAGIC_0 = 'G'.code.toByte()
        const val MAGIC_1 = 'A'.code.toByte()
        const val MAGIC_2 = 'K'.code.toByte()
        const val MAGIC_3 = 'R'.code.toByte()
        const val FORMAT_VERSION = 1
        const val TYPE_STROKE: Byte = 0
        const val TYPE_STAMP: Byte = 1
    }

    private val file: File get() = File(context.filesDir, FILE_NAME)
    private val tempFile: File get() = File(context.filesDir, "$FILE_NAME.tmp")

    actual fun load(): DrawingSnapshot? {
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
                val stampTypes = StampType.entries
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

    actual fun save(snapshot: DrawingSnapshot) {
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
            if (!tmp.renameTo(file)) {
                tmp.copyTo(file, overwrite = true)
                tmp.delete()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save drawing", e)
            tmp.delete()
        }
    }
}
