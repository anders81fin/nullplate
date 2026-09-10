package io.github.anders81fin.nullplate.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

private const val SIZE = 240
private const val STROKE = 20f

private const val TRACK_COLOR = 0xFF3A342E.toInt()
private const val ARC_COLOR = 0xFFF0E9DE.toInt()

// The launcher icon rendered as a gauge: the same ring, in the same two
// colours, filling clockwise as the fast runs.
fun ringBitmap(progress: Float): Bitmap {
    val bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val inset = STROKE / 2f + 1f
    val bounds = RectF(inset, inset, SIZE - inset, SIZE - inset)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = STROKE
        strokeCap = Paint.Cap.ROUND
    }

    paint.color = TRACK_COLOR
    canvas.drawOval(bounds, paint)

    // A zero-length arc with a round cap still paints a dot, which would read
    // as progress that has not happened; an idle plate stays empty.
    if (progress > 0.001f) {
        paint.color = ARC_COLOR
        canvas.drawArc(bounds, -90f, 360f * progress.coerceIn(0f, 1f), false, paint)
    }

    return bitmap
}
