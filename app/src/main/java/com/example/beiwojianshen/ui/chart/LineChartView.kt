package com.example.beiwojianshen.ui.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 2f
        style = Paint.Style.STROKE
        color = 0x66000000
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 4f
        style = Paint.Style.STROKE
        color = 0xFF3F51B5.toInt()
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFF3F51B5.toInt()
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 28f
        color = 0x99000000.toInt()
    }

    private var labels: List<String> = emptyList()
    private var values: List<Double> = emptyList()
    private var pointsX: FloatArray = FloatArray(0)
    private var pointsY: FloatArray = FloatArray(0)
    private var selectedIndex: Int = -1
    private var onPointSelected: ((label: String, value: Double) -> Unit)? = null

    fun setSeries(labels: List<String>, values: List<Double>) {
        this.labels = labels
        this.values = values
        this.selectedIndex = -1
        invalidate()
    }

    fun setOnPointSelectedListener(listener: ((label: String, value: Double) -> Unit)?) {
        this.onPointSelected = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (values.isEmpty()) return super.onTouchEvent(event)
        if (event.action != MotionEvent.ACTION_DOWN) return super.onTouchEvent(event)
        if (pointsX.isEmpty() || pointsY.isEmpty()) return super.onTouchEvent(event)

        val hitRadius = 24f * resources.displayMetrics.density
        val hitRadius2 = hitRadius * hitRadius
        var best = -1
        var bestD2 = Float.MAX_VALUE
        for (i in pointsX.indices) {
            val dx = event.x - pointsX[i]
            val dy = event.y - pointsY[i]
            val d2 = dx * dx + dy * dy
            if (d2 < bestD2) {
                bestD2 = d2
                best = i
            }
        }
        if (best >= 0 && bestD2 <= hitRadius2) {
            selectedIndex = best
            invalidate()
            onPointSelected?.invoke(labels.getOrNull(best).orEmpty(), values[best])
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val count = values.size
        val w = width.toFloat()
        val h = height.toFloat()
        if (count == 0 || w <= 0f || h <= 0f) return

        val paddingLeft = 24f
        val paddingRight = 24f
        val paddingTop = 24f
        val paddingBottom = 36f

        val left = paddingLeft
        val top = paddingTop
        val right = w - paddingRight
        val bottom = h - paddingBottom

        canvas.drawRect(left, top, right, bottom, axisPaint)

        var minV = Double.POSITIVE_INFINITY
        var maxV = Double.NEGATIVE_INFINITY
        for (v in values) {
            minV = min(minV, v)
            maxV = max(maxV, v)
        }
        if (minV == Double.POSITIVE_INFINITY || maxV == Double.NEGATIVE_INFINITY) return

        val range = (maxV - minV).takeIf { it > 0 } ?: 1.0
        val stepX = if (count == 1) 0f else (right - left) / (count - 1)

        if (pointsX.size != count) pointsX = FloatArray(count)
        if (pointsY.size != count) pointsY = FloatArray(count)

        val path = Path()
        for (i in 0 until count) {
            val x = left + stepX * i
            val norm = (values[i] - minV) / range
            val y = bottom - (bottom - top) * norm.toFloat()
            pointsX[i] = x
            pointsY[i] = y
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        canvas.drawPath(path, linePaint)

        for (i in 0 until count) {
            val r = if (i == selectedIndex) 9f else 6f
            canvas.drawCircle(pointsX[i], pointsY[i], r, pointPaint)
        }

        val firstLabel = labels.firstOrNull().orEmpty()
        val lastLabel = labels.lastOrNull().orEmpty()
        canvas.drawText(firstLabel, left, h - 8f, textPaint)
        val lastWidth = textPaint.measureText(lastLabel)
        canvas.drawText(lastLabel, (right - lastWidth).coerceAtLeast(left), h - 8f, textPaint)
    }
}
