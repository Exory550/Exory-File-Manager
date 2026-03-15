package com.exory550.exoryfilemanager.extensions

import kotlin.math.roundToInt

import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect

/**
 * Extension functions for Point and PointF
 */

/**
 * Convert Point to PointF
 */
fun Point.toPointF(): PointF {
    return PointF(x.toFloat(), y.toFloat())
}

/**
 * Convert PointF to Point
 */
fun PointF.toPoint(): Point {
    return Point(x.toInt(), y.toInt())
}

/**
 * Check if point is within rectangle
 */
fun Point.isWithin(rect: Rect): Boolean {
    return x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom
}

/**
 * Check if point is within rectangle
 */
fun PointF.isWithin(rect: Rect): Boolean {
    return x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom
}

/**
 * Get distance between two points
 */
infix fun Point.distanceTo(other: Point): Float {
    val dx = (x - other.x).toFloat()
    val dy = (y - other.y).toFloat()
    return kotlin.math.sqrt(dx * dx + dy * dy)
}

/**
 * Get distance between two points
 */
infix fun PointF.distanceTo(other: PointF): Float {
    val dx = x - other.x
    val dy = y - other.y
    return kotlin.math.sqrt(dx * dx + dy * dy)
}

/**
 * Get squared distance between two points (faster, no sqrt)
 */
infix fun Point.squaredDistanceTo(other: Point): Int {
    val dx = x - other.x
    val dy = y - other.y
    return dx * dx + dy * dy
}

/**
 * Get squared distance between two points (faster, no sqrt)
 */
infix fun PointF.squaredDistanceTo(other: PointF): Float {
    val dx = x - other.x
    val dy = y - other.y
    return dx * dx + dy * dy
}

/**
 * Get midpoint between two points
 */
infix fun Point.midpointTo(other: Point): Point {
    return Point((x + other.x) / 2, (y + other.y) / 2)
}

/**
 * Get midpoint between two points
 */
infix fun PointF.midpointTo(other: PointF): PointF {
    return PointF((x + other.x) / 2f, (y + other.y) / 2f)
}

/**
 * Get vector from point to another
 */
infix fun Point.vectorTo(other: Point): Point {
    return Point(other.x - x, other.y - y)
}

/**
 * Get vector from point to another
 */
infix fun PointF.vectorTo(other: PointF): PointF {
    return PointF(other.x - x, other.y - y)
}

/**
 * Add vector to point
 */
operator fun Point.plus(vector: Point): Point {
    return Point(x + vector.x, y + vector.y)
}

/**
 * Add vector to point
 */
operator fun PointF.plus(vector: PointF): PointF {
    return PointF(x + vector.x, y + vector.y)
}

/**
 * Subtract vector from point
 */
operator fun Point.minus(vector: Point): Point {
    return Point(x - vector.x, y - vector.y)
}

/**
 * Subtract vector from point
 */
operator fun PointF.minus(vector: PointF): PointF {
    return PointF(x - vector.x, y - vector.y)
}

/**
 * Multiply point by scalar
 */
operator fun Point.times(scalar: Int): Point {
    return Point(x * scalar, y * scalar)
}

/**
 * Multiply point by scalar
 */
operator fun PointF.times(scalar: Float): PointF {
    return PointF(x * scalar, y * scalar)
}

/**
 * Divide point by scalar
 */
operator fun Point.div(scalar: Int): Point {
    return Point(x / scalar, y / scalar)
}

/**
 * Divide point by scalar
 */
operator fun PointF.div(scalar: Float): PointF {
    return PointF(x / scalar, y / scalar)
}

/**
 * Rotate point around origin
 */
fun Point.rotate(degrees: Float): Point {
    val rad = Math.toRadians(degrees.toDouble())
    val cos = kotlin.math.cos(rad)
    val sin = kotlin.math.sin(rad)
    val newX = (x * cos - y * sin).toInt()
    val newY = (x * sin + y * cos).toInt()
    return Point(newX, newY)
}

/**
 * Rotate point around origin
 */
fun PointF.rotate(degrees: Float): PointF {
    val rad = Math.toRadians(degrees.toDouble())
    val cos = kotlin.math.cos(rad)
    val sin = kotlin.math.sin(rad)
    val newX = (x * cos - y * sin).toFloat()
    val newY = (x * sin + y * cos).toFloat()
    return PointF(newX, newY)
}

/**
 * Rotate point around another point
 */
fun Point.rotateAround(center: Point, degrees: Float): Point {
    val dx = x - center.x
    val dy = y - center.y
    val rad = Math.toRadians(degrees.toDouble())
    val cos = kotlin.math.cos(rad)
    val sin = kotlin.math.sin(rad)
    val newX = (dx * cos - dy * sin + center.x).toInt()
    val newY = (dx * sin + dy * cos + center.y).toInt()
    return Point(newX, newY)
}

/**
 * Rotate point around another point
 */
fun PointF.rotateAround(center: PointF, degrees: Float): PointF {
    val dx = x - center.x
    val dy = y - center.y
    val rad = Math.toRadians(degrees.toDouble())
    val cos = kotlin.math.cos(rad)
    val sin = kotlin.math.sin(rad)
    val newX = (dx * cos - dy * sin + center.x).toFloat()
    val newY = (dx * sin + dy * cos + center.y).toFloat()
    return PointF(newX, newY)
}

/**
 * Get angle between two points (in degrees)
 */
infix fun Point.angleTo(other: Point): Float {
    val dx = (other.x - x).toFloat()
    val dy = (other.y - y).toFloat()
    return kotlin.math.atan2(dy, dx) * 180f / kotlin.math.PI.toFloat()
}

/**
 * Get angle between two points (in degrees)
 */
infix fun PointF.angleTo(other: PointF): Float {
    val dx = other.x - x
    val dy = other.y - y
    return kotlin.math.atan2(dy, dx) * 180f / kotlin.math.PI.toFloat()
}

/**
 * Normalize point (for vectors)
 */
fun Point.normalize(): PointF {
    val length = kotlin.math.sqrt((x * x + y * y).toDouble()).toFloat()
    return if (length > 0) {
        PointF(x / length, y / length)
    } else {
        PointF(0f, 0f)
    }
}

/**
 * Normalize point (for vectors)
 */
fun PointF.normalize(): PointF {
    val length = kotlin.math.sqrt((x * x + y * y).toDouble()).toFloat()
    return if (length > 0) {
        PointF(x / length, y / length)
    } else {
        PointF(0f, 0f)
    }
}

/**
 * Get length of point as vector
 */
val Point.length: Float
    get() = kotlin.math.sqrt((x * x + y * y).toDouble()).toFloat()

/**
 * Get length of point as vector
 */
val PointF.length: Float
    get() = kotlin.math.sqrt((x * x + y * y).toDouble()).toFloat()

/**
 * Get squared length (faster, no sqrt)
 */
val Point.squaredLength: Int
    get() = x * x + y * y

/**
 * Get squared length (faster, no sqrt)
 */
val PointF.squaredLength: Float
    get() = x * x + y * y

/**
 * Get angle of point as vector (in degrees)
 */
val Point.angle: Float
    get() = kotlin.math.atan2(y.toDouble(), x.toDouble()).toFloat() * 180f / kotlin.math.PI.toFloat()

/**
 * Get angle of point as vector (in degrees)
 */
val PointF.angle: Float
    get() = kotlin.math.atan2(y.toDouble(), x.toDouble()).toFloat() * 180f / kotlin.math.PI.toFloat()

/**
 * Check if point is zero
 */
val Point.isZero: Boolean
    get() = x == 0 && y == 0

/**
 * Check if point is zero
 */
val PointF.isZero: Boolean
    get() = x == 0f && y == 0f

/**
 * Check if point is within bounds
 */
fun Point.isWithinBounds(width: Int, height: Int): Boolean {
    return x in 0 until width && y in 0 until height
}

/**
 * Check if point is within bounds
 */
fun PointF.isWithinBounds(width: Int, height: Int): Boolean {
    return x in 0f until width.toFloat() && y in 0f until height.toFloat()
}

/**
 * Clamp point to bounds
 */
fun Point.clamp(minX: Int, minY: Int, maxX: Int, maxY: Int): Point {
    return Point(
        x.coerceIn(minX, maxX),
        y.coerceIn(minY, maxY)
    )
}

/**
 * Clamp point to bounds
 */
fun PointF.clamp(minX: Float, minY: Float, maxX: Float, maxY: Float): PointF {
    return PointF(
        x.coerceIn(minX, maxX),
        y.coerceIn(minY, maxY)
    )
}

/**
 * Convert point to string
 */
fun Point.toShortString(): String {
    return "($x, $y)"
}

/**
 * Convert point to string
 */
fun PointF.toShortString(): String {
    return String.format("(%.1f, %.1f)", x, y)
}

/**
 * Round point to nearest integer
 */
fun PointF.round(): Point {
    return Point(kotlin.math.roundToInt(x), kotlin.math.roundToInt(y))
}

/**
 * Floor point to integer
 */
fun PointF.floor(): Point {
    return Point(kotlin.math.floor(x.toDouble()).toInt(), kotlin.math.floor(y.toDouble()).toInt())
}

/**
 * Ceil point to integer
 */
fun PointF.ceil(): Point {
    return Point(kotlin.math.ceil(x.toDouble()).toInt(), kotlin.math.ceil(y.toDouble()).toInt())
}

/**
 * Get manhattan distance between points
 */
infix fun Point.manhattanDistanceTo(other: Point): Int {
    return kotlin.math.abs(x - other.x) + kotlin.math.abs(y - other.y)
}

/**
 * Get manhattan distance between points
 */
infix fun PointF.manhattanDistanceTo(other: PointF): Float {
    return kotlin.math.abs(x - other.x) + kotlin.math.abs(y - other.y)
}

/**
 * Get chebyshev distance between points
 */
infix fun Point.chebyshevDistanceTo(other: Point): Int {
    return maxOf(kotlin.math.abs(x - other.x), kotlin.math.abs(y - other.y))
}

/**
 * Get chebyshev distance between points
 */
infix fun PointF.chebyshevDistanceTo(other: PointF): Float {
    return maxOf(kotlin.math.abs(x - other.x), kotlin.math.abs(y - other.y))
}

/**
 * Linear interpolation between points
 */
infix fun Point.lerp(other: Point): (Float) -> Point {
    return { t ->
        Point(
            (x + (other.x - x) * t).toInt(),
            (y + (other.y - y) * t).toInt()
        )
    }
}

/**
 * Linear interpolation between points
 */
infix fun PointF.lerp(other: PointF): (Float) -> PointF {
    return { t ->
        PointF(
            x + (other.x - x) * t,
            y + (other.y - y) * t
        )
    }
}

/**
 * Dot product of two points as vectors
 */
infix fun Point.dot(other: Point): Int {
    return x * other.x + y * other.y
}

/**
 * Dot product of two points as vectors
 */
infix fun PointF.dot(other: PointF): Float {
    return x * other.x + y * other.y
}

/**
 * Cross product of two points as vectors
 */
infix fun Point.cross(other: Point): Int {
    return x * other.y - y * other.x
}

/**
 * Cross product of two points as vectors
 */
infix fun PointF.cross(other: PointF): Float {
    return x * other.y - y * other.x
}
