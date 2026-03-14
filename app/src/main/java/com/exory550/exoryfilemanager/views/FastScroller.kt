package com.exory550.exoryfilemanager.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.exory550.exoryfilemanager.R
import kotlin.math.max
import kotlin.math.min

class FastScroller @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var recyclerView: RecyclerView? = null
    private var thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var thumbRect = RectF()
    private var trackRect = RectF()

    private var thumbWidth = resources.getDimensionPixelSize(R.dimen.fast_scroller_thumb_width)
    private var thumbHeight = resources.getDimensionPixelSize(R.dimen.fast_scroller_thumb_height)
    private var trackWidth = resources.getDimensionPixelSize(R.dimen.fast_scroller_track_width)

    private var isDragging = false
    private var touchY = 0f
    private var thumbOffset = 0f
    private var scrollOffset = 0f

    private var thumbColor: Int
    private var trackColor: Int

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FastScroller)
        thumbColor = typedArray.getColor(
            R.styleable.FastScroller_thumbColor,
            ContextCompat.getColor(context, R.color.fast_scroller_thumb)
        )
        trackColor = typedArray.getColor(
            R.styleable.FastScroller_trackColor,
            ContextCompat.getColor(context, R.color.fast_scroller_track)
        )
        typedArray.recycle()

        thumbPaint.color = thumbColor
        thumbPaint.style = Paint.Style.FILL
        thumbPaint.isAntiAlias = true

        trackPaint.color = trackColor
        trackPaint.style = Paint.Style.FILL
        trackPaint.isAntiAlias = true
    }

    fun attachToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!isDragging) {
                    updateThumbPosition()
                }
            }
        })
    }

    private fun updateThumbPosition() {
        recyclerView?.let { rv ->
            val verticalScrollOffset = rv.computeVerticalScrollOffset()
            val verticalScrollRange = rv.computeVerticalScrollRange()
            val verticalScrollExtent = rv.computeVerticalScrollExtent()

            if (verticalScrollRange - verticalScrollExtent > 0) {
                val proportion = verticalScrollOffset.toFloat() / (verticalScrollRange - verticalScrollExtent)
                val thumbTop = proportion * (height - thumbHeight).toFloat()
                thumbRect.top = thumbTop
                thumbRect.bottom = thumbTop + thumbHeight
                invalidate()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val left = w - thumbWidth - paddingRight
        thumbRect.set(
            left.toFloat(),
            0f,
            (left + thumbWidth).toFloat(),
            thumbHeight.toFloat()
        )

        trackRect.set(
            (w - trackWidth - paddingRight).toFloat(),
            0f,
            (w - paddingRight).toFloat(),
            h.toFloat()
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRoundRect(trackRect, trackWidth / 2f, trackWidth / 2f, trackPaint)
        canvas.drawRoundRect(thumbRect, thumbWidth / 2f, thumbWidth / 2f, thumbPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (thumbRect.contains(event.x, event.y)) {
                    isDragging = true
                    touchY = event.y
                    thumbOffset = thumbRect.top
                    parent.requestDisallowInterceptTouchEvent(true)
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val newTop = (thumbOffset + (event.y - touchY)).coerceIn(0f, height - thumbHeight)
                    thumbRect.top = newTop
                    thumbRect.bottom = newTop + thumbHeight
                    invalidate()

                    scrollToPosition((newTop / (height - thumbHeight)))
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    isDragging = false
                    parent.requestDisallowInterceptTouchEvent(false)
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun scrollToPosition(proportion: Float) {
        recyclerView?.let { rv ->
            val verticalScrollRange = rv.computeVerticalScrollRange()
            val verticalScrollExtent = rv.computeVerticalScrollExtent()
            val scrollTo = (proportion * (verticalScrollRange - verticalScrollExtent)).toInt()
            rv.scrollBy(0, scrollTo - rv.computeVerticalScrollOffset())
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        recyclerView = null
    }
}
