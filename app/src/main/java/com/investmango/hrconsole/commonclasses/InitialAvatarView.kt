package com.investmango.hrconsole.commonclasses

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class InitialAvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var initial: String = ""
    private var backgroundColor: Int = 0

    // Array of material colors for background
    private val colors = arrayOf(
        Color.parseColor("#F44336"), // Red
        Color.parseColor("#E91E63"), // Pink
        Color.parseColor("#9C27B0"), // Purple
        Color.parseColor("#673AB7"), // Deep Purple
        Color.parseColor("#3F51B5"), // Indigo
        Color.parseColor("#2196F3"), // Blue
        Color.parseColor("#03A9F4"), // Light Blue
        Color.parseColor("#00BCD4"), // Cyan
        Color.parseColor("#009688"), // Teal
        Color.parseColor("#4CAF50")  // Green
    )

    init {
        textPaint.apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
        }
    }

    fun setName(name: String) {
        initial = name.trim().takeIf { it.isNotEmpty() }?.let {
            it.first().uppercase()
        } ?: ""

        // Generate consistent color based on name
        backgroundColor = if (initial.isNotEmpty()) {
            colors[Math.abs(name.hashCode()) % colors.size]
        } else {
            colors[0]
        }

        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = resolveSize(100, widthMeasureSpec)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()
        val radius = width.coerceAtMost(height) / 2f

        // Draw circle background
        paint.color = backgroundColor
        canvas.drawCircle(width / 2f, height / 2f, radius, paint)

        // Draw text
        if (initial.isNotEmpty()) {
            textPaint.textSize = radius * 0.8f
            val textHeight = (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(
                initial,
                width / 2f,
                height / 2f - textHeight,
                textPaint
            )
        }
    }
}