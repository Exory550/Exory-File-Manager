package com.exory550.exoryfilemanager.extensions

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.text.toSpannable

/**
 * Extension functions for TextView
 */

/**
 * Get text from TextView safely
 */
val TextView.safeText: String
    get() = text?.toString() ?: ""

/**
 * Set text safely (handles null)
 */
fun TextView.setTextSafely(text: String?) {
    this.text = text ?: ""
}

/**
 * Set text with formatting
 */
fun TextView.setTextWithFormat(format: String, vararg args: Any?) {
    text = String.format(format, *args)
}

/**
 * Set text with resource formatting
 */
fun TextView.setTextWithFormat(@StringRes formatRes: Int, vararg args: Any?) {
    text = context.getString(formatRes, *args)
}

/**
 * Append text
 */
fun TextView.appendText(text: CharSequence) {
    append(text)
}

/**
 * Append line
 */
fun TextView.appendLine(text: CharSequence) {
    append(text)
    append("\n")
}

/**
 * Prepend text
 */
fun TextView.prependText(text: CharSequence) {
    this.text = "$text${this.text}"
}

/**
 * Clear text
 */
fun TextView.clear() {
    text = ""
}

/**
 * Check if TextView is empty
 */
val TextView.isEmpty: Boolean
    get() = safeText.isEmpty()

/**
 * Check if TextView is not empty
 */
val TextView.isNotEmpty: Boolean
    get() = safeText.isNotEmpty()

/**
 * Check if TextView is blank
 */
val TextView.isBlank: Boolean
    get() = safeText.isBlank()

/**
 * Check if TextView is not blank
 */
val TextView.isNotBlank: Boolean
    get() = safeText.isNotBlank()

/**
 * Set text color conditionally
 */
fun TextView.setTextColorConditional(condition: Boolean, @ColorInt colorTrue: Int, @ColorInt colorFalse: Int) {
    setTextColor(if (condition) colorTrue else colorFalse)
}

/**
 * Set text size in SP
 */
fun TextView.setTextSizeSp(sizeSp: Float) {
    setTextSize(sizeSp)
}

/**
 * Set text size in DP
 */
fun TextView.setTextSizeDp(sizeDp: Float) {
    setTextSize(android.util.TypedValue.COMPLEX_UNIT_DIP, sizeDp)
}

/**
 * Set text size in PX
 */
fun TextView.setTextSizePx(sizePx: Float) {
    setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, sizePx)
}

/**
 * Make text bold
 */
fun TextView.setBold(bold: Boolean = true) {
    setTypeface(null, if (bold) Typeface.BOLD else Typeface.NORMAL)
}

/**
 * Make text italic
 */
fun TextView.setItalic(italic: Boolean = true) {
    setTypeface(null, if (italic) Typeface.ITALIC else Typeface.NORMAL)
}

/**
 * Make text bold and italic
 */
fun TextView.setBoldItalic() {
    setTypeface(null, Typeface.BOLD_ITALIC)
}

/**
 * Underline text
 */
fun TextView.setUnderlined(underlined: Boolean = true) {
    if (underlined) {
        paint.isUnderlineText = true
    } else {
        paint.isUnderlineText = false
    }
    invalidate()
}

/**
 * Strike through text
 */
fun TextView.setStrikeThrough(strikeThrough: Boolean = true) {
    paint.isStrikeThruText = strikeThrough
    invalidate()
}

/**
 * Set all caps
 */
fun TextView.setAllCaps(allCaps: Boolean = true) {
    isAllCaps = allCaps
}

/**
 * Set line spacing
 */
fun TextView.setLineSpacingExtra(extra: Float) {
    setLineSpacing(extra, 1.0f)
}

/**
 * Set line spacing multiplier
 */
fun TextView.setLineSpacingMultiplier(multiplier: Float) {
    setLineSpacing(0f, multiplier)
}

/**
 * Set max lines with ellipsize
 */
fun TextView.setMaxLinesWithEllipsize(maxLines: Int, where: TextUtils.TruncateAt = TextUtils.TruncateAt.END) {
    this.maxLines = maxLines
    ellipsize = where
}

/**
 * Set single line with ellipsize
 */
fun TextView.setSingleLineWithEllipsize(where: TextUtils.TruncateAt = TextUtils.TruncateAt.END) {
    maxLines = 1
    ellipsize = where
    isSingleLine = true
}

/**
 * Set compound drawables
 */
fun TextView.setCompoundDrawables(
    left: Drawable? = null,
    top: Drawable? = null,
    right: Drawable? = null,
    bottom: Drawable? = null
) {
    setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)
}

/**
 * Set compound drawables from resources
 */
fun TextView.setCompoundDrawablesRes(
    @DrawableRes left: Int = 0,
    @DrawableRes top: Int = 0,
    @DrawableRes right: Int = 0,
    @DrawableRes bottom: Int = 0
) {
    val context = context
    setCompoundDrawablesWithIntrinsicBounds(
        if (left != 0) ContextCompat.getDrawable(context, left) else null,
        if (top != 0) ContextCompat.getDrawable(context, top) else null,
        if (right != 0) ContextCompat.getDrawable(context, right) else null,
        if (bottom != 0) ContextCompat.getDrawable(context, bottom) else null
    )
}

/**
 * Set compound drawables with tint
 */
fun TextView.setCompoundDrawablesWithTint(
    left: Drawable? = null,
    top: Drawable? = null,
    right: Drawable? = null,
    bottom: Drawable? = null,
    @ColorInt tintColor: Int
) {
    left?.applyTint(tintColor)
    top?.applyTint(tintColor)
    right?.applyTint(tintColor)
    bottom?.applyTint(tintColor)
    setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)
}

/**
 * Set compound drawable padding
 */
fun TextView.setCompoundDrawablePadding(padding: Int) {
    compoundDrawablePadding = padding
}

/**
 * Set text appearance from style
 */
fun TextView.setTextAppearanceCompat(@androidx.annotation.StyleRes styleRes: Int) {
    androidx.core.widget.TextViewCompat.setTextAppearance(this, styleRes)
}

/**
 * Set auto size text with defaults
 */
fun TextView.setAutoSizeTextWithDefaults() {
    androidx.core.widget.TextViewCompat.setAutoSizeTextTypeWithDefaults(
        this,
        androidx.core.widget.TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
    )
}

/**
 * Set auto size text with granular values
 */
fun TextView.setAutoSizeTextWithGranularity(
    minSize: Int,
    maxSize: Int,
    stepGranularity: Int,
    unit: Int = android.util.TypedValue.COMPLEX_UNIT_SP
) {
    androidx.core.widget.TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
        this, minSize, maxSize, stepGranularity, unit
    )
}

/**
 * Make part of text clickable
 */
fun TextView.makeLinks(vararg links: Pair<String, View.OnClickListener>) {
    val spannableString = SpannableString(text)
    var startIndexOfLink = -1
    
    for (link in links) {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                link.second.onClick(widget)
            }
        }
        
        startIndexOfLink = this.text.toString().indexOf(link.first, startIndexOfLink + 1)
        if (startIndexOfLink >= 0) {
            spannableString.setSpan(
                clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
    
    movementMethod = LinkMovementMethod.getInstance()
    setText(spannableString, TextView.BufferType.SPANNABLE)
}

/**
 * Highlight part of text with color
 */
fun TextView.highlightText(substring: String, @ColorInt color: Int) {
    val spannable = SpannableString(text)
    var index = text.toString().indexOf(substring)
    
    while (index >= 0) {
        spannable.setSpan(
            ForegroundColorSpan(color),
            index,
            index + substring.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        index = text.toString().indexOf(substring, index + substring.length)
    }
    
    setText(spannable)
}

/**
 * Make part of text bold
 */
fun TextView.makeBold(substring: String) {
    val spannable = SpannableString(text)
    var index = text.toString().indexOf(substring)
    
    while (index >= 0) {
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            index,
            index + substring.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        index = text.toString().indexOf(substring, index + substring.length)
    }
    
    setText(spannable)
}

/**
 * Make part of text italic
 */
fun TextView.makeItalic(substring: String) {
    val spannable = SpannableString(text)
    var index = text.toString().indexOf(substring)
    
    while (index >= 0) {
        spannable.setSpan(
            StyleSpan(Typeface.ITALIC),
            index,
            index + substring.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        index = text.toString().indexOf(substring, index + substring.length)
    }
    
    setText(spannable)
}

/**
 * Underline part of text
 */
fun TextView.underlineText(substring: String) {
    val spannable = SpannableString(text)
    var index = text.toString().indexOf(substring)
    
    while (index >= 0) {
        spannable.setSpan(
            UnderlineSpan(),
            index,
            index + substring.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        index = text.toString().indexOf(substring, index + substring.length)
    }
    
    setText(spannable)
}

/**
 * Set HTML text
 */
fun TextView.setHtmlText(html: String) {
    text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
    movementMethod = LinkMovementMethod.getInstance()
}

/**
 * Set HTML text from resource
 */
fun TextView.setHtmlText(@StringRes htmlRes: Int) {
    setHtmlText(context.getString(htmlRes))
}

/**
 * Build spannable string
 */
fun buildSpanned(block: SpannableStringBuilder.() -> Unit): SpannableString {
    return SpannableStringBuilder().apply(block).toSpannableString()
}

/**
 * SpannableStringBuilder helper
 */
class SpannableStringBuilder {
    private val builder = android.text.SpannableStringBuilder()
    
    fun append(text: CharSequence) {
        builder.append(text)
    }
    
    fun appendWithSpan(text: CharSequence, vararg spans: Any) {
        val start = builder.length
        builder.append(text)
        spans.forEach { span ->
            builder.setSpan(span, start, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
    
    fun toSpannableString(): SpannableString {
        return SpannableString(builder)
    }
}

/**
 * Create a spannable string with spans
 */
fun spannableString(init: SpannableStringBuilder.() -> Unit): SpannableString {
    return buildSpanned(init)
}

/**
 * Animate text change
 */
fun TextView.animateTextChange(newText: String, duration: Long = 300) {
    animate()
        .alpha(0f)
        .setDuration(duration / 2)
        .withEndAction {
            text = newText
            animate()
                .alpha(1f)
                .setDuration(duration / 2)
                .start()
        }
        .start()
}

/**
 * Count lines in text
 */
val TextView.lineCountCompat: Int
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            lineCount
        } else {
            // Fallback for older versions
            layout?.lineCount ?: 0
        }
    }

/**
 * Get ellipsized text
 */
val TextView.ellipsizedText: String
    get() {
        return if (layout != null) {
            val lines = lineCountCompat
            val end = layout.getLineEnd(lines - 1)
            text.toString().substring(0, end)
        } else {
            text.toString()
        }
    }

/**
 * Check if text is ellipsized
 */
val TextView.isEllipsized: Boolean
    get() {
        return layout != null && lineCountCompat > 0 && layout.getEllipsisCount(lineCountCompat - 1) > 0
    }

/**
 * Measure text width
 */
fun TextView.measureTextWidth(): Float {
    return paint.measureText(text.toString())
}

/**
 * Measure text height
 */
val TextView.measureTextHeight: Float
    get() = paint.descent() - paint.ascent()

/**
 * Get font metrics
 */
val TextView.fontMetrics: android.graphics.Paint.FontMetrics
    get() = paint.fontMetrics

/**
 * Get baseline
 */
val TextView.baselineY: Float
    get() = baseline.toFloat()

/**
 * Set letter spacing
 */
fun TextView.setLetterSpacingCompat(spacing: Float) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        letterSpacing = spacing
    } else {
        // Fallback for older versions
        val sb = StringBuilder()
        text?.forEach { char ->
            sb.append(char)
            sb.append(' ')
        }
        text = sb.toString()
    }
}

/**
 * Set font from assets
 */
fun TextView.setFontFromAssets(fontPath: String) {
    try {
        val typeface = Typeface.createFromAsset(context.assets, fontPath)
        setTypeface(typeface)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
