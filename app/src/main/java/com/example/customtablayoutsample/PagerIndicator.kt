package com.example.customtablayoutsample

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout
import android.widget.TextView

// source https://nda.ya.ru/t/ecVS5WeH6kE2wd
class PagerIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        setWillNotDraw(false)
        inflate(context, R.layout.pager_indicator, this)
    }

    private val textCaption = findViewById<TextView>(R.id.pager_indicator_text)
    private val audioCaption = findViewById<TextView>(R.id.pager_indicator_audio)

    private val cornerRadius = 12.dp

    private val shadowRadius = 2.dp
    private val shadowColor = Color.parseColor("#10000000")

    private var lastTouch: Float? = null

    var onTextClick: (() -> Unit)? = null
    var onAudioClick: (() -> Unit)? = null

    private val selectorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = shadowColor
        setShadowLayer(shadowRadius, 0f, 2f, shadowColor)
    }

    private val selectorRect = RectF()
    private val selectorPath = Path()
    private val textBound = Rect()

    var fractionWithMotionEventListener: ((fraction: Float, event: MotionEvent) -> Unit)? = null
    var fractionListener: ((Float) -> Unit)? = null

    var isDragging: Boolean = false

    enum class State { TEXT, AUDIO }

    var state: State = State.TEXT
        private set

    var fraction: Float = 0f
        set(value) {
            if (field == value) {
                return
            }
            if (value < 0f || value > 1f) {
                return
            }
            field = value
            fractionListener?.invoke(field)
            state = if (field > 0.5) {
                State.AUDIO
            } else {
                State.TEXT
            }
            invalidate()
        }

    fun setAudioState() {
        if (state == State.AUDIO) return
        snapValueAnimator(0f, 1f).start()
    }

    fun setTextState() {
        if (state == State.TEXT) return
        snapValueAnimator(1f, 0f).start()
    }

    init {
        textCaption.setOnClickListener { onTextClick?.invoke() }
        audioCaption.setOnClickListener { onAudioClick?.invoke() }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN && selectorRect.contains(ev.x, ev.y)) {
            isDragging = true
        }

        if (ev.action == MotionEvent.ACTION_MOVE && isDragging && lastTouch != null) {
            fraction += (ev.x - lastTouch!!) / (audioCaption.left - textCaption.left)
        }

        if (ev.action == MotionEvent.ACTION_UP && selectorRect.contains(ev.x, ev.y)) {
            val endValue = if (fraction < 0.5f) 0f else 1f
            snapValueAnimator(fraction, endValue) { isDragging = false }.start()
        }

        if (
            (ev.action == MotionEvent.ACTION_MOVE ||
                    ev.action == MotionEvent.ACTION_UP ||
                    ev.action == MotionEvent.ACTION_DOWN) &&
            isDragging
        ) {
            fractionWithMotionEventListener?.invoke(fraction, ev)
        }

        lastTouch = ev.x
        return false
    }

    private fun snapValueAnimator(start: Float, end: Float, onAnimationEnded: (() -> Unit)? = null): ValueAnimator {
        return ValueAnimator.ofFloat(start, end)
            .apply {
                addUpdateListener { animation ->
                    fraction = animation.animatedValue as Float
                }
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        onAnimationEnded?.invoke()
                    }
                })
                duration = 100L
                interpolator = LinearInterpolator()
            }
    }

    override fun drawChild(canvas: Canvas, child: View?, drawingTime: Long): Boolean {

        fun TextView.drawTextOneMoreTime() {
            paint.getTextBounds(text.toString(), 0, text.length, textBound)
            canvas.drawText(
                text.toString(),
                (this.left + this.paddingStart + textBound.left).toFloat(),
                (this.top + baseline).toFloat(),
                paint
            )
        }

        val result = super.drawChild(canvas, child, drawingTime)

        canvas.save()

        val distance = audioCaption.left - textCaption.left
        val widthDiff = textCaption.width - audioCaption.width
        val selectorWidth = textCaption.width - widthDiff * fraction
        val shift = distance * fraction
        val left = textCaption.left + shift
        val right = left + selectorWidth

        selectorRect.left = left
        selectorRect.top = textCaption.top.toFloat()
        selectorRect.right = right
        selectorRect.bottom = textCaption.bottom.toFloat()

        canvas.drawRoundRect(selectorRect, cornerRadius, cornerRadius, shadowPaint)
        canvas.drawRoundRect(selectorRect, cornerRadius, cornerRadius, selectorPaint)

        selectorPath.reset()
        selectorPath.addRoundRect(selectorRect, cornerRadius, cornerRadius, Path.Direction.CW)

        canvas.clipPath(selectorPath)

        textCaption.drawTextOneMoreTime()
        audioCaption.drawTextOneMoreTime()

        canvas.restore()

        return result
    }
}

val Int.dp get() = (this * Resources.getSystem().displayMetrics.density)
