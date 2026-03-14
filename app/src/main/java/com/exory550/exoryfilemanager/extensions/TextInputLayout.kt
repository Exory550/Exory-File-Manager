package com.exory550.exoryfilemanager.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout

fun TextInputLayout.setHintAnimationEnabled(enabled: Boolean) {
    isHintAnimationEnabled = enabled
}

fun TextInputLayout.setEndIconOnClick(listener: View.OnClickListener) {
    setEndIconOnClickListener(listener)
}

fun TextInputLayout.setStartIconDrawable(drawable: Drawable?) {
    startIconDrawable = drawable
}

fun TextInputLayout.setEndIconDrawable(drawable: Drawable?) {
    endIconDrawable = drawable
}
