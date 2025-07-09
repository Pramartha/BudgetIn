package com.example.budgetin

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.min

class CustomPieChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class PieSlice(
        val value: Float,
        val color: Int,
        val label: String
    )

    private val slices = mutableListOf<PieSlice>()
    private val paint = Paint()
    private val textPaint = Paint()
    private val rect = RectF()

    init {
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL

        textPaint.isAntiAlias = true
        textPaint.color = Color.WHITE
        textPaint.textSize = 24f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD
    }

    fun setData(data: List<PieSlice>) {
        slices.clear()
        slices.addAll(data)
        Log.d("CustomPieChart", "Data set with ${slices.size} slices")
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val padding = 20f
        val size = min(w - padding * 2, h - padding * 2)
        val left = (w - size) / 2
        val top = (h - size) / 2

        rect.set(left, top, left + size, top + size)

        Log.d("CustomPieChart", "Size changed: ${w}x${h}, rect: $rect")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        val size = min(width, height)
        setMeasuredDimension(size, size)

        Log.d("CustomPieChart", "Measured size: ${size}x${size}")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (slices.isEmpty()) {
            Log.d("CustomPieChart", "No data to draw")
            return
        }

        Log.d("CustomPieChart", "Drawing pie chart with ${slices.size} slices")

        val total = slices.sumOf { it.value.toDouble() }.toFloat()
        if (total <= 0) {
            Log.d("CustomPieChart", "Total value is 0 or negative")
            return
        }

        var currentAngle = 0f

        slices.forEach { slice ->
            val sweepAngle = (slice.value / total) * 360f

            paint.color = slice.color
            canvas.drawArc(rect, currentAngle, sweepAngle, true, paint)

            // Draw percentage text
            val angle = Math.toRadians((currentAngle + sweepAngle / 2).toDouble())
            val radius = rect.width() / 2
            val textX = rect.centerX() + (radius * 0.6 * Math.cos(angle)).toFloat()
            val textY = rect.centerY() + (radius * 0.6 * Math.sin(angle)).toFloat()

            val percentage = (slice.value / total * 100).toInt()
            if (percentage > 5) { // Only show text if slice is large enough
                canvas.drawText("${percentage}%", textX, textY, textPaint)
            }

            currentAngle += sweepAngle
        }
    }
}