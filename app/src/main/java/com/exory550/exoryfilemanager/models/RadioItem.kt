package com.exory550.exoryfilemanager.models

import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.DrawableRes

data class RadioItem(
    val id: Long,
    val title: String,
    val description: String? = null,
    @DrawableRes val iconRes: Int? = null,
    val iconDrawable: Drawable? = null,
    val isEnabled: Boolean = true,
    val isSelected: Boolean = false,
    val tag: Any? = null,
    val groupId: Int = 0
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString(),
        if (parcel.readInt() != 0) parcel.readInt() else null,
        null,
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readValue(Any::class.java.classLoader),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeInt(if (iconRes != null) 1 else 0)
        iconRes?.let { parcel.writeInt(it) }
        parcel.writeByte(if (isEnabled) 1 else 0)
        parcel.writeByte(if (isSelected) 1 else 0)
        parcel.writeValue(tag)
        parcel.writeInt(groupId)
    }

    override fun describeContents(): Int = 0

    fun copy(
        id: Long = this.id,
        title: String = this.title,
        description: String? = this.description,
        iconRes: Int? = this.iconRes,
        iconDrawable: Drawable? = this.iconDrawable,
        isEnabled: Boolean = this.isEnabled,
        isSelected: Boolean = this.isSelected,
        tag: Any? = this.tag,
        groupId: Int = this.groupId
    ): RadioItem {
        return RadioItem(
            id, title, description, iconRes, iconDrawable,
            isEnabled, isSelected, tag, groupId
        )
    }

    companion object CREATOR : Parcelable.Creator<RadioItem> {
        override fun createFromParcel(parcel: Parcel): RadioItem {
            return RadioItem(parcel)
        }

        override fun newArray(size: Int): Array<RadioItem?> {
            return arrayOfNulls(size)
        }

        fun fromString(title: String, id: Long = title.hashCode().toLong()): RadioItem {
            return RadioItem(
                id = id,
                title = title
            )
        }

        fun fromStrings(titles: List<String>, startId: Long = 0): List<RadioItem> {
            return titles.mapIndexed { index, title ->
                RadioItem(
                    id = startId + index,
                    title = title
                )
            }
        }

        fun fromPairs(pairs: List<Pair<String, Int>>, startId: Long = 0): List<RadioItem> {
            return pairs.mapIndexed { index, (title, icon) ->
                RadioItem(
                    id = startId + index,
                    title = title,
                    iconRes = icon
                )
            }
        }
    }
}
