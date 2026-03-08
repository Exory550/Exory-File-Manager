package com.exory550.exoryfilemanager.views

import android.content.Context
import android.graphics.PointF
import android.os.Parcel
import android.os.Parcelable
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

class MyLinearLayoutManager : LinearLayoutManager {

    private var isScrollEnabled = true
    private var smoothScrollSpeed = DEFAULT_SMOOTH_SCROLL_SPEED
    private var orientation = VERTICAL

    companion object {
        private const val DEFAULT_SMOOTH_SCROLL_SPEED = 100f
        const val MILLISECONDS_PER_INCH = 25f
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, orientation: Int, reverseLayout: Boolean) : super(
        context,
        orientation,
        reverseLayout
    )

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State,
        position: Int
    ) {
        if (!isScrollEnabled) return

        val smoothScroller = object : LinearSmoothScroller(recyclerView.context) {
            override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
                return this@MyLinearLayoutManager.computeScrollVectorForPosition(targetPosition)
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return smoothScrollSpeed / displayMetrics.densityDpi
            }
        }
        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
    }

    override fun scrollToPosition(position: Int) {
        if (isScrollEnabled) {
            super.scrollToPosition(position)
        }
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }

    override fun supportsPredictiveItemAnimations(): Boolean = false

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(superState, isScrollEnabled, smoothScrollSpeed, orientation)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            isScrollEnabled = state.isScrollEnabled
            smoothScrollSpeed = state.smoothScrollSpeed
            orientation = state.orientation
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    fun setScrollEnabled(enabled: Boolean) {
        isScrollEnabled = enabled
    }

    fun setSmoothScrollSpeed(speed: Float) {
        smoothScrollSpeed = speed
    }

    fun setLayoutOrientation(orientation: Int) {
        this.orientation = orientation
        this.orientation = orientation
    }

    class SavedState : Parcelable {
        val superState: Parcelable?
        val isScrollEnabled: Boolean
        val smoothScrollSpeed: Float
        val orientation: Int

        constructor(
            superState: Parcelable?,
            isScrollEnabled: Boolean,
            smoothScrollSpeed: Float,
            orientation: Int
        ) {
            this.superState = superState
            this.isScrollEnabled = isScrollEnabled
            this.smoothScrollSpeed = smoothScrollSpeed
            this.orientation = orientation
        }

        constructor(parcel: Parcel) {
            superState = parcel.readParcelable(javaClass.classLoader)
            isScrollEnabled = parcel.readByte() != 0.toByte()
            smoothScrollSpeed = parcel.readFloat()
            orientation = parcel.readInt()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeParcelable(superState, flags)
            parcel.writeByte(if (isScrollEnabled) 1 else 0)
            parcel.writeFloat(smoothScrollSpeed)
            parcel.writeInt(orientation)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}
