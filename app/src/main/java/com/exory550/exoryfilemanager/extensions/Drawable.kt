package com.exory550.exoryfilemanager.extensions

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.renderscript.*
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat

/**
 * Extension functions for Drawable
 */

/**
 * Convert Drawable to Bitmap
 */
fun Drawable.toBitmap(): Bitmap {
    if (this is BitmapDrawable) {
        return bitmap
    }
    
    val bitmap = if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
        Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    } else {
        Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
    }
    
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    
    return bitmap
}

/**
 * Convert Drawable to Bitmap with specific size
 */
fun Drawable.toBitmap(width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}

/**
 * Apply tint color to drawable
 */
fun Drawable.applyTint(@ColorInt color: Int): Drawable {
    val wrapped = DrawableCompat.wrap(this).mutate()
    DrawableCompat.setTint(wrapped, color)
    return wrapped
}

/**
 * Apply tint color from resource
 */
fun Drawable.applyTintRes(context: Context, @ColorRes colorRes: Int): Drawable {
    val color = ContextCompat.getColor(context, colorRes)
    return applyTint(color)
}

/**
 * Apply tint list to drawable
 */
fun Drawable.applyTintList(@ColorInt vararg colors: Int): Drawable {
    val wrapped = DrawableCompat.wrap(this).mutate()
    val colorStateList = android.content.res.ColorStateList.valueOf(colors[0])
    DrawableCompat.setTintList(wrapped, colorStateList)
    return wrapped
}

/**
 * Create a circular version of the drawable
 */
fun Drawable.toCircular(): Drawable {
    val bitmap = toBitmap()
    val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    
    val paint = Paint().apply {
        isAntiAlias = true
        shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }
    
    val radius = minOf(bitmap.width, bitmap.height) / 2f
    canvas.drawCircle(bitmap.width / 2f, bitmap.height / 2f, radius, paint)
    
    return BitmapDrawable(bitmap.resources, output)
}

/**
 * Create a rounded corner version of the drawable
 */
fun Drawable.toRoundedCorners(radius: Float): Drawable {
    val bitmap = toBitmap()
    val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    
    val paint = Paint().apply {
        isAntiAlias = true
        shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }
    
    canvas.drawRoundRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), radius, radius, paint)
    
    return BitmapDrawable(bitmap.resources, output)
}

/**
 * Apply blur effect to drawable
 */
fun Drawable.applyBlur(context: Context, radius: Float = 25f): Drawable {
    val bitmap = toBitmap()
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        val rs = RenderScript.create(context)
        val input = Allocation.createFromBitmap(rs, bitmap)
        val output = Allocation.createTyped(rs, input.type)
        val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        
        script.setRadius(radius)
        script.setInput(input)
        script.forEach(output)
        output.copyTo(bitmap)
        
        rs.destroy()
    } else {
        // Fallback for older versions
        android.media.ThumbnailUtils.extractThumbnail(bitmap, bitmap.width, bitmap.height)
    }
    
    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Convert drawable to grayscale
 */
fun Drawable.toGrayscale(): Drawable {
    val bitmap = toBitmap()
    val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    
    val paint = Paint().apply {
        val cm = ColorMatrix().apply { setSaturation(0f) }
        colorFilter = ColorMatrixColorFilter(cm)
    }
    
    canvas.drawBitmap(bitmap, 0f, 0f, paint)
    
    return BitmapDrawable(bitmap.resources, output)
}

/**
 * Change drawable opacity
 */
fun Drawable.setOpacity(opacity: Int): Drawable {
    val wrapped = DrawableCompat.wrap(this).mutate()
    wrapped.alpha = opacity
    return wrapped
}

/**
 * Rotate drawable
 */
fun Drawable.rotate(degrees: Float): Drawable {
    val bitmap = toBitmap()
    val matrix = Matrix().apply { postRotate(degrees) }
    val output = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    return BitmapDrawable(bitmap.resources, output)
}

/**
 * Flip drawable horizontally
 */
fun Drawable.flipHorizontal(): Drawable {
    val bitmap = toBitmap()
    val matrix = Matrix().apply { preScale(-1f, 1f) }
    val output = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    return BitmapDrawable(bitmap.resources, output)
}

/**
 * Flip drawable vertically
 */
fun Drawable.flipVertical(): Drawable {
    val bitmap = toBitmap()
    val matrix = Matrix().apply { preScale(1f, -1f) }
    val output = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    return BitmapDrawable(bitmap.resources, output)
}

/**
 * Scale drawable
 */
fun Drawable.scale(scaleFactor: Float): Drawable {
    val bitmap = toBitmap()
    val newWidth = (bitmap.width * scaleFactor).toInt()
    val newHeight = (bitmap.height * scaleFactor).toInt()
    val output = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    return BitmapDrawable(bitmap.resources, output)
}

/**
 * Get average color of drawable
 */
fun Drawable.getAverageColor(): Int {
    val bitmap = toBitmap()
    var red = 0
    var green = 0
    var blue = 0
    var count = 0
    
    for (x in 0 until bitmap.width step 5) {
        for (y in 0 until bitmap.height step 5) {
            val pixel = bitmap.getPixel(x, y)
            red += Color.red(pixel)
            green += Color.green(pixel)
            blue += Color.blue(pixel)
            count++
        }
    }
    
    return if (count > 0) {
        Color.rgb(red / count, green / count, blue / count)
    } else {
        Color.TRANSPARENT
    }
}

/**
 * Get dominant color of drawable
 */
fun Drawable.getDominantColor(): Int {
    val bitmap = toBitmap()
    val colorCount = mutableMapOf<Int, Int>()
    
    for (x in 0 until bitmap.width step 10) {
        for (y in 0 until bitmap.height step 10) {
            val pixel = bitmap.getPixel(x, y)
            colorCount[pixel] = colorCount.getOrDefault(pixel, 0) + 1
        }
    }
    
    return colorCount.maxByOrNull { it.value }?.key ?: Color.TRANSPARENT
}

/**
 * Check if drawable is vector
 */
val Drawable.isVector: Boolean
    get() = this is VectorDrawable || this is VectorDrawableCompat

/**
 * Check if drawable is bitmap
 */
val Drawable.isBitmap: Boolean
    get() = this is BitmapDrawable

/**
 * Save drawable to file
 */
fun Drawable.saveToFile(file: File, format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 100): Boolean {
    return try {
        val bitmap = toBitmap()
        file.outputStream().use { out ->
            bitmap.compress(format, quality, out)
        }
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Convert drawable to byte array
 */
fun Drawable.toByteArray(format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 100): ByteArray {
    val bitmap = toBitmap()
    val stream = java.io.ByteArrayOutputStream()
    bitmap.compress(format, quality, stream)
    return stream.toByteArray()
}

/**
 * Create drawable from byte array
 */
fun Context.drawableFromByteArray(data: ByteArray): Drawable? {
    return try {
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        BitmapDrawable(resources, bitmap)
    } catch (e: Exception) {
        null
    }
}

/**
 * Create drawable from resource with specific color
 */
fun Context.getColoredDrawable(@DrawableRes drawableRes: Int, @ColorInt color: Int): Drawable {
    val drawable = ContextCompat.getDrawable(this, drawableRes) ?: return ColorDrawable(color)
    return DrawableCompat.wrap(drawable).mutate().apply {
        DrawableCompat.setTint(this, color)
    }
}

/**
 * Extension functions for ImageView
 */

/**
 * Set drawable with tint
 */
fun ImageView.setImageDrawableWithTint(drawable: Drawable?, @ColorInt color: Int) {
    drawable?.let {
        setImageDrawable(it.applyTint(color))
    }
}

/**
 * Set drawable from resource with tint
 */
fun ImageView.setImageResourceWithTint(@DrawableRes resId: Int, @ColorInt color: Int) {
    val drawable = ContextCompat.getDrawable(context, resId)
    setImageDrawableWithTint(drawable, color)
}

/**
 * Animate drawable rotation
 */
fun ImageView.rotate(duration: Long = 1000, repeat: Boolean = false) {
    val anim = android.view.animation.RotateAnimation(
        0f, 360f,
        android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
        android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
    ).apply {
        this.duration = duration
        if (repeat) {
            repeatCount = android.view.animation.Animation.INFINITE
        }
    }
    startAnimation(anim)
}

/**
 * Crossfade between two drawables
 */
fun ImageView.crossfade(newDrawable: Drawable, duration: Long = 300) {
    val crossfade = android.transition.TransitionSet().apply {
        addTransition(android.transition.Fade())
        this.duration = duration
    }
    
    android.transition.TransitionManager.beginDelayedTransition(this.parent as android.view.ViewGroup, crossfade)
    setImageDrawable(newDrawable)
}

/**
 * Extension functions for Color
 */

/**
 * Convert color int to hex string
 */
@ColorInt
fun Int.toHexString(withHash: Boolean = true): String {
    val hex = Integer.toHexString(this).uppercase()
    return if (withHash) "#$hex" else hex
}

/**
 * Get color brightness
 */
@ColorInt
fun Int.getBrightness(): Float {
    return (0.299 * Color.red(this) + 0.587 * Color.green(this) + 0.114 * Color.blue(this)) / 255
}

/**
 * Check if color is light
 */
@ColorInt
fun Int.isLight(): Boolean {
    return getBrightness() > 0.5
}

/**
 * Check if color is dark
 */
@ColorInt
fun Int.isDark(): Boolean {
    return getBrightness() <= 0.5
}

/**
 * Darken color by percentage
 */
@ColorInt
fun Int.darken(percentage: Float): Int {
    val hsv = FloatArray(3)
    Color.colorToHSV(this, hsv)
    hsv[2] = (hsv[2] * (1 - percentage)).coerceIn(0f, 1f)
    return Color.HSVToColor(Color.alpha(this), hsv)
}

/**
 * Lighten color by percentage
 */
@ColorInt
fun Int.lighten(percentage: Float): Int {
    val hsv = FloatArray(3)
    Color.colorToHSV(this, hsv)
    hsv[2] = (hsv[2] * (1 + percentage)).coerceIn(0f, 1f)
    return Color.HSVToColor(Color.alpha(this), hsv)
}

/**
 * Set color alpha
 */
@ColorInt
fun Int.withAlpha(alpha: Int): Int {
    return (this and 0x00ffffff) or (alpha shl 24)
}

/**
 * Set color alpha by percentage
 */
@ColorInt
fun Int.withAlphaPercentage(percentage: Float): Int {
    val alpha = (255 * percentage.coerceIn(0f, 1f)).toInt()
    return withAlpha(alpha)
}

/**
 * Blend two colors
 */
@ColorInt
fun Int.blendWith(@ColorInt other: Int, ratio: Float): Int {
    val inverse = 1 - ratio
    val a = (Color.alpha(this) * ratio + Color.alpha(other) * inverse).toInt()
    val r = (Color.red(this) * ratio + Color.red(other) * inverse).toInt()
    val g = (Color.green(this) * ratio + Color.green(other) * inverse).toInt()
    val b = (Color.blue(this) * ratio + Color.blue(other) * inverse).toInt()
    return Color.argb(a, r, g, b)
}
