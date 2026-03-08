package com.exory550.exoryfilemanager.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.exory550.exoryfilemanager.interfaces.RecyclerScrollCallback

class MyRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var scrollCallback: RecyclerScrollCallback? = null
    private var isScrolling = false
    private var isFastScrolling = false
    private var touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var initialTouchY = 0f
    private var lastScrollPosition = -1
    private var isScrollEnabled = true
    private var isLongClickScrollEnabled = false
    private var onItemClickListener: ((View, Int) -> Unit)? = null
    private var onItemLongClickListener: ((View, Int) -> Boolean)? = null
    private var onScrollListener: OnScrollListener? = null

    init {
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING -> {
                        if (!isScrolling) {
                            isScrolling = true
                            scrollCallback?.onScrollStateChanged(true)
                        }
                    }
                    SCROLL_STATE_IDLE -> {
                        if (isScrolling) {
                            isScrolling = false
                            scrollCallback?.onScrollStateChanged(false)
                        }
                        if (isFastScrolling) {
                            isFastScrolling = false
                            scrollCallback?.onFastScrollFinished()
                        }
                    }
                }
                onScrollListener?.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = layoutManager ?: return
                val firstVisibleItem = (layoutManager as? androidx.recyclerview.widget.LinearLayoutManager)
                    ?.findFirstVisibleItemPosition() ?: 0
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount

                if (lastScrollPosition != firstVisibleItem) {
                    lastScrollPosition = firstVisibleItem
                    scrollCallback?.onScrollPosition(firstVisibleItem, visibleItemCount, totalItemCount)
                }

                if (dy > touchSlop) {
                    scrollCallback?.onScrollDown()
                } else if (dy < -touchSlop) {
                    scrollCallback?.onScrollUp()
                }

                onScrollListener?.onScrolled(recyclerView, dx, dy)
            }
        })
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        if (!isScrollEnabled) return false

        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                initialTouchY = e.y
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = Math.abs(e.y - initialTouchY)
                if (dy > touchSlop * 2) {
                    isFastScrolling = true
                    scrollCallback?.onFastScrollStarted()
                }
            }
        }
        return super.onInterceptTouchEvent(e)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (!isScrollEnabled) return false
        return super.onTouchEvent(e)
    }

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        if (!isScrollEnabled) return false
        return super.fling(velocityX, velocityY)
    }

    fun setScrollCallback(callback: RecyclerScrollCallback?) {
        this.scrollCallback = callback
    }

    fun setScrollEnabled(enabled: Boolean) {
        isScrollEnabled = enabled
    }

    fun setLongClickScrollEnabled(enabled: Boolean) {
        isLongClickScrollEnabled = enabled
    }

    fun smoothScrollToTop() {
        smoothScrollToPosition(0)
        scrollCallback?.onScrollToTop()
    }

    fun smoothScrollToBottom() {
        val itemCount = adapter?.itemCount ?: 0
        if (itemCount > 0) {
            smoothScrollToPosition(itemCount - 1)
            scrollCallback?.onScrollToBottom()
        }
    }

    fun setOnItemClickListener(listener: (View, Int) -> Unit) {
        this.onItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: (View, Int) -> Boolean) {
        this.onItemLongClickListener = listener
    }

    fun setOnScrollListener(listener: OnScrollListener) {
        this.onScrollListener = listener
    }

    fun findFirstVisibleItemPosition(): Int {
        return (layoutManager as? androidx.recyclerview.widget.LinearLayoutManager)
            ?.findFirstVisibleItemPosition() ?: 0
    }

    fun findLastVisibleItemPosition(): Int {
        return (layoutManager as? androidx.recyclerview.widget.LinearLayoutManager)
            ?.findLastVisibleItemPosition() ?: 0
    }

    fun isLastItemVisible(): Boolean {
        val layoutManager = layoutManager as? androidx.recyclerview.widget.LinearLayoutManager ?: return false
        val lastVisible = findLastVisibleItemPosition()
        val itemCount = adapter?.itemCount ?: 0
        return lastVisible >= itemCount - 1
    }

    fun isFirstItemVisible(): Boolean {
        return findFirstVisibleItemPosition() <= 0
    }

    fun getVisibleItemCount(): Int {
        return layoutManager?.childCount ?: 0
    }

    fun getTotalItemCount(): Int {
        return adapter?.itemCount ?: 0
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scrollCallback = null
        onItemClickListener = null
        onItemLongClickListener = null
        onScrollListener = null
    }
}
