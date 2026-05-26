package com.codestream.tetrak.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max

class AdvancedColorPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val hsv = floatArrayOf(232f, 0.64f, 1f)
    private val animatedHsv = hsv.copyOf()
    private val spectrumRect = RectF()
    private val hueRect = RectF()

    private val spectrumPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val huePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1f)
        color = Color.argb(56, 120, 120, 120)
    }
    private val markerFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }
    private val markerStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(2.5f)
        color = Color.argb(190, 0, 0, 0)
    }
    private val markerCenterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val cornerRadius = dp(22f)
    private val sliderHeight = dp(26f)
    private val gap = dp(24f)
    private val contentPadding = dp(8f)
    private var activeArea = Area.NONE
    private var changeListener: ((Int) -> Unit)? = null
    private var animator: ValueAnimator? = null

    enum class Area { NONE, SPECTRUM, HUE }

    fun setOnColorChangedListener(listener: (Int) -> Unit) {
        changeListener = listener
    }

    fun getSelectedColor(): Int = Color.HSVToColor(hsv)

    fun setColor(color: Int, animate: Boolean = false) {
        val target = FloatArray(3)
        Color.colorToHSV(color, target)
        if (animate) {
            animateTo(target)
        } else {
            hsv[0] = target[0]
            hsv[1] = target[1]
            hsv[2] = target[2]
            animatedHsv[0] = target[0]
            animatedHsv[1] = target[1]
            animatedHsv[2] = target[2]
            invalidate()
        }
        changeListener?.invoke(Color.HSVToColor(hsv))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = dp(320f).toInt()
        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val spectrumSize = max(1, width - paddingLeft - paddingRight - (contentPadding * 2).toInt())
        val desiredHeight = paddingTop + paddingBottom + (contentPadding * 2).toInt() + spectrumSize + gap.toInt() + sliderHeight.toInt() + dp(16f).toInt()
        setMeasuredDimension(width, resolveSize(desiredHeight, heightMeasureSpec))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val left = paddingLeft + contentPadding
        val right = w - paddingRight - contentPadding
        val availableWidth = right - left
        val top = paddingTop + contentPadding
        spectrumRect.set(left, top, right, top + availableWidth)
        hueRect.set(left, spectrumRect.bottom + gap, right, spectrumRect.bottom + gap + sliderHeight)
        rebuildShaders()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        rebuildShaders()
        canvas.drawRoundRect(spectrumRect, cornerRadius, cornerRadius, spectrumPaint)
        canvas.drawRoundRect(spectrumRect, cornerRadius, cornerRadius, overlayPaint)
        canvas.drawRoundRect(spectrumRect, cornerRadius, cornerRadius, borderPaint)

        canvas.drawRoundRect(hueRect, hueRect.height() / 2f, hueRect.height() / 2f, huePaint)
        canvas.drawRoundRect(hueRect, hueRect.height() / 2f, hueRect.height() / 2f, borderPaint)

        drawSpectrumMarker(canvas)
        drawHueMarker(canvas)
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        super.onDetachedFromWindow()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                activeArea = when {
                    spectrumRect.contains(event.x, event.y) -> Area.SPECTRUM
                    isNearHueSlider(event.x, event.y) -> Area.HUE
                    else -> Area.NONE
                }
                if (activeArea == Area.NONE) return false
                updateFromTouch(event.x, event.y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                updateFromTouch(event.x, event.y)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                updateFromTouch(event.x, event.y)
                parent?.requestDisallowInterceptTouchEvent(false)
                activeArea = Area.NONE
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updateFromTouch(x: Float, y: Float) {
        when (activeArea) {
            Area.SPECTRUM -> {
                val clampedX = x.coerceIn(spectrumRect.left, spectrumRect.right)
                val clampedY = y.coerceIn(spectrumRect.top, spectrumRect.bottom)
                hsv[1] = ((clampedX - spectrumRect.left) / spectrumRect.width()).coerceIn(0f, 1f)
                hsv[2] = (1f - (clampedY - spectrumRect.top) / spectrumRect.height()).coerceIn(0f, 1f)
            }
            Area.HUE -> {
                val clampedX = x.coerceIn(hueRect.left, hueRect.right)
                hsv[0] = (((clampedX - hueRect.left) / hueRect.width()) * 360f).coerceIn(0f, 360f)
            }
            Area.NONE -> return
        }
        animatedHsv[0] = hsv[0]
        animatedHsv[1] = hsv[1]
        animatedHsv[2] = hsv[2]
        changeListener?.invoke(Color.HSVToColor(hsv))
        invalidate()
    }

    private fun rebuildShaders() {
        val hueColor = Color.HSVToColor(floatArrayOf(animatedHsv[0], 1f, 1f))
        spectrumPaint.shader = LinearGradient(
            spectrumRect.left,
            spectrumRect.top,
            spectrumRect.right,
            spectrumRect.top,
            Color.WHITE,
            hueColor,
            Shader.TileMode.CLAMP
        )
        overlayPaint.shader = LinearGradient(
            spectrumRect.left,
            spectrumRect.top,
            spectrumRect.left,
            spectrumRect.bottom,
            Color.TRANSPARENT,
            Color.BLACK,
            Shader.TileMode.CLAMP
        )
        huePaint.shader = LinearGradient(
            hueRect.left,
            hueRect.centerY(),
            hueRect.right,
            hueRect.centerY(),
            intArrayOf(
                Color.RED,
                Color.YELLOW,
                Color.GREEN,
                Color.CYAN,
                Color.BLUE,
                Color.MAGENTA,
                Color.RED
            ),
            null,
            Shader.TileMode.CLAMP
        )
    }

    private fun drawSpectrumMarker(canvas: Canvas) {
        val x = spectrumRect.left + spectrumRect.width() * animatedHsv[1]
        val y = spectrumRect.top + spectrumRect.height() * (1f - animatedHsv[2])
        val color = Color.HSVToColor(animatedHsv)
        markerCenterPaint.color = color
        canvas.drawCircle(x, y, dp(14f), markerFillPaint)
        canvas.drawCircle(x, y, dp(14f), markerStrokePaint)
        canvas.drawCircle(x, y, dp(8f), markerCenterPaint)
    }

    private fun drawHueMarker(canvas: Canvas) {
        val x = hueRect.left + hueRect.width() * (animatedHsv[0] / 360f)
        val y = hueRect.centerY()
        markerCenterPaint.color = Color.HSVToColor(floatArrayOf(animatedHsv[0], 1f, 1f))
        canvas.drawCircle(x, y, dp(15f), markerFillPaint)
        canvas.drawCircle(x, y, dp(15f), markerStrokePaint)
        canvas.drawCircle(x, y, dp(8f), markerCenterPaint)
    }

    private fun animateTo(target: FloatArray) {
        animator?.cancel()
        val start = animatedHsv.copyOf()
        hsv[0] = target[0]
        hsv[1] = target[1]
        hsv[2] = target[2]
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 220L
            addUpdateListener { animation ->
                val progress = animation.animatedFraction
                animatedHsv[0] = interpolateHue(start[0], target[0], progress)
                animatedHsv[1] = start[1] + (target[1] - start[1]) * progress
                animatedHsv[2] = start[2] + (target[2] - start[2]) * progress
                invalidate()
            }
            start()
        }
    }

    private fun interpolateHue(start: Float, end: Float, fraction: Float): Float {
        var delta = end - start
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        val result = start + delta * fraction
        return (result + 360f) % 360f
    }

    private fun isNearHueSlider(x: Float, y: Float): Boolean {
        val expandedTop = hueRect.top - dp(18f)
        val expandedBottom = hueRect.bottom + dp(18f)
        return x >= hueRect.left && x <= hueRect.right && y >= expandedTop && y <= expandedBottom
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
