package com.exory550.exoryfilemanager.models

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.DrawableRes

sealed class ListItem : Parcelable {
    abstract val id: Long
    abstract val title: String
    abstract val subtitle: String?
    abstract val iconRes: Int?
    abstract val iconUrl: String?
    abstract val isEnabled: Boolean
    abstract val isSelected: Boolean
    abstract val tag: Any?

    data class SimpleItem(
        override val id: Long,
        override val title: String,
        override val subtitle: String? = null,
        @DrawableRes override val iconRes: Int? = null,
        override val iconUrl: String? = null,
        override val isEnabled: Boolean = true,
        override val isSelected: Boolean = false,
        override val tag: Any? = null
    ) : ListItem() {
        constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString() ?: "",
            parcel.readString(),
            if (parcel.readInt() != 0) parcel.readInt() else null,
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            parcel.readValue(Any::class.java.classLoader)
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeLong(id)
            parcel.writeString(title)
            parcel.writeString(subtitle)
            parcel.writeInt(if (iconRes != null) 1 else 0)
            iconRes?.let { parcel.writeInt(it) }
            parcel.writeString(iconUrl)
            parcel.writeByte(if (isEnabled) 1 else 0)
            parcel.writeByte(if (isSelected) 1 else 0)
            parcel.writeValue(tag)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<SimpleItem> {
            override fun createFromParcel(parcel: Parcel): SimpleItem = SimpleItem(parcel)
            override fun newArray(size: Int): Array<SimpleItem?> = arrayOfNulls(size)
        }
    }

    data class HeaderItem(
        override val id: Long,
        override val title: String,
        override val subtitle: String? = null,
        override val iconRes: Int? = null,
        override val iconUrl: String? = null,
        val showDivider: Boolean = true,
        override val isEnabled: Boolean = true,
        override val isSelected: Boolean = false,
        override val tag: Any? = null
    ) : ListItem() {
        constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString() ?: "",
            parcel.readString(),
            if (parcel.readInt() != 0) parcel.readInt() else null,
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            parcel.readValue(Any::class.java.classLoader)
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeLong(id)
            parcel.writeString(title)
            parcel.writeString(subtitle)
            parcel.writeInt(if (iconRes != null) 1 else 0)
            iconRes?.let { parcel.writeInt(it) }
            parcel.writeString(iconUrl)
            parcel.writeByte(if (showDivider) 1 else 0)
            parcel.writeByte(if (isEnabled) 1 else 0)
            parcel.writeByte(if (isSelected) 1 else 0)
            parcel.writeValue(tag)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<HeaderItem> {
            override fun createFromParcel(parcel: Parcel): HeaderItem = HeaderItem(parcel)
            override fun newArray(size: Int): Array<HeaderItem?> = arrayOfNulls(size)
        }
    }

    data class DividerItem(
        override val id: Long,
        val titleText: String? = null,
        override val isEnabled: Boolean = true,
        override val isSelected: Boolean = false,
        override val tag: Any? = null
    ) : ListItem() {
        override val title: String get() = titleText ?: ""
        override val subtitle: String? = null
        override val iconRes: Int? = null
        override val iconUrl: String? = null

        constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            parcel.readValue(Any::class.java.classLoader)
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeLong(id)
            parcel.writeString(title)
            parcel.writeByte(if (isEnabled) 1 else 0)
            parcel.writeByte(if (isSelected) 1 else 0)
            parcel.writeValue(tag)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<DividerItem> {
            override fun createFromParcel(parcel: Parcel): DividerItem = DividerItem(parcel)
            override fun newArray(size: Int): Array<DividerItem?> = arrayOfNulls(size)
        }
    }

    data class LoadingItem(
        override val id: Long,
        val message: String? = null,
        override val isEnabled: Boolean = true,
        override val isSelected: Boolean = false,
        override val tag: Any? = null
    ) : ListItem() {
        override val title: String = message ?: ""
        override val subtitle: String? = null
        override val iconRes: Int? = null
        override val iconUrl: String? = null

        constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            parcel.readValue(Any::class.java.classLoader)
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeLong(id)
            parcel.writeString(message)
            parcel.writeByte(if (isEnabled) 1 else 0)
            parcel.writeByte(if (isSelected) 1 else 0)
            parcel.writeValue(tag)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<LoadingItem> {
            override fun createFromParcel(parcel: Parcel): LoadingItem = LoadingItem(parcel)
            override fun newArray(size: Int): Array<LoadingItem?> = arrayOfNulls(size)
        }
    }
}
