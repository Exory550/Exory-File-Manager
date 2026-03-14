package com.exory550.exoryfilemanager.extensions

import android.content.res.Resources

import android.animation.*
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import android.view.*
import android.view.animation.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.core.view.WindowInsetsCompat.Type
import androidx.recyclerview.widget.RecyclerView
import com.exory550.exoryfilemanager.R

/**
 * Extension functions for View and related classes
 */

/**
 * Convert dp to pixels
 */
val Number.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

/**
 * Convert sp to pixels
 */
val Number.sp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

/**
 * Convert pixels to dp
 */
val Number.pxToDp: Float
    get() = this.toFloat() / Resources.getSystem().displayMetrics.density

/**
 * Show view
 */
fun View.show() {
    visibility = View.VISIBLE
}

/**
 * Hide view (GONE)
 */
fun View.hide() {
    visibility = View.GONE
}

/**
 * Hide view with INVISIBLE
 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * Toggle visibility
 */
fun View.toggle() {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
}

/**
 * Set visibility with condition
 */
fun View.visibleIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

/**
 * Set visibility with condition (INVISIBLE)
 */
fun View.invisibleIf(condition: Boolean) {
    visibility = if (condition) View.INVISIBLE else View.VISIBLE
}

/**
 * Check if view is visible
 */
val View.isVisible: Boolean
    get() = visibility == View.VISIBLE

/**
 * Check if view is gone
 */
val View.isGone: Boolean
    get() = visibility == View.GONE

/**
 * Check if view is invisible
 */
val View.isInvisible: Boolean
    get() = visibility == View.INVISIBLE

/**
 * Enable view
 */
fun View.enable() {
    isEnabled = true
}

/**
 * Disable view
 */
fun View.disable() {
    isEnabled = false
}

/**
 * Set enabled with condition
 */
fun View.enableIf(condition: Boolean) {
    isEnabled = condition
}

/**
 * Check if view is enabled
 */
val View.isEnabledCompat: Boolean
    get() = isEnabled

/**
 * Set selected with condition
 */
fun View.selectIf(condition: Boolean) {
    isSelected = condition
}

/**
 * Set background color
 */
fun View.setBackgroundColor(@ColorInt color: Int) {
    setBackgroundColor(color)
}

/**
 * Set background drawable
 */
fun View.setBackgroundDrawable(drawable: Drawable?) {
    background = drawable
}

/**
 * Set background resource with compatibility
 */
fun View.setBackgroundResourceCompat(@DrawableRes resId: Int) {
    setBackgroundResource(resId)
}

/**
 * Set padding in pixels
 */
fun View.setPadding(padding: Int) {
    setPadding(padding, padding, padding, padding)
}

/**
 * Set padding in dp
 */
fun View.setPaddingDp(paddingDp: Int) {
    val paddingPx = paddingDp.dp
    setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
}

/**
 * Set horizontal padding
 */
fun View.setHorizontalPadding(padding: Int) {
    setPadding(padding, paddingTop, padding, paddingBottom)
}

/**
 * Set vertical padding
 */
fun View.setVerticalPadding(padding: Int) {
    setPadding(paddingLeft, padding, paddingRight, padding)
}

/**
 * Add padding
 */
fun View.addPadding(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) {
    setPadding(paddingLeft + left, paddingTop + top, paddingRight + right, paddingBottom + bottom)
}

/**
 * Get view width including margins
 */
val View.widthWithMargins: Int
    get() {
        val params = layoutParams as? ViewGroup.MarginLayoutParams
        return width + (params?.leftMargin ?: 0) + (params?.rightMargin ?: 0)
    }

/**
 * Get view height including margins
 */
val View.heightWithMargins: Int
    get() {
        val params = layoutParams as? ViewGroup.MarginLayoutParams
        return height + (params?.topMargin ?: 0) + (params?.bottomMargin ?: 0)
    }

/**
 * Set margins
 */
fun View.setMargins(left: Int, top: Int, right: Int, bottom: Int) {
    val params = layoutParams as? ViewGroup.MarginLayoutParams
    params?.setMargins(left, top, right, bottom)
    layoutParams = params
}

/**
 * Set margins in dp
 */
fun View.setMarginsDp(left: Int, top: Int, right: Int, bottom: Int) {
    setMargins(left.dp, top.dp, right.dp, bottom.dp)
}

/**
 * Set margin
 */
fun View.setMargin(margin: Int) {
    setMargins(margin, margin, margin, margin)
}

/**
 * Set margin in dp
 */
fun View.setMarginDp(marginDp: Int) {
    setMargins(marginDp.dp, marginDp.dp, marginDp.dp, marginDp.dp)
}

/**
 * Get layout params
 */
inline fun <reified T : ViewGroup.LayoutParams> View.updateLayoutParams(block: T.() -> Unit) {
    val params = layoutParams as? T ?: return
    block(params)
    layoutParams = params
}

/**
 * Set width
 */
fun View.setWidth(width: Int) {
    updateLayoutParams<ViewGroup.LayoutParams> { this.width = width }
}

/**
 * Set height
 */
fun View.setHeight(height: Int) {
    updateLayoutParams<ViewGroup.LayoutParams> { this.height = height }
}

/**
 * Set size
 */
fun View.setSize(width: Int, height: Int) {
    updateLayoutParams<ViewGroup.LayoutParams> {
        this.width = width
        this.height = height
    }
}

/**
 * Set weight
 */
fun View.setWeight(weight: Float) {
    updateLayoutParams<LinearLayout.LayoutParams> { this.weight = weight }
}

/**
 * Set gravity
 */
fun View.setGravity(gravity: Int) {
    updateLayoutParams<LinearLayout.LayoutParams> { this.gravity = gravity }
}

/**
 * Set layout rules for RelativeLayout
 */
fun View.addRule(verb: Int, subject: Int = 0) {
    val params = layoutParams as? RelativeLayout.LayoutParams
    params?.addRule(verb, subject)
    layoutParams = params
}

/**
 * Remove layout rules for RelativeLayout
 */
fun View.removeRule(verb: Int) {
    val params = layoutParams as? RelativeLayout.LayoutParams
    params?.removeRule(verb)
    layoutParams = params
}

/**
 * Center in parent
 */
fun View.centerInParent() {
    val params = layoutParams as? RelativeLayout.LayoutParams
    params?.addRule(RelativeLayout.CENTER_IN_PARENT)
    layoutParams = params
}

/**
 * Center horizontally
 */
fun View.centerHorizontal() {
    val params = layoutParams as? RelativeLayout.LayoutParams
    params?.addRule(RelativeLayout.CENTER_HORIZONTAL)
    layoutParams = params
}

/**
 * Center vertically
 */
fun View.centerVertical() {
    val params = layoutParams as? RelativeLayout.LayoutParams
    params?.addRule(RelativeLayout.CENTER_VERTICAL)
    layoutParams = params
}

/**
 * Align parent left
 */
fun View.alignParentLeft() {
    val params = layoutParams as? RelativeLayout.LayoutParams
    params?.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
    layoutParams = params
}

/**
 * Align parent right
 */
fun View.alignParentRight() {
    val params = layoutParams as? RelativeLayout.LayoutParams
    params?.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
    layoutParams = params
}

/**
 * Align parent top
 */
fun View.alignParentTop() {
    val params = layoutParams as? RelativeLayout.LayoutParams
    params?.addRule(RelativeLayout.ALIGN_PARENT_TOP)
    layoutParams = params
}

/**
 * Align parent bottom
 */
fun View.alignParentBottom() {
    val params = layoutParams as? RelativeLayout.LayoutParams
    params?.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
    layoutParams = params
}

/**
 * Below view
 */
fun View.below(viewId: Int) {
    val params = layoutParams as? RelativeLayout.LayoutParams
    params?.addRule(RelativeLayout.BELOW, viewId)
    layoutParams = params
}

/**
 * Above view
 */
fun View.above(viewId: Int) {
    val params = layoutParams as? RelativeLayout.LayoutParams
    params?.addRule(RelativeLayout.ABOVE, viewId)
    layoutParams = params
}

/**
 * To left of view
 */
fun View.leftOf(viewId: Int) {
    val params = layoutParams as? RelativeLayout.LayoutParams
    params?.addRule(RelativeLayout.LEFT_OF, viewId)
    layoutParams = params
}

/**
 * To right of view
 */
fun View.rightOf(viewId: Int) {
    val params = layoutParams as? RelativeLayout.LayoutParams
    params?.addRule(RelativeLayout.RIGHT_OF, viewId)
    layoutParams = params
}

/**
 * Align left with view
 */
fun View.alignLeft(viewId: Int) {
    val params = layoutParams as? RelativeLayout.LayoutParams
    params?.addRule(RelativeLayout.ALIGN_LEFT, viewId)
    layoutParams = params
}

/**
 * Align right with view
 */
fun View.alignRight(viewId: Int) {
    val params = layoutParams as? RelativeLayout.LayoutParams
    params?.addRule(RelativeLayout.ALIGN_RIGHT, viewId)
    layoutParams = params
}

/**
 * Align top with view
 */
fun View.alignTop(viewId: Int) {
    val params = layoutParams as? RelativeLayout.LayoutParams
    params?.addRule(RelativeLayout.ALIGN_TOP, viewId)
    layoutParams = params
}

/**
 * Align bottom with view
 */
fun View.alignBottom(viewId: Int) {
    val params = layoutParams as? RelativeLayout.LayoutParams
    params?.addRule(RelativeLayout.ALIGN_BOTTOM, viewId)
    layoutParams = params
}

/**
 * Set elevation
 */
fun View.setElevationCompat(elevation: Float) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        this.elevation = elevation
    }
}

/**
 * Set translation X
 */
fun View.setTranslationXCompat(translationX: Float) {
    this.translationX = translationX
}

/**
 * Set translation Y
 */
fun View.setTranslationYCompat(translationY: Float) {
    this.translationY = translationY
}

/**
 * Set translation Z
 */
fun View.setTranslationZCompat(translationZ: Float) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        this.translationZ = translationZ
    }
}

/**
 * Set rotation
 */
fun View.setRotationCompat(rotation: Float) {
    this.rotation = rotation
}

/**
 * Set rotation X
 */
fun View.setRotationXCompat(rotationX: Float) {
    this.rotationX = rotationX
}

/**
 * Set rotation Y
 */
fun View.setRotationYCompat(rotationY: Float) {
    this.rotationY = rotationY
}

/**
 * Set scale X
 */
fun View.setScaleXCompat(scaleX: Float) {
    this.scaleX = scaleX
}

/**
 * Set scale Y
 */
fun View.setScaleYCompat(scaleY: Float) {
    this.scaleY = scaleY
}

/**
 * Set pivot X
 */
fun View.setPivotXCompat(pivotX: Float) {
    this.pivotX = pivotX
}

/**
 * Set pivot Y
 */
fun View.setPivotYCompat(pivotY: Float) {
    this.pivotY = pivotY
}

/**
 * Set alpha
 */
fun View.setAlphaCompat(alpha: Float) {
    this.alpha = alpha
}

/**
 * Fade in animation
 */
fun View.fadeIn(duration: Long = 300, startDelay: Long = 0, onEnd: (() -> Unit)? = null) {
    if (visibility == View.VISIBLE) return
    
    alpha = 0f
    visibility = View.VISIBLE
    
    animate()
        .alpha(1f)
        .setDuration(duration)
        .setStartDelay(startDelay)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onEnd?.invoke()
            }
        })
        .start()
}

/**
 * Fade out animation
 */
fun View.fadeOut(duration: Long = 300, startDelay: Long = 0, onEnd: (() -> Unit)? = null) {
    if (visibility != View.VISIBLE) return
    
    animate()
        .alpha(0f)
        .setDuration(duration)
        .setStartDelay(startDelay)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                visibility = View.GONE
                alpha = 1f
                onEnd?.invoke()
            }
        })
        .start()
}

/**
 * Scale in animation
 */
fun View.scaleIn(duration: Long = 300, onEnd: (() -> Unit)? = null) {
    if (visibility == View.VISIBLE) return
    
    scaleX = 0f
    scaleY = 0f
    visibility = View.VISIBLE
    
    animate()
        .scaleX(1f)
        .scaleY(1f)
        .setDuration(duration)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onEnd?.invoke()
            }
        })
        .start()
}

/**
 * Scale out animation
 */
fun View.scaleOut(duration: Long = 300, onEnd: (() -> Unit)? = null) {
    if (visibility != View.VISIBLE) return
    
    animate()
        .scaleX(0f)
        .scaleY(0f)
        .setDuration(duration)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                visibility = View.GONE
                scaleX = 1f
                scaleY = 1f
                onEnd?.invoke()
            }
        })
        .start()
}

/**
 * Slide in from left
 */
fun View.slideInLeft(duration: Long = 300, onEnd: (() -> Unit)? = null) {
    if (visibility == View.VISIBLE) return
    
    translationX = -width.toFloat()
    visibility = View.VISIBLE
    
    animate()
        .translationX(0f)
        .setDuration(duration)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onEnd?.invoke()
            }
        })
        .start()
}

/**
 * Slide in from right
 */
fun View.slideInRight(duration: Long = 300, onEnd: (() -> Unit)? = null) {
    if (visibility == View.VISIBLE) return
    
    translationX = width.toFloat()
    visibility = View.VISIBLE
    
    animate()
        .translationX(0f)
        .setDuration(duration)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onEnd?.invoke()
            }
        })
        .start()
}

/**
 * Slide in from top
 */
fun View.slideInTop(duration: Long = 300, onEnd: (() -> Unit)? = null) {
    if (visibility == View.VISIBLE) return
    
    translationY = -height.toFloat()
    visibility = View.VISIBLE
    
    animate()
        .translationY(0f)
        .setDuration(duration)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onEnd?.invoke()
            }
        })
        .start()
}

/**
 * Slide in from bottom
 */
fun View.slideInBottom(duration: Long = 300, onEnd: (() -> Unit)? = null) {
    if (visibility == View.VISIBLE) return
    
    translationY = height.toFloat()
    visibility = View.VISIBLE
    
    animate()
        .translationY(0f)
        .setDuration(duration)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onEnd?.invoke()
            }
        })
        .start()
}

/**
 * Slide out to left
 */
fun View.slideOutLeft(duration: Long = 300, onEnd: (() -> Unit)? = null) {
    if (visibility != View.VISIBLE) return
    
    animate()
        .translationX(-width.toFloat())
        .setDuration(duration)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                visibility = View.GONE
                translationX = 0f
                onEnd?.invoke()
            }
        })
        .start()
}

/**
 * Slide out to right
 */
fun View.slideOutRight(duration: Long = 300, onEnd: (() -> Unit)? = null) {
    if (visibility != View.VISIBLE) return
    
    animate()
        .translationX(width.toFloat())
        .setDuration(duration)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                visibility = View.GONE
                translationX = 0f
                onEnd?.invoke()
            }
        })
        .start()
}

/**
 * Slide out to top
 */
fun View.slideOutTop(duration: Long = 300, onEnd: (() -> Unit)? = null) {
    if (visibility != View.VISIBLE) return
    
    animate()
        .translationY(-height.toFloat())
        .setDuration(duration)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                visibility = View.GONE
                translationY = 0f
                onEnd?.invoke()
            }
        })
        .start()
}

/**
 * Slide out to bottom
 */
fun View.slideOutBottom(duration: Long = 300, onEnd: (() -> Unit)? = null) {
    if (visibility != View.VISIBLE) return
    
    animate()
        .translationY(height.toFloat())
        .setDuration(duration)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                visibility = View.GONE
                translationY = 0f
                onEnd?.invoke()
            }
        })
        .start()
}

/**
 * Pulse animation (scale up and down)
 */
fun View.pulse(duration: Long = 300, scale: Float = 1.1f) {
    animate()
        .scaleX(scale)
        .scaleY(scale)
        .setDuration(duration / 2)
        .withEndAction {
            animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(duration / 2)
                .start()
        }
        .start()
}

/**
 * Shake animation
 */
fun View.shake(duration: Long = 300, offset: Float = 20f) {
    val anim = TranslateAnimation(0f, offset, 0f, 0f)
    anim.duration = 50
    anim.repeatCount = 5
    anim.repeatMode = Animation.REVERSE
    startAnimation(anim)
}

/**
 * Rotate animation
 */
fun View.rotate(duration: Long = 300, degrees: Float = 360f, repeat: Boolean = false) {
    val anim = RotateAnimation(0f, degrees, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
    anim.duration = duration
    if (repeat) {
        anim.repeatCount = Animation.INFINITE
    }
    startAnimation(anim)
}

/**
 * Bounce animation
 */
fun View.bounce() {
    val anim = ScaleAnimation(1.0f, 1.1f, 1.0f, 1.1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
    anim.duration = 200
    anim.repeatMode = Animation.REVERSE
    anim.repeatCount = 1
    startAnimation(anim)
}

/**
 * Get screen location
 */
val View.screenLocation: Point
    get() {
        val location = IntArray(2)
        getLocationOnScreen(location)
        return Point(location[0], location[1])
    }

/**
 * Get window location
 */
val View.windowLocation: Point
    get() {
        val location = IntArray(2)
        getLocationInWindow(location)
        return Point(location[0], location[1])
    }

/**
 * Get view rect on screen
 */
val View.screenRect: Rect
    get() {
        val location = screenLocation
        return Rect(location.x, location.y, location.x + width, location.y + height)
    }

/**
 * Check if view is fully visible on screen
 */
fun View.isFullyVisible(): Boolean {
    if (!isShown) return false
    
    val rect = Rect()
    return getGlobalVisibleRect(rect) && rect.width() >= width && rect.height() >= height
}

/**
 * Check if view is partially visible
 */
fun View.isPartiallyVisible(): Boolean {
    if (!isShown) return false
    
    val rect = Rect()
    return getGlobalVisibleRect(rect) && rect.width() > 0 && rect.height() > 0
}

/**
 * Set onClick with debounce
 */
fun View.setOnClickDebounced(debounceTime: Long = 600, action: () -> Unit) {
    var lastClickTime = 0L
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > debounceTime) {
            lastClickTime = currentTime
            action()
        }
    }
}

/**
 * Set onClick with throttle
 */
fun View.setOnClickThrottled(throttleTime: Long = 1000, action: () -> Unit) {
    var canClick = true
    setOnClickListener {
        if (canClick) {
            canClick = false
            action()
            postDelayed({ canClick = true }, throttleTime)
        }
    }
}

/**
 * Set onLongClick with haptic feedback
 */
fun View.setOnLongClickWithHaptic(action: () -> Boolean) {
    setOnLongClickListener {
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        action()
    }
}

/**
 * Remove onClick listener
 */
fun View.removeOnClick() {
    setOnClickListener(null)
}

/**
 * Remove onLongClick listener
 */
fun View.removeOnLongClick() {
    setOnLongClickListener(null)
}

/**
 * Set ripple effect
 */
fun View.setRippleEffect(@ColorInt color: Int = Color.GRAY) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
        foreground = ContextCompat.getDrawable(context, typedValue.resourceId)
    }
}

/**
 * Set circular reveal animation
 */
fun View.circularReveal(centerX: Int, centerY: Int, duration: Long = 500) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val anim = ViewAnimationUtils.createCircularReveal(
            this, centerX, centerY, 0f, Math.max(width, height).toFloat()
        )
        anim.duration = duration
        visibility = View.VISIBLE
        anim.start()
    } else {
        fadeIn(duration)
    }
}

/**
 * Take screenshot of view
 */
fun View.takeScreenshot(): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    draw(canvas)
    return bitmap
}

/**
 * Save view as bitmap to file
 */
fun View.saveToFile(file: java.io.File, format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 100): Boolean {
    return try {
        val bitmap = takeScreenshot()
        file.outputStream().use { out ->
            bitmap.compress(format, quality, out)
        }
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Add shadow
 */
fun View.addShadow(
    radius: Float = 8f,
    dx: Float = 0f,
    dy: Float = 4f,
    @ColorInt color: Int = Color.BLACK
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        elevation = radius
        translationZ = dy
    } else {
        // Fallback for older versions
        val paint = Paint().apply {
            this.color = color
            this.maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
        }
        setLayerType(View.LAYER_TYPE_SOFTWARE, paint)
    }
}

/**
 * Remove shadow
 */
fun View.removeShadow() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        elevation = 0f
        translationZ = 0f
    } else {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }
}

/**
 * Set touch feedback (change alpha on touch)
 */
fun View.setTouchFeedback(alphaPressed: Float = 0.6f) {
    setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> v.alpha = alphaPressed
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.alpha = 1f
        }
        false
    }
}

/**
 * Extension functions for ViewGroup
 */

/**
 * Inflate layout into ViewGroup
 */
fun ViewGroup.inflate(layoutRes: Int): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, false)
}

/**
 * Add views
 */
fun ViewGroup.addViews(vararg views: View) {
    views.forEach { addView(it) }
}

/**
 * Remove all views
 */
fun ViewGroup.removeAllViewsCompat() {
    removeAllViews()
}

/**
 * Get child views as list
 */
val ViewGroup.childViews: List<View>
    get() = (0 until childCount).map { getChildAt(it) }

/**
 * For each child
 */
inline fun ViewGroup.forEachChild(action: (View) -> Unit) {
    for (i in 0 until childCount) {
        action(getChildAt(i))
    }
}

/**
 * Extension functions for RecyclerView
 */

/**
 * Add item decoration
 */
fun RecyclerView.addItemDecorationCompat(decoration: RecyclerView.ItemDecoration) {
    addItemDecoration(decoration)
}

/**
 * Smooth scroll to position
 */
fun RecyclerView.smoothScrollToPositionCompat(position: Int) {
    smoothScrollToPosition(position)
}

/**
 * Check if RecyclerView can scroll vertically
 */
val RecyclerView.canScrollVerticallyCompat: Boolean
    get() = canScrollVertically(1) || canScrollVertically(-1)

/**
 * Extension functions for ViewTreeObserver
 */

/**
 * Execute when view is laid out
 */
inline fun View.doOnLayout(crossinline action: (view: View) -> Unit) {
    if (isLaidOut && height > 0 && width > 0) {
        action(this)
    } else {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (isLaidOut && height > 0 && width > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    action(this@doOnLayout)
                }
            }
        })
    }
}

/**
 * Execute when view is attached to window
 */
inline fun View.doOnAttach(crossinline action: (view: View) -> Unit) {
    if (isAttachedToWindow) {
        action(this)
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                removeOnAttachStateChangeListener(this)
                action(v)
            }
            override fun onViewDetachedFromWindow(v: View) {}
        })
    }
}

/**
 * Execute when view is detached from window
 */
inline fun View.doOnDetach(crossinline action: (view: View) -> Unit) {
    if (!isAttachedToWindow) {
        action(this)
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}
            override fun onViewDetachedFromWindow(v: View) {
                removeOnAttachStateChangeListener(this)
                action(v)
            }
        })
    }
}

/**
 * Extension functions for WindowInsets
 */

/**
 * Apply window insets
 */
fun View.applyWindowInsets(listener: (View, WindowInsetsCompat) -> WindowInsetsCompat) {
    ViewCompat.setOnApplyWindowInsetsListener(this, listener)
}

/**
 * Get system window insets
 */
val View.systemWindowInsets: WindowInsetsCompat
    get() = ViewCompat.getRootWindowInsets(this) ?: WindowInsetsCompat(null)

/**
 * Get status bar height from insets
 */
val View.statusBarHeightFromInsets: Int
    get() = systemWindowInsets.getInsets(Type.statusBars()).top

/**
 * Get navigation bar height from insets
 */
val View.navigationBarHeightFromInsets: Int
    get() = systemWindowInsets.getInsets(Type.navigationBars()).bottom

/**
 * Consume system bars
 */
fun View.consumeSystemBars() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
        insets.consumeSystemWindowInsets()
    }
}

/**
 * Extension functions for margin layout params
 */

/**
 * Set layout margin
 */
var ViewGroup.MarginLayoutParams.margin: Int
    get() = leftMargin
    set(value) {
        setMargins(value, value, value, value)
    }

/**
 * Set layout margin in dp
 */
fun ViewGroup.MarginLayoutParams.setMarginDp(value: Int) {
    margin = value.dp
}
