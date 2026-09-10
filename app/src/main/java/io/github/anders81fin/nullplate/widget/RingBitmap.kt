package io.github.anders81fin.nullplate.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

private const val SIZE = 240
private const val OUTER_RADIUS = 100f
private const val OUTER_STROKE = 19f

// The plate's rim. Same proportions as the launcher icon: close to the outer
// edge, and about half its weight — pushed further in, two circles of similar
// weight read as a target rather than a plate.
private const val RIM_RADIUS = OUTER_RADIUS * 0.717f
private const val RIM_STROKE = 8f

// Light enough to read as a plate against the widget's own dark background;
// a track only a shade off the background disappears at widget size.
private const val TRACK_COLOR = 0xFF544B41.toInt()
private const val ARC_COLOR = 0xFFF0E9DE.toInt()

// The launcher icon rendered as a gauge: the same ring, in the same two
// colours, filling clockwise as the fast runs.
fun ringBitmap(progress: Float): Bitmap {
    val bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val centre = SIZE / 2f
    val bounds = RectF(
        centre - OUTER_RADIUS,
        centre - OUTER_RADIUS,
        centre + OUTER_RADIUS,
        centre + OUTER_RADIUS,
    )

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = OUTER_STROKE
        strokeCap = Paint.Cap.ROUND
        color = TRACK_COLOR
    }

    canvas.drawOval(bounds, paint)

    paint.strokeWidth = RIM_STROKE
    canvas.drawCircle(centre, centre, RIM_RADIUS, paint)
    paint.strokeWidth = OUTER_STROKE

    // A zero-length arc with a round cap still paints a dot, which would read
    // as progress that has not happened; an idle plate stays empty.
    if (progress > 0.001f) {
        paint.color = ARC_COLOR
        canvas.drawArc(bounds, -90f, 360f * progress.coerceIn(0f, 1f), false, paint)
    }

    return bitmap
}
