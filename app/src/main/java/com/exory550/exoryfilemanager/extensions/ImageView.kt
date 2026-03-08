package com.exory550.exoryfilemanager.extensions

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.renderscript.*
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.exory550.exoryfilemanager.R
import java.io.File

/**
 * Extension functions for ImageView
 */

/**
 * Load image from file with Glide
 */
fun ImageView.loadImage(file: File?, placeholder: Drawable? = null) {
    if (file?.exists() == true) {
        Glide.with(context)
            .load(file)
            .apply {
                placeholder?.let { placeholder(it) }
                error(R.drawable.ic_broken_image)
                diskCacheStrategy(DiskCacheStrategy.NONE)
                skipMemoryCache(true)
            }
            .into(this)
    } else {
        setImageDrawable(placeholder ?: ContextCompat.getDrawable(context, R.drawable.ic_broken_image))
    }
}

/**
 * Load image from URI with Glide
 */
fun ImageView.loadImage(uri: Uri?, placeholder: Drawable? = null) {
    if (uri != null) {
        Glide.with(context)
            .load(uri)
            .apply {
                placeholder?.let { placeholder(it) }
                error(R.drawable.ic_broken_image)
            }
            .into(this)
    } else {
        setImageDrawable(placeholder ?: ContextCompat.getDrawable(context, R.drawable.ic_broken_image))
    }
}

/**
 * Load image from resource with Glide
 */
fun ImageView.loadImage(@DrawableRes resId: Int, placeholder: Drawable? = null) {
    Glide.with(context)
        .load(resId)
        .apply {
            placeholder?.let { placeholder(it) }
            error(R.drawable.ic_broken_image)
        }
        .into(this)
}

/**
 * Load image with custom target
 */
fun ImageView.loadImageAsync(
    file: File?,
    onSuccess: (Bitmap) -> Unit,
    onError: (Exception?) -> Unit = {}
) {
    if (file?.exists() == true) {
        Glide.with(context)
            .asBitmap()
            .load(file)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    onSuccess(resource)
                }
                
                override fun onLoadCleared(placeholder: Drawable?) {}
                
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    onError(null)
                }
            })
    } else {
        onError(null)
    }
}

/**
 * Apply blur effect to ImageView
 */
fun ImageView.blur(radius: Float = 25f) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        val bitmap = (drawable ?: return).toBitmap()
        
        val rs = RenderScript.create(context)
        val input = Allocation.createFromBitmap(rs, bitmap)
        val output = Allocation.createTyped(rs, input.type)
        val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        
        script.setRadius(radius)
        script.setInput(input)
        script.forEach(output)
        output.copyTo(bitmap)
        
        setImageBitmap(bitmap)
        rs.destroy()
    }
}

/**
 * Apply circle crop to ImageView
 */
fun ImageView.circleCrop() {
    Glide.with(context)
        .load( (drawable ?: return).toBitmap() )
        .apply(RequestOptions.circleCropTransform())
        .into(this)
}

/**
 * Apply rounded corners to ImageView
 */
fun ImageView.roundedCorners(radius: Float) {
    Glide.with(context)
        .load( (drawable ?: return).toBitmap() )
        .apply(RequestOptions.bitmapTransform(RoundedCorners(radius.toInt())))
        .into(this)
}

/**
 * Apply grayscale filter to ImageView
 */
fun ImageView.setGrayscale(grayscale: Boolean) {
    if (grayscale) {
        val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
        val filter = ColorMatrixColorFilter(colorMatrix)
        setColorFilter(filter)
    } else {
        clearColorFilter()
    }
}

/**
 * Set tint color
 */
fun ImageView.setTint(@ColorInt color: Int) {
    setColorFilter(color, PorterDuff.Mode.SRC_IN)
}

/**
 * Clear tint
 */
fun ImageView.clearTint() {
    clearColorFilter()
}

/**
 * Set alpha with animation
 */
fun ImageView.setAlphaAnimated(alpha: Float, duration: Long = 300) {
    animate()
        .alpha(alpha)
        .setDuration(duration)
        .start()
}

/**
 * Fade in image
 */
fun ImageView.fadeIn(duration: Long = 300) {
    alpha = 0f
    visibility = View.VISIBLE
    animate()
        .alpha(1f)
        .setDuration(duration)
        .start()
}

/**
 * Fade out image
 */
fun ImageView.fadeOut(duration: Long = 300) {
    animate()
        .alpha(0f)
        .setDuration(duration)
        .withEndAction { visibility = View.GONE }
        .start()
}

/**
 * Rotate image
 */
fun ImageView.rotate(degrees: Float) {
    rotation = degrees
}

/**
 * Rotate image with animation
 */
fun ImageView.rotateAnimated(degrees: Float, duration: Long = 300) {
    animate()
        .rotation(degrees)
        .setDuration(duration)
        .start()
}

/**
 * Scale image
 */
fun ImageView.scale(scaleX: Float, scaleY: Float) {
    this.scaleX = scaleX
    this.scaleY = scaleY
}

/**
 * Scale image with animation
 */
fun ImageView.scaleAnimated(scaleX: Float, scaleY: Float, duration: Long = 300) {
    animate()
        .scaleX(scaleX)
        .scaleY(scaleY)
        .setDuration(duration)
        .start()
}

/**
 * Zoom in animation
 */
fun ImageView.zoomIn(duration: Long = 300) {
    scaleAnimated(1.2f, 1.2f, duration)
}

/**
 * Zoom out animation
 */
fun ImageView.zoomOut(duration: Long = 300) {
    scaleAnimated(0.8f, 0.8f, duration)
}

/**
 * Reset scale
 */
fun ImageView.resetScale(duration: Long = 300) {
    scaleAnimated(1f, 1f, duration)
}

/**
 * Get bitmap from ImageView
 */
val ImageView.bitmap: Bitmap?
    get() = (drawable as? BitmapDrawable)?.bitmap

/**
 * Get bitmap safely
 */
fun ImageView.getBitmapSafely(): Bitmap? {
    return try {
        bitmap
    } catch (e: Exception) {
        null
    }
}

/**
 * Convert ImageView to bitmap
 */
fun ImageView.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    draw(canvas)
    return bitmap
}

/**
 * Save ImageView content to file
 */
fun ImageView.saveToFile(file: File, format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 100): Boolean {
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
 * Get dominant color from image
 */
fun ImageView.getDominantColor(): Int {
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
 * Get average color from image
 */
fun ImageView.getAverageColor(): Int {
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
 * Check if image has alpha channel
 */
val ImageView.hasAlpha: Boolean
    get() = (drawable?.toBitmap()?.hasAlpha() == true)

/**
 * Get image dimensions
 */
val ImageView.imageDimensions: Pair<Int, Int>
    get() {
        val bitmap = bitmap ?: return Pair(0, 0)
        return Pair(bitmap.width, bitmap.height)
    }

/**
 * Get image width
 */
val ImageView.imageWidth: Int
    get() = imageDimensions.first

/**
 * Get image height
 */
val ImageView.imageHeight: Int
    get() = imageDimensions.second

/**
 * Check if image is landscape
 */
val ImageView.isLandscape: Boolean
    get() = imageWidth > imageHeight

/**
 * Check if image is portrait
 */
val ImageView.isPortrait: Boolean
    get() = imageHeight > imageWidth

/**
 * Check if image is square
 */
val ImageView.isSquare: Boolean
    get() = imageWidth == imageHeight

/**
 * Fit image to view size
 */
fun ImageView.fitToView() {
    scaleType = ImageView.ScaleType.FIT_CENTER
}

/**
 * Center crop image
 */
fun ImageView.centerCrop() {
    scaleType = ImageView.ScaleType.CENTER_CROP
}

/**
 * Center inside image
 */
fun ImageView.centerInside() {
    scaleType = ImageView.ScaleType.CENTER_INSIDE
}

/**
 * Matrix scale type
 */
fun ImageView.matrixScale() {
    scaleType = ImageView.ScaleType.MATRIX
}

/**
 * Set image matrix
 */
fun ImageView.setImageMatrix(matrix: Matrix) {
    imageMatrix = matrix
}

/**
 * Add padding to image
 */
fun ImageView.setImagePadding(left: Int, top: Int, right: Int, bottom: Int) {
    setPadding(left, top, right, bottom)
}

/**
 * Set image padding uniformly
 */
fun ImageView.setImagePadding(padding: Int) {
    setPadding(padding, padding, padding, padding)
}

/**
 * Extension functions for Bitmap
 */

/**
 * Resize bitmap
 */
fun Bitmap.resize(width: Int, height: Int): Bitmap {
    return Bitmap.createScaledBitmap(this, width, height, true)
}

/**
 * Crop bitmap to square
 */
fun Bitmap.cropToSquare(): Bitmap {
    val size = minOf(width, height)
    val x = (width - size) / 2
    val y = (height - size) / 2
    return Bitmap.createBitmap(this, x, y, size, size)
}

/**
 * Crop bitmap to circle
 */
fun Bitmap.cropToCircle(): Bitmap {
    val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    
    val paint = Paint().apply {
        isAntiAlias = true
        shader = BitmapShader(this@cropToCircle, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }
    
    val radius = minOf(width, height) / 2f
    canvas.drawCircle(width / 2f, height / 2f, radius, paint)
    
    return output
}

/**
 * Rotate bitmap
 */
fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

/**
 * Flip bitmap horizontally
 */
fun Bitmap.flipHorizontal(): Bitmap {
    val matrix = Matrix().apply { preScale(-1f, 1f) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

/**
 * Flip bitmap vertically
 */
fun Bitmap.flipVertical(): Bitmap {
    val matrix = Matrix().apply { preScale(1f, -1f) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

/**
 * Apply blur to bitmap
 */
fun Bitmap.blur(context: Context, radius: Float = 25f): Bitmap {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        val rs = RenderScript.create(context)
        val input = Allocation.createFromBitmap(rs, this)
        val output = Allocation.createTyped(rs, input.type)
        val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        
        script.setRadius(radius)
        script.setInput(input)
        script.forEach(output)
        output.copyTo(this)
        
        rs.destroy()
    }
    return this
}
