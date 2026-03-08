package com.exory550.exoryfilemanager.extensions

import android.media.ExifInterface
import android.os.Build
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Extension functions for ExifInterface
 */

/**
 * Get all EXIF tags as a map
 */
fun ExifInterface.getAllAttributes(): Map<String, String> {
    val attributes = mutableMapOf<String, String>()
    
    // Standard tags
    val standardTags = arrayOf(
        ExifInterface.TAG_APERTURE,
        ExifInterface.TAG_APERTURE_VALUE,
        ExifInterface.TAG_ARTIST,
        ExifInterface.TAG_BITS_PER_SAMPLE,
        ExifInterface.TAG_BRIGHTNESS_VALUE,
        ExifInterface.TAG_CFA_PATTERN,
        ExifInterface.TAG_COLOR_SPACE,
        ExifInterface.TAG_COMPONENTS_CONFIGURATION,
        ExifInterface.TAG_COMPRESSED_BITS_PER_PIXEL,
        ExifInterface.TAG_COMPRESSION,
        ExifInterface.TAG_CONTRAST,
        ExifInterface.TAG_COPYRIGHT,
        ExifInterface.TAG_CUSTOM_RENDERED,
        ExifInterface.TAG_DATETIME,
        ExifInterface.TAG_DATETIME_DIGITIZED,
        ExifInterface.TAG_DATETIME_ORIGINAL,
        ExifInterface.TAG_DEFAULT_CROP_SIZE,
        ExifInterface.TAG_DEVICE_SETTING_DESCRIPTION,
        ExifInterface.TAG_DIGITAL_ZOOM_RATIO,
        ExifInterface.TAG_DNG_VERSION,
        ExifInterface.TAG_EXIF_VERSION,
        ExifInterface.TAG_EXPOSURE_BIAS_VALUE,
        ExifInterface.TAG_EXPOSURE_INDEX,
        ExifInterface.TAG_EXPOSURE_MODE,
        ExifInterface.TAG_EXPOSURE_PROGRAM,
        ExifInterface.TAG_EXPOSURE_TIME,
        ExifInterface.TAG_FILE_SOURCE,
        ExifInterface.TAG_FLASH,
        ExifInterface.TAG_FLASH_ENERGY,
        ExifInterface.TAG_FLASHPIX_VERSION,
        ExifInterface.TAG_FOCAL_LENGTH,
        ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM,
        ExifInterface.TAG_FOCAL_PLANE_RESOLUTION_UNIT,
        ExifInterface.TAG_FOCAL_PLANE_X_RESOLUTION,
        ExifInterface.TAG_FOCAL_PLANE_Y_RESOLUTION,
        ExifInterface.TAG_GAIN_CONTROL,
        ExifInterface.TAG_GAMMA,
        ExifInterface.TAG_GPS_ALTITUDE,
        ExifInterface.TAG_GPS_ALTITUDE_REF,
        ExifInterface.TAG_GPS_AREA_INFORMATION,
        ExifInterface.TAG_GPS_DATESTAMP,
        ExifInterface.TAG_GPS_DEST_BEARING,
        ExifInterface.TAG_GPS_DEST_BEARING_REF,
        ExifInterface.TAG_GPS_DEST_DISTANCE,
        ExifInterface.TAG_GPS_DEST_DISTANCE_REF,
        ExifInterface.TAG_GPS_DEST_LATITUDE,
        ExifInterface.TAG_GPS_DEST_LATITUDE_REF,
        ExifInterface.TAG_GPS_DEST_LONGITUDE,
        ExifInterface.TAG_GPS_DEST_LONGITUDE_REF,
        ExifInterface.TAG_GPS_DIFFERENTIAL,
        ExifInterface.TAG_GPS_DOP,
        ExifInterface.TAG_GPS_IMG_DIRECTION,
        ExifInterface.TAG_GPS_IMG_DIRECTION_REF,
        ExifInterface.TAG_GPS_LATITUDE,
        ExifInterface.TAG_GPS_LATITUDE_REF,
        ExifInterface.TAG_GPS_LONGITUDE,
        ExifInterface.TAG_GPS_LONGITUDE_REF,
        ExifInterface.TAG_GPS_MAP_DATUM,
        ExifInterface.TAG_GPS_MEASURE_MODE,
        ExifInterface.TAG_GPS_PROCESSING_METHOD,
        ExifInterface.TAG_GPS_SATELLITES,
        ExifInterface.TAG_GPS_SPEED,
        ExifInterface.TAG_GPS_SPEED_REF,
        ExifInterface.TAG_GPS_STATUS,
        ExifInterface.TAG_GPS_TIMESTAMP,
        ExifInterface.TAG_GPS_TRACK,
        ExifInterface.TAG_GPS_TRACK_REF,
        ExifInterface.TAG_GPS_VERSION_ID,
        ExifInterface.TAG_IMAGE_DESCRIPTION,
        ExifInterface.TAG_IMAGE_LENGTH,
        ExifInterface.TAG_IMAGE_UNIQUE_ID,
        ExifInterface.TAG_IMAGE_WIDTH,
        ExifInterface.TAG_INTEROPERABILITY_INDEX,
        ExifInterface.TAG_ISO_SPEED_RATINGS,
        ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT,
        ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH,
        ExifInterface.TAG_LIGHT_SOURCE,
        ExifInterface.TAG_MAKE,
        ExifInterface.TAG_MAKER_NOTE,
        ExifInterface.TAG_MAX_APERTURE_VALUE,
        ExifInterface.TAG_METERING_MODE,
        ExifInterface.TAG_MODEL,
        ExifInterface.TAG_NEW_SUBFILE_TYPE,
        ExifInterface.TAG_OECF,
        ExifInterface.TAG_ORF_ASPECT_FRAME,
        ExifInterface.TAG_ORF_PREVIEW_IMAGE_LENGTH,
        ExifInterface.TAG_ORF_PREVIEW_IMAGE_START,
        ExifInterface.TAG_ORF_THUMBNAIL_IMAGE,
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.TAG_PHOTOMETRIC_INTERPRETATION,
        ExifInterface.TAG_PIXEL_X_DIMENSION,
        ExifInterface.TAG_PIXEL_Y_DIMENSION,
        ExifInterface.TAG_PLANAR_CONFIGURATION,
        ExifInterface.TAG_PRIMARY_CHROMATICITIES,
        ExifInterface.TAG_REFERENCE_BLACK_WHITE,
        ExifInterface.TAG_RELATED_SOUND_FILE,
        ExifInterface.TAG_RESOLUTION_UNIT,
        ExifInterface.TAG_ROWS_PER_STRIP,
        ExifInterface.TAG_RW2_ISO,
        ExifInterface.TAG_RW2_JPG_FROM_RAW,
        ExifInterface.TAG_RW2_SENSOR_BOTTOM_BORDER,
        ExifInterface.TAG_RW2_SENSOR_LEFT_BORDER,
        ExifInterface.TAG_RW2_SENSOR_RIGHT_BORDER,
        ExifInterface.TAG_RW2_SENSOR_TOP_BORDER,
        ExifInterface.TAG_SAMPLES_PER_PIXEL,
        ExifInterface.TAG_SATURATION,
        ExifInterface.TAG_SCENE_CAPTURE_TYPE,
        ExifInterface.TAG_SCENE_TYPE,
        ExifInterface.TAG_SENSING_METHOD,
        ExifInterface.TAG_SHARPNESS,
        ExifInterface.TAG_SHUTTER_SPEED_VALUE,
        ExifInterface.TAG_SOFTWARE,
        ExifInterface.TAG_SPATIAL_FREQUENCY_RESPONSE,
        ExifInterface.TAG_SPECTRAL_SENSITIVITY,
        ExifInterface.TAG_STRIP_BYTE_COUNTS,
        ExifInterface.TAG_STRIP_OFFSETS,
        ExifInterface.TAG_SUBFILE_TYPE,
        ExifInterface.TAG_SUBJECT_AREA,
        ExifInterface.TAG_SUBJECT_DISTANCE,
        ExifInterface.TAG_SUBJECT_DISTANCE_RANGE,
        ExifInterface.TAG_SUBJECT_LOCATION,
        ExifInterface.TAG_SUBSEC_TIME,
        ExifInterface.TAG_SUBSEC_TIME_DIGITIZED,
        ExifInterface.TAG_SUBSEC_TIME_ORIGINAL,
        ExifInterface.TAG_THUMBNAIL_IMAGE_LENGTH,
        ExifInterface.TAG_THUMBNAIL_IMAGE_WIDTH,
        ExifInterface.TAG_THUMBNAIL_ORIENTATION,
        ExifInterface.TAG_TRANSFER_FUNCTION,
        ExifInterface.TAG_USER_COMMENT,
        ExifInterface.TAG_WHITE_BALANCE,
        ExifInterface.TAG_WHITE_POINT,
        ExifInterface.TAG_XMP,
        ExifInterface.TAG_X_RESOLUTION,
        ExifInterface.TAG_Y_CB_CR_COEFFICIENTS,
        ExifInterface.TAG_Y_CB_CR_POSITIONING,
        ExifInterface.TAG_Y_CB_CR_SUB_SAMPLING,
        ExifInterface.TAG_Y_RESOLUTION
    )
    
    for (tag in standardTags) {
        try {
            val value = getAttribute(tag)
            if (!value.isNullOrBlank()) {
                attributes[tag] = value
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
    
    // Double tags for GPS (if available)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        try {
            val latLong = latLong
            if (latLong != null) {
                attributes["LATITUDE_LONGITUDE"] = "${latLong[0]}, ${latLong[1]}"
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
    
    return attributes
}

/**
 * Get formatted date time
 */
fun ExifInterface.getFormattedDateTime(): String {
    val dateTime = getAttribute(ExifInterface.TAG_DATETIME) ?: return ""
    val dateTimeOriginal = getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL) ?: return dateTime
    val dateTimeDigitized = getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED) ?: return dateTimeOriginal
    
    return try {
        val inputFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US)
        val outputFormat = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(dateTimeOriginal)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateTimeOriginal
    }
}

/**
 * Get GPS location as a string
 */
fun ExifInterface.getGpsLocation(): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val latLong = latLong ?: return ""
        return String.format(Locale.US, "%.6f, %.6f", latLong[0], latLong[1])
    }
    
    val latitude = getAttribute(ExifInterface.TAG_GPS_LATITUDE) ?: return ""
    val longitude = getAttribute(ExifInterface.TAG_GPS_LONGITUDE) ?: return ""
    return "$latitude, $longitude"
}

/**
 * Get GPS altitude as a string
 */
fun ExifInterface.getGpsAltitude(): String {
    val altitude = getAttribute(ExifInterface.TAG_GPS_ALTITUDE) ?: return ""
    val altitudeRef = getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF)
    
    return if (altitudeRef == "1") {
        "-$altitude m"
    } else {
        "$altitude m"
    }
}

/**
 * Get camera make and model
 */
fun ExifInterface.getCameraInfo(): String {
    val make = getAttribute(ExifInterface.TAG_MAKE) ?: ""
    val model = getAttribute(ExifInterface.TAG_MODEL) ?: ""
    
    return when {
        make.isNotEmpty() && model.isNotEmpty() -> "$make $model"
        make.isNotEmpty() -> make
        model.isNotEmpty() -> model
        else -> ""
    }
}

/**
 * Get exposure info
 */
fun ExifInterface.getExposureInfo(): String {
    val exposureTime = getAttribute(ExifInterface.TAG_EXPOSURE_TIME)
    val fNumber = getAttribute(ExifInterface.TAG_APERTURE)
    val iso = getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS)
    
    val parts = mutableListOf<String>()
    
    exposureTime?.let { parts.add("${it}s") }
    fNumber?.let { parts.add("f/$it") }
    iso?.let { parts.add("ISO $it") }
    
    return parts.joinToString(" · ")
}

/**
 * Get focal length
 */
fun ExifInterface.getFocalLength(): String {
    val focalLength = getAttribute(ExifInterface.TAG_FOCAL_LENGTH) ?: return ""
    val focalLength35mm = getAttribute(ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM)
    
    return if (focalLength35mm != null) {
        "${focalLength}mm (${focalLength35mm}mm eq.)"
    } else {
        "${focalLength}mm"
    }
}

/**
 * Get flash info
 */
fun ExifInterface.getFlashInfo(): String {
    val flash = getAttributeInt(ExifInterface.TAG_FLASH, 0)
    
    return when (flash and 0x1) {
        0 -> "No Flash"
        else -> {
            val mode = when (flash shr 3 and 0x1) {
                1 -> " (Return detected)" else -> ""
            }
            "Flash fired$mode"
        }
    }
}

/**
 * Get white balance info
 */
fun ExifInterface.getWhiteBalanceInfo(): String {
    val whiteBalance = getAttributeInt(ExifInterface.TAG_WHITE_BALANCE, 0)
    return if (whiteBalance == 1) {
        "Manual"
    } else {
        "Auto"
    }
}

/**
 * Get scene capture type
 */
fun ExifInterface.getSceneCaptureType(): String {
    val sceneType = getAttributeInt(ExifInterface.TAG_SCENE_CAPTURE_TYPE, 0)
    
    return when (sceneType) {
        ExifInterface.SCENE_CAPTURE_TYPE_LANDSCAPE -> "Landscape"
        ExifInterface.SCENE_CAPTURE_TYPE_PORTRAIT -> "Portrait"
        ExifInterface.SCENE_CAPTURE_TYPE_NIGHT -> "Night"
        ExifInterface.SCENE_CAPTURE_TYPE_STANDARD -> "Standard"
        else -> "Unknown"
    }
}

/**
 * Get metering mode
 */
fun ExifInterface.getMeteringMode(): String {
    val meteringMode = getAttributeInt(ExifInterface.TAG_METERING_MODE, 0)
    
    return when (meteringMode) {
        ExifInterface.METERING_MODE_AVERAGE -> "Average"
        ExifInterface.METERING_MODE_CENTER_WEIGHT_AVERAGE -> "Center Weighted Average"
        ExifInterface.METERING_MODE_MULTI_SPOT -> "Multi Spot"
        ExifInterface.METERING_MODE_PARTIAL -> "Partial"
        ExifInterface.METERING_MODE_OTHER -> "Other"
        ExifInterface.METERING_MODE_SPOT -> "Spot"
        ExifInterface.METERING_MODE_UNKNOWN -> "Unknown"
        else -> "Unknown"
    }
}

/**
 * Get exposure program
 */
fun ExifInterface.getExposureProgram(): String {
    val program = getAttributeInt(ExifInterface.TAG_EXPOSURE_PROGRAM, 0)
    
    return when (program) {
        ExifInterface.EXPOSURE_PROGRAM_ACTION -> "Action"
        ExifInterface.EXPOSURE_PROGRAM_APERTURE_PRIORITY -> "Aperture Priority"
        ExifInterface.EXPOSURE_PROGRAM_CREATIVE -> "Creative"
        ExifInterface.EXPOSURE_PROGRAM_LANDSCAPE_MODE -> "Landscape"
        ExifInterface.EXPOSURE_PROGRAM_MANUAL -> "Manual"
        ExifInterface.EXPOSURE_PROGRAM_NORMAL -> "Normal"
        ExifInterface.EXPOSURE_PROGRAM_NOT_DEFINED -> "Not Defined"
        ExifInterface.EXPOSURE_PROGRAM_PORTRAIT_MODE -> "Portrait"
        ExifInterface.EXPOSURE_PROGRAM_SHUTTER_PRIORITY -> "Shutter Priority"
        else -> "Unknown"
    }
}

/**
 * Get image size
 */
fun ExifInterface.getImageSize(): String {
    val width = getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0)
    val height = getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0)
    
    return if (width > 0 && height > 0) {
        "${width}×${height}"
    } else {
        ""
    }
}

/**
 * Get thumbnail size
 */
fun ExifInterface.getThumbnailSize(): String {
    val width = getAttributeInt(ExifInterface.TAG_THUMBNAIL_IMAGE_WIDTH, 0)
    val height = getAttributeInt(ExifInterface.TAG_THUMBNAIL_IMAGE_LENGTH, 0)
    
    return if (width > 0 && height > 0) {
        "${width}×${height}"
    } else {
        ""
    }
}

/**
 * Get orientation as human readable string
 */
fun ExifInterface.getOrientationString(): String {
    val orientation = getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    
    return when (orientation) {
        ExifInterface.ORIENTATION_NORMAL -> "Normal"
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> "Flip Horizontal"
        ExifInterface.ORIENTATION_ROTATE_180 -> "Rotate 180°"
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> "Flip Vertical"
        ExifInterface.ORIENTATION_TRANSPOSE -> "Transpose"
        ExifInterface.ORIENTATION_ROTATE_90 -> "Rotate 90° CW"
        ExifInterface.ORIENTATION_TRANSVERSE -> "Transverse"
        ExifInterface.ORIENTATION_ROTATE_270 -> "Rotate 270° CW"
        else -> "Unknown"
    }
}

/**
 * Get all EXIF info as formatted string
 */
fun ExifInterface.getFormattedInfo(): String {
    val info = mutableListOf<String>()
    
    getCameraInfo().takeIf { it.isNotEmpty() }?.let { info.add("Camera: $it") }
    getExposureInfo().takeIf { it.isNotEmpty() }?.let { info.add("Exposure: $it") }
    getFocalLength().takeIf { it.isNotEmpty() }?.let { info.add("Focal: $it") }
    getFlashInfo().takeIf { it.isNotEmpty() }?.let { info.add("Flash: $it") }
    getWhiteBalanceInfo().takeIf { it.isNotEmpty() }?.let { info.add("White Balance: $it") }
    getSceneCaptureType().takeIf { it.isNotEmpty() }?.let { info.add("Scene: $it") }
    getMeteringMode().takeIf { it.isNotEmpty() }?.let { info.add("Metering: $it") }
    getExposureProgram().takeIf { it.isNotEmpty() }?.let { info.add("Program: $it") }
    getImageSize().takeIf { it.isNotEmpty() }?.let { info.add("Size: $it") }
    getFormattedDateTime().takeIf { it.isNotEmpty() }?.let { info.add("Date: $it") }
    getGpsLocation().takeIf { it.isNotEmpty() }?.let { info.add("GPS: $it") }
    
    return info.joinToString("\n")
}

/**
 * Copy EXIF data from one file to another
 */
fun ExifInterface.copyTo(destFile: File) {
    try {
        val destExif = ExifInterface(destFile.absolutePath)
        
        getAllAttributes().forEach { (tag, value) ->
            try {
                destExif.setAttribute(tag, value)
            } catch (e: Exception) {
                // Ignore
            }
        }
        
        destExif.saveAttributes()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

/**
 * Remove all EXIF data
 */
fun ExifInterface.removeAllAttributes() {
    val tagsToRemove = getAllAttributes().keys
    tagsToRemove.forEach { tag ->
        try {
            setAttribute(tag, null)
        } catch (e: Exception) {
            // Ignore
        }
    }
    saveAttributes()
}

/**
 * Remove sensitive EXIF data (GPS, camera info, etc.)
 */
fun ExifInterface.removeSensitiveInfo() {
    val sensitiveTags = listOf(
        ExifInterface.TAG_GPS_LATITUDE,
        ExifInterface.TAG_GPS_LATITUDE_REF,
        ExifInterface.TAG_GPS_LONGITUDE,
        ExifInterface.TAG_GPS_LONGITUDE_REF,
        ExifInterface.TAG_GPS_ALTITUDE,
        ExifInterface.TAG_GPS_ALTITUDE_REF,
        ExifInterface.TAG_GPS_TIMESTAMP,
        ExifInterface.TAG_GPS_DATESTAMP,
        ExifInterface.TAG_GPS_PROCESSING_METHOD,
        ExifInterface.TAG_MAKE,
        ExifInterface.TAG_MODEL,
        ExifInterface.TAG_SOFTWARE,
        ExifInterface.TAG_ARTIST,
        ExifInterface.TAG_COPYRIGHT,
        ExifInterface.TAG_IMAGE_UNIQUE_ID
    )
    
    sensitiveTags.forEach { tag ->
        try {
            setAttribute(tag, null)
        } catch (e: Exception) {
            // Ignore
        }
    }
    
    saveAttributes()
}

/**
 * Extension functions for File to get ExifInterface
 */
fun File.getExifInterface(): ExifInterface? {
    return try {
        ExifInterface(absolutePath)
    } catch (e: IOException) {
        null
    }
}

/**
 * Check if file has EXIF data
 */
fun File.hasExifData(): Boolean {
    return getExifInterface()?.getAllAttributes()?.isNotEmpty() ?: false
}

/**
 * Get EXIF data as formatted string
 */
fun File.getExifFormattedInfo(): String {
    return getExifInterface()?.getFormattedInfo() ?: "No EXIF data"
}

/**
 * Remove EXIF data from file
 */
fun File.removeExifData(): Boolean {
    return try {
        getExifInterface()?.removeAllAttributes()
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Copy EXIF data from another file
 */
fun File.copyExifFrom(sourceFile: File): Boolean {
    return try {
        val sourceExif = sourceFile.getExifInterface()
        val destExif = getExifInterface()
        
        if (sourceExif != null && destExif != null) {
            sourceExif.getAllAttributes().forEach { (tag, value) ->
                try {
                    destExif.setAttribute(tag, value)
                } catch (e: Exception) {
                    // Ignore
                }
            }
            destExif.saveAttributes()
            true
        } else {
            false
        }
    } catch (e: Exception) {
        false
    }
}
