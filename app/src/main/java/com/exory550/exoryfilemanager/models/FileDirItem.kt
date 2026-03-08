package com.exory550.exoryfilemanager.models

import android.os.Parcel
import android.os.Parcelable
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class FileDirItem(
    val path: String,
    val name: String,
    val size: Long,
    val lastModified: Long,
    val isDirectory: Boolean,
    val isHidden: Boolean = false,
    val isSelected: Boolean = false,
    val isCut: Boolean = false,
    val permissions: String? = null,
    val owner: String? = null,
    val group: String? = null,
    val mimeType: String? = null,
    val thumbnailPath: String? = null,
    val itemCount: Int = 0,
    val containsMedia: Boolean = false,
    val isEncrypted: Boolean = false,
    val isFavorite: Boolean = false,
    val tags: List<String> = emptyList(),
    val dateAdded: Long = System.currentTimeMillis()
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.createStringArrayList() ?: emptyList(),
        parcel.readLong()
    )

    val extension: String
        get() = if (isDirectory) "" else name.substringAfterLast(".", "").lowercase(Locale.US)

    val isFile: Boolean
        get() = !isDirectory

    val isImage: Boolean
        get() = !isDirectory && extension in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif")

    val isVideo: Boolean
        get() = !isDirectory && extension in listOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "3gp", "mpeg", "mpg")

    val isAudio: Boolean
        get() = !isDirectory && extension in listOf("mp3", "wav", "ogg", "m4a", "aac", "flac", "wma", "ape", "ac3", "dts")

    val isDocument: Boolean
        get() = !isDirectory && extension in listOf("txt", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt", "ods", "odp", "rtf", "md", "csv", "tsv")

    val isArchive: Boolean
        get() = !isDirectory && extension in listOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz", "z", "arj", "cab", "iso")

    val isApk: Boolean
        get() = !isDirectory && extension == "apk"

    val isCode: Boolean
        get() = !isDirectory && extension in listOf("java", "kt", "kts", "xml", "json", "yml", "yaml", "properties", "gradle", "c", "cpp", "h", "hpp", "cs", "php", "js", "ts", "py", "rb", "go", "rs", "swift", "html", "css", "scss", "less")

    val isLog: Boolean
        get() = !isDirectory && extension in listOf("log", "logs", "trace", "debug")

    val parentPath: String
        get() = File(path).parent ?: ""

    val nameWithoutExtension: String
        get() = name.substringBeforeLast(".", name)

    val formattedSize: String
        get() = size.toFormattedFileSize()

    val formattedDate: String
        get() = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(lastModified))

    val formattedDateShort: String
        get() = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(lastModified))

    val formattedTime: String
        get() = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(lastModified))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(path)
        parcel.writeString(name)
        parcel.writeLong(size)
        parcel.writeLong(lastModified)
        parcel.writeByte(if (isDirectory) 1 else 0)
        parcel.writeByte(if (isHidden) 1 else 0)
        parcel.writeByte(if (isSelected) 1 else 0)
        parcel.writeByte(if (isCut) 1 else 0)
        parcel.writeString(permissions)
        parcel.writeString(owner)
        parcel.writeString(group)
        parcel.writeString(mimeType)
        parcel.writeString(thumbnailPath)
        parcel.writeInt(itemCount)
        parcel.writeByte(if (containsMedia) 1 else 0)
        parcel.writeByte(if (isEncrypted) 1 else 0)
        parcel.writeByte(if (isFavorite) 1 else 0)
        parcel.writeStringList(tags)
        parcel.writeLong(dateAdded)
    }

    override fun describeContents(): Int = 0

    fun copy(
        path: String = this.path,
        name: String = this.name,
        size: Long = this.size,
        lastModified: Long = this.lastModified,
        isDirectory: Boolean = this.isDirectory,
        isHidden: Boolean = this.isHidden,
        isSelected: Boolean = this.isSelected,
        isCut: Boolean = this.isCut,
        permissions: String? = this.permissions,
        owner: String? = this.owner,
        group: String? = this.group,
        mimeType: String? = this.mimeType,
        thumbnailPath: String? = this.thumbnailPath,
        itemCount: Int = this.itemCount,
        containsMedia: Boolean = this.containsMedia,
        isEncrypted: Boolean = this.isEncrypted,
        isFavorite: Boolean = this.isFavorite,
        tags: List<String> = this.tags,
        dateAdded: Long = this.dateAdded
    ): FileDirItem {
        return FileDirItem(
            path, name, size, lastModified, isDirectory, isHidden, isSelected, isCut,
            permissions, owner, group, mimeType, thumbnailPath, itemCount, containsMedia,
            isEncrypted, isFavorite, tags, dateAdded
        )
    }

    companion object CREATOR : Parcelable.Creator<FileDirItem> {
        override fun createFromParcel(parcel: Parcel): FileDirItem {
            return FileDirItem(parcel)
        }

        override fun newArray(size: Int): Array<FileDirItem?> {
            return arrayOfNulls(size)
        }

        fun fromFile(file: File): FileDirItem {
            return FileDirItem(
                path = file.absolutePath,
                name = file.name,
                size = if (file.isFile) file.length() else 0,
                lastModified = file.lastModified(),
                isDirectory = file.isDirectory,
                isHidden = file.isHidden
            )
        }

        fun fromFile(file: File, isSelected: Boolean): FileDirItem {
            return fromFile(file).copy(isSelected = isSelected)
        }
    }
}

fun Long.toFormattedFileSize(): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB")
    var size = this.toDouble()
    var unitIndex = 0

    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }

    return String.format("%.1f %s", size, units[unitIndex])
}
