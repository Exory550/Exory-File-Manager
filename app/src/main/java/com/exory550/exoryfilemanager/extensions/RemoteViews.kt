package com.exory550.exoryfilemanager.extensions

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.widget.RemoteViews
import android.widget.RemoteViews.RemoteView

/**
 * Extension functions for RemoteViews
 */

/**
 * Set text with visibility check
 */
fun RemoteViews.setTextOrHide(viewId: Int, text: CharSequence?) {
    if (text.isNullOrEmpty()) {
        setViewVisibility(viewId, android.view.View.GONE)
    } else {
        setTextViewText(viewId, text)
        setViewVisibility(viewId, android.view.View.VISIBLE)
    }
}

/**
 * Set text with fallback and visibility
 */
fun RemoteViews.setTextWithFallback(viewId: Int, text: CharSequence?, fallbackText: CharSequence) {
    val finalText = if (text.isNullOrEmpty()) fallbackText else text
    setTextViewText(viewId, finalText)
    setViewVisibility(viewId, android.view.View.VISIBLE)
}

/**
 * Set image with visibility check
 */
fun RemoteViews.setImageOrHide(viewId: Int, bitmap: Bitmap?) {
    if (bitmap == null) {
        setViewVisibility(viewId, android.view.View.GONE)
    } else {
        setImageViewBitmap(viewId, bitmap)
        setViewVisibility(viewId, android.view.View.VISIBLE)
    }
}

/**
 * Set image resource with visibility check
 */
fun RemoteViews.setImageResourceOrHide(viewId: Int, resId: Int) {
    if (resId == 0) {
        setViewVisibility(viewId, android.view.View.GONE)
    } else {
        setImageViewResource(viewId, resId)
        setViewVisibility(viewId, android.view.View.VISIBLE)
    }
}

/**
 * Set image URI with visibility check
 */
fun RemoteViews.setImageUriOrHide(viewId: Int, uri: android.net.Uri?) {
    if (uri == null) {
        setViewVisibility(viewId, android.view.View.GONE)
    } else {
        setImageViewUri(viewId, uri)
        setViewVisibility(viewId, android.view.View.VISIBLE)
    }
}

/**
 * Set progress with visibility
 */
fun RemoteViews.setProgressWithVisibility(
    viewId: Int,
    max: Int,
    progress: Int,
    indeterminate: Boolean = false
) {
    setProgressBar(viewId, max, progress, indeterminate)
    setViewVisibility(viewId, android.view.View.VISIBLE)
}

/**
 * Set onClick pending intent with flags
 */
fun RemoteViews.setOnClickPendingIntentSafely(
    viewId: Int,
    intent: Intent,
    flags: Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
) {
    val pendingIntent = PendingIntent.getActivity(
        android.app.Application(), 0, intent, flags
    )
    setOnClickPendingIntent(viewId, pendingIntent)
}

/**
 * Set on click to launch activity
 */
fun RemoteViews.setOnClickLaunchActivity(
    viewId: Int,
    context: android.content.Context,
    targetClass: Class<*>,
    flags: Int = Intent.FLAG_ACTIVITY_NEW_TASK
) {
    val intent = Intent(context, targetClass).apply {
        addFlags(flags)
    }
    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    setOnClickPendingIntent(viewId, pendingIntent)
}

/**
 * Set on click to broadcast
 */
fun RemoteViews.setOnClickBroadcast(
    viewId: Int,
    context: android.content.Context,
    action: String,
    flags: Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
) {
    val intent = Intent(action)
    val pendingIntent = PendingIntent.getBroadcast(
        context, 0, intent, flags
    )
    setOnClickPendingIntent(viewId, pendingIntent)
}

/**
 * Set compound drawables for text view
 */
fun RemoteViews.setCompoundDrawables(
    viewId: Int,
    left: Int = 0,
    top: Int = 0,
    right: Int = 0,
    bottom: Int = 0
) {
    setTextViewCompoundDrawables(viewId, left, top, right, bottom)
}

/**
 * Set text color conditionally
 */
fun RemoteViews.setTextColorConditional(
    viewId: Int,
    condition: Boolean,
    colorTrue: Int,
    colorFalse: Int
) {
    setTextColor(viewId, if (condition) colorTrue else colorFalse)
}

/**
 * Set text size in SP
 */
fun RemoteViews.setTextSize(viewId: Int, sizeSp: Float) {
    setFloat(viewId, "setTextSize", sizeSp)
}

/**
 * Set text size in PX
 */
fun RemoteViews.setTextSizePx(viewId: Int, sizePx: Float) {
    setTextViewTextSize(viewId, android.util.TypedValue.COMPLEX_UNIT_PX, sizePx)
}

/**
 * Set text size in DP
 */
fun RemoteViews.setTextSizeDp(viewId: Int, sizeDp: Float) {
    setTextViewTextSize(viewId, android.util.TypedValue.COMPLEX_UNIT_DIP, sizeDp)
}

/**
 * Set text size in SP
 */
fun RemoteViews.setTextSizeSp(viewId: Int, sizeSp: Float) {
    setTextViewTextSize(viewId, android.util.TypedValue.COMPLEX_UNIT_SP, sizeSp)
}

/**
 * Set text alignment
 */
fun RemoteViews.setTextAlignment(viewId: Int, alignment: Int) {
    setInt(viewId, "setTextAlignment", alignment)
}

/**
 * Set text gravity
 */
fun RemoteViews.setGravity(viewId: Int, gravity: Int) {
    setInt(viewId, "setGravity", gravity)
}

/**
 * Set padding
 */
fun RemoteViews.setPadding(viewId: Int, left: Int, top: Int, right: Int, bottom: Int) {
    setInt(viewId, "setPadding", left, top, right, bottom)
}

/**
 * Set background color
 */
fun RemoteViews.setBackgroundColor(viewId: Int, color: Int) {
    setInt(viewId, "setBackgroundColor", color)
}

/**
 * Set background resource
 */
fun RemoteViews.setBackgroundResource(viewId: Int, resId: Int) {
    setInt(viewId, "setBackgroundResource", resId)
}

/**
 * Set alpha
 */
fun RemoteViews.setAlpha(viewId: Int, alpha: Float) {
    setFloat(viewId, "setAlpha", alpha)
}

/**
 * Set scale type for image view
 */
fun RemoteViews.setScaleType(viewId: Int, scaleType: android.widget.ImageView.ScaleType) {
    setInt(viewId, "setScaleType", scaleType.ordinal)
}

/**
 * Set max width
 */
fun RemoteViews.setMaxWidth(viewId: Int, maxWidth: Int) {
    setInt(viewId, "setMaxWidth", maxWidth)
}

/**
 * Set max height
 */
fun RemoteViews.setMaxHeight(viewId: Int, maxHeight: Int) {
    setInt(viewId, "setMaxHeight", maxHeight)
}

/**
 * Set min width
 */
fun RemoteViews.setMinWidth(viewId: Int, minWidth: Int) {
    setInt(viewId, "setMinWidth", minWidth)
}

/**
 * Set min height
 */
fun RemoteViews.setMinHeight(viewId: Int, minHeight: Int) {
    setInt(viewId, "setMinHeight", minHeight)
}

/**
 * Set lines for text view
 */
fun RemoteViews.setLines(viewId: Int, lines: Int) {
    setInt(viewId, "setLines", lines)
}

/**
 * Set max lines for text view
 */
fun RemoteViews.setMaxLines(viewId: Int, maxLines: Int) {
    setInt(viewId, "setMaxLines", maxLines)
}

/**
 * Set min lines for text view
 */
fun RemoteViews.setMinLines(viewId: Int, minLines: Int) {
    setInt(viewId, "setMinLines", minLines)
}

/**
 * Set single line for text view
 */
fun RemoteViews.setSingleLine(viewId: Int, singleLine: Boolean) {
    setBoolean(viewId, "setSingleLine", singleLine)
}

/**
 * Set ellipsize for text view
 */
fun RemoteViews.setEllipsize(viewId: Int, where: android.text.TextUtils.TruncateAt) {
    setInt(viewId, "setEllipsize", where.ordinal)
}

/**
 * Set marquee repeat limit
 */
fun RemoteViews.setMarqueeRepeatLimit(viewId: Int, limit: Int) {
    setInt(viewId, "setMarqueeRepeatLimit", limit)
}

/**
 * Set enabled state
 */
fun RemoteViews.setEnabled(viewId: Int, enabled: Boolean) {
    setBoolean(viewId, "setEnabled", enabled)
}

/**
 * Set selected state
 */
fun RemoteViews.setSelected(viewId: Int, selected: Boolean) {
    setBoolean(viewId, "setSelected", selected)
}

/**
 * Set focused state
 */
fun RemoteViews.setFocused(viewId: Int, focused: Boolean) {
    setBoolean(viewId, "setFocused", focused)
}

/**
 * Set pressed state
 */
fun RemoteViews.setPressed(viewId: Int, pressed: Boolean) {
    setBoolean(viewId, "setPressed", pressed)
}

/**
 * Set content description
 */
fun RemoteViews.setContentDescription(viewId: Int, description: CharSequence) {
    setCharSequence(viewId, "setContentDescription", description)
}

/**
 * Set tag
 */
fun RemoteViews.setTag(viewId: Int, tag: Any) {
    setObject(viewId, "setTag", tag)
}

/**
 * Set tag with key
 */
fun RemoteViews.setTag(viewId: Int, key: Int, tag: Any) {
    setObject(viewId, "setTag", key, tag)
}

/**
 * Set progress tint list
 */
fun RemoteViews.setProgressTintList(viewId: Int, color: Int) {
    setInt(viewId, "setProgressTintList", color)
}

/**
 * Set progress background tint list
 */
fun RemoteViews.setProgressBackgroundTintList(viewId: Int, color: Int) {
    setInt(viewId, "setProgressBackgroundTintList", color)
}

/**
 * Set indeterminate tint list
 */
fun RemoteViews.setIndeterminateTintList(viewId: Int, color: Int) {
    setInt(viewId, "setIndeterminateTintList", color)
}

/**
 * Apply multiple operations at once
 */
fun RemoteViews.apply(block: RemoteViews.() -> Unit) {
    block()
}

/**
 * Create a new RemoteViews with modified properties
 */
fun RemoteViews.modify(block: RemoteViews.() -> Unit): RemoteViews {
    val copy = RemoteViews(packageName, layoutId)
    copy.apply {
        // Copy all views? This is simplified
    }
    copy.block()
    return copy
}

/**
 * Chain multiple set operations
 */
class RemoteViewsBuilder(private val remoteViews: RemoteViews) {
    
    fun setText(viewId: Int, text: CharSequence): RemoteViewsBuilder = apply {
        remoteViews.setTextViewText(viewId, text)
    }
    
    fun setTextColor(viewId: Int, color: Int): RemoteViewsBuilder = apply {
        remoteViews.setTextColor(viewId, color)
    }
    
    fun setTextSize(viewId: Int, size: Float): RemoteViewsBuilder = apply {
        remoteViews.setTextSize(viewId, size)
    }
    
    fun setImageResource(viewId: Int, resId: Int): RemoteViewsBuilder = apply {
        remoteViews.setImageViewResource(viewId, resId)
    }
    
    fun setImageBitmap(viewId: Int, bitmap: Bitmap): RemoteViewsBuilder = apply {
        remoteViews.setImageViewBitmap(viewId, bitmap)
    }
    
    fun setImageUri(viewId: Int, uri: android.net.Uri): RemoteViewsBuilder = apply {
        remoteViews.setImageViewUri(viewId, uri)
    }
    
    fun setProgress(viewId: Int, max: Int, progress: Int, indeterminate: Boolean): RemoteViewsBuilder = apply {
        remoteViews.setProgressBar(viewId, max, progress, indeterminate)
    }
    
    fun setVisibility(viewId: Int, visibility: Int): RemoteViewsBuilder = apply {
        remoteViews.setViewVisibility(viewId, visibility)
    }
    
    fun setOnClickPendingIntent(viewId: Int, pendingIntent: PendingIntent): RemoteViewsBuilder = apply {
        remoteViews.setOnClickPendingIntent(viewId, pendingIntent)
    }
    
    fun setOnClickFillInIntent(viewId: Int, intent: Intent): RemoteViewsBuilder = apply {
        remoteViews.setOnClickFillInIntent(viewId, intent)
    }
    
    fun build(): RemoteViews = remoteViews
}

/**
 * Create a builder for RemoteViews
 */
fun RemoteViews.build(): RemoteViewsBuilder {
    return RemoteViewsBuilder(this)
}

/**
 * Apply a theme to RemoteViews
 */
fun RemoteViews.applyTheme(themeResId: Int): RemoteViews {
    // This is a simplified version - actual theming requires Context
    return this
}

/**
 * Create a RemoteViews for notification
 */
fun createNotificationRemoteViews(
    packageName: String,
    layoutId: Int,
    iconId: Int,
    title: CharSequence,
    text: CharSequence
): RemoteViews {
    return RemoteViews(packageName, layoutId).apply {
        setImageViewResource(android.R.id.icon, iconId)
        setTextViewText(android.R.id.title, title)
        setTextViewText(android.R.id.text, text)
    }
}

/**
 * Create a RemoteViews for app widget
 */
fun createWidgetRemoteViews(
    packageName: String,
    layoutId: Int,
    block: RemoteViewsBuilder.() -> Unit
): RemoteViews {
    val remoteViews = RemoteViews(packageName, layoutId)
    val builder = RemoteViewsBuilder(remoteViews)
    builder.block()
    return builder.build()
}
