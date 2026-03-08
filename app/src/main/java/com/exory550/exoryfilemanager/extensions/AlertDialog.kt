package com.exory550.exoryfilemanager.extensions

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.exory550.exoryfilemanager.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Extension functions for creating and showing dialogs
 */

// ============== AlertDialog Builder Extensions ==============

fun Context.showAlertDialog(
    title: String? = null,
    message: String? = null,
    positiveButton: String = getString(android.R.string.ok),
    negativeButton: String? = null,
    neutralButton: String? = null,
    cancelable: Boolean = true,
    icon: Drawable? = null,
    onPositive: ((DialogInterface) -> Unit)? = null,
    onNegative: ((DialogInterface) -> Unit)? = null,
    onNeutral: ((DialogInterface) -> Unit)? = null,
    onCancel: ((DialogInterface) -> Unit)? = null,
    onDismiss: ((DialogInterface) -> Unit)? = null
): AlertDialog {
    return MaterialAlertDialogBuilder(this).apply {
        title?.let { setTitle(it) }
        message?.let { setMessage(it) }
        setCancelable(cancelable)
        icon?.let { setIcon(it) }
        
        setPositiveButton(positiveButton) { dialog, _ -> onPositive?.invoke(dialog) }
        negativeButton?.let { setNegativeButton(it) { dialog, _ -> onNegative?.invoke(dialog) } }
        neutralButton?.let { setNeutralButton(it) { dialog, _ -> onNeutral?.invoke(dialog) } }
        
        setOnCancelListener { onCancel?.invoke(it) }
        setOnDismissListener { onDismiss?.invoke(it) }
    }.show()
}

fun Context.showAlertDialog(
    @StringRes titleRes: Int? = null,
    @StringRes messageRes: Int? = null,
    @StringRes positiveButtonRes: Int = android.R.string.ok,
    @StringRes negativeButtonRes: Int? = null,
    @StringRes neutralButtonRes: Int? = null,
    cancelable: Boolean = true,
    @DrawableRes iconRes: Int? = null,
    onPositive: ((DialogInterface) -> Unit)? = null,
    onNegative: ((DialogInterface) -> Unit)? = null,
    onNeutral: ((DialogInterface) -> Unit)? = null,
    onCancel: ((DialogInterface) -> Unit)? = null,
    onDismiss: ((DialogInterface) -> Unit)? = null
): AlertDialog {
    return MaterialAlertDialogBuilder(this).apply {
        titleRes?.let { setTitle(it) }
        messageRes?.let { setMessage(it) }
        setCancelable(cancelable)
        iconRes?.let { setIcon(it) }
        
        setPositiveButton(positiveButtonRes) { dialog, _ -> onPositive?.invoke(dialog) }
        negativeButtonRes?.let { setNegativeButton(it) { dialog, _ -> onNegative?.invoke(dialog) } }
        neutralButtonRes?.let { setNeutralButton(it) { dialog, _ -> onNeutral?.invoke(dialog) } }
        
        setOnCancelListener { onCancel?.invoke(it) }
        setOnDismissListener { onDismiss?.invoke(it) }
    }.show()
}

fun Context.showConfirmationDialog(
    title: String,
    message: String,
    positiveButton: String = getString(R.string.yes),
    negativeButton: String = getString(R.string.no),
    onConfirm: () -> Unit,
    onCancel: (() -> Unit)? = null
) {
    showAlertDialog(
        title = title,
        message = message,
        positiveButton = positiveButton,
        negativeButton = negativeButton,
        onPositive = { onConfirm() },
        onNegative = { onCancel?.invoke() }
    )
}

fun Context.showInfoDialog(
    title: String,
    message: String,
    buttonText: String = getString(R.string.ok)
) {
    showAlertDialog(
        title = title,
        message = message,
        positiveButton = buttonText
    )
}

fun Context.showErrorDialog(
    message: String,
    buttonText: String = getString(R.string.ok)
) {
    showAlertDialog(
        title = getString(R.string.error),
        message = message,
        positiveButton = buttonText,
        iconRes = R.drawable.ic_error
    )
}

fun Context.showSuccessDialog(
    message: String,
    buttonText: String = getString(R.string.ok)
) {
    showAlertDialog(
        title = getString(R.string.success),
        message = message,
        positiveButton = buttonText,
        iconRes = R.drawable.ic_success
    )
}

fun Context.showWarningDialog(
    message: String,
    buttonText: String = getString(R.string.ok)
) {
    showAlertDialog(
        title = getString(R.string.warning),
        message = message,
        positiveButton = buttonText,
        iconRes = R.drawable.ic_warning
    )
}

// ============== Input Dialog ==============

fun Context.showInputDialog(
    title: String? = null,
    hint: String? = null,
    prefill: String? = null,
    inputType: Int = InputType.TYPE_CLASS_TEXT,
    positiveButton: String = getString(R.string.ok),
    negativeButton: String = getString(R.string.cancel),
    onInput: (String) -> Unit,
    onCancel: (() -> Unit)? = null
) {
    val input = EditText(this).apply {
        this.hint = hint
        setText(prefill)
        this.inputType = inputType
        if (prefill != null) {
            setSelection(prefill.length)
        }
    }
    
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setView(input)
        .setPositiveButton(positiveButton) { _, _ ->
            onInput(input.text.toString())
        }
        .setNegativeButton(negativeButton) { _, _ ->
            onCancel?.invoke()
        }
        .show()
}

fun Context.showTextAreaDialog(
    title: String? = null,
    hint: String? = null,
    prefill: String? = null,
    positiveButton: String = getString(R.string.ok),
    negativeButton: String = getString(R.string.cancel),
    onInput: (String) -> Unit,
    onCancel: (() -> Unit)? = null
) {
    val input = EditText(this).apply {
        this.hint = hint
        setText(prefill)
        if (prefill != null) {
            setSelection(prefill.length)
        }
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        minLines = 3
        maxLines = 5
    }
    
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setView(input)
        .setPositiveButton(positiveButton) { _, _ ->
            onInput(input.text.toString())
        }
        .setNegativeButton(negativeButton) { _, _ ->
            onCancel?.invoke()
        }
        .show()
}

fun Context.showMaterialInputDialog(
    title: String? = null,
    hint: String? = null,
    prefill: String? = null,
    inputType: Int = InputType.TYPE_CLASS_TEXT,
    positiveButton: String = getString(R.string.ok),
    negativeButton: String = getString(R.string.cancel),
    onInput: (String) -> Unit,
    onCancel: (() -> Unit)? = null
) {
    val layout = TextInputLayout(this).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        setPadding(48, 16, 48, 16)
    }
    
    val input = TextInputEditText(this).apply {
        this.hint = hint
        setText(prefill)
        this.inputType = inputType
        if (prefill != null) {
            setSelection(prefill.length)
        }
    }
    
    layout.addView(input)
    
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setView(layout)
        .setPositiveButton(positiveButton) { _, _ ->
            onInput(input.text.toString())
        }
        .setNegativeButton(negativeButton) { _, _ ->
            onCancel?.invoke()
        }
        .show()
}

// ============== List Dialog ==============

fun Context.showListDialog(
    title: String? = null,
    items: Array<String>,
    selectedIndex: Int = -1,
    cancelable: Boolean = true,
    onItemSelected: (Int, String) -> Unit,
    onCancel: (() -> Unit)? = null
) {
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setItems(items) { _, which ->
            onItemSelected(which, items[which])
        }
        .setCancelable(cancelable)
        .setOnCancelListener { onCancel?.invoke() }
        .show()
}

fun Context.showSingleChoiceDialog(
    title: String? = null,
    items: Array<String>,
    selectedIndex: Int = -1,
    positiveButton: String = getString(R.string.ok),
    negativeButton: String = getString(R.string.cancel),
    onItemSelected: (Int, String) -> Unit,
    onCancel: (() -> Unit)? = null
) {
    var selected = selectedIndex
    
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setSingleChoiceItems(items, selectedIndex) { _, which ->
            selected = which
        }
        .setPositiveButton(positiveButton) { _, _ ->
            if (selected >= 0 && selected < items.size) {
                onItemSelected(selected, items[selected])
            }
        }
        .setNegativeButton(negativeButton) { _, _ ->
            onCancel?.invoke()
        }
        .show()
}

fun Context.showMultiChoiceDialog(
    title: String? = null,
    items: Array<String>,
    checkedItems: BooleanArray? = null,
    positiveButton: String = getString(R.string.ok),
    negativeButton: String = getString(R.string.cancel),
    onItemsSelected: (List<Int>, List<String>) -> Unit,
    onCancel: (() -> Unit)? = null
) {
    val checked = checkedItems?.clone() ?: BooleanArray(items.size) { false }
    
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMultiChoiceItems(items, checked) { _, which, isChecked ->
            checked[which] = isChecked
        }
        .setPositiveButton(positiveButton) { _, _ ->
            val selectedIndices = checked.indices.filter { checked[it] }
            val selectedItems = selectedIndices.map { items[it] }
            onItemsSelected(selectedIndices, selectedItems)
        }
        .setNegativeButton(negativeButton) { _, _ ->
            onCancel?.invoke()
        }
        .show()
}

// ============== Custom View Dialog ==============

fun Context.showCustomDialog(
    title: String? = null,
    view: View,
    positiveButton: String? = getString(R.string.ok),
    negativeButton: String? = getString(R.string.cancel),
    neutralButton: String? = null,
    cancelable: Boolean = true,
    onPositive: ((DialogInterface) -> Unit)? = null,
    onNegative: ((DialogInterface) -> Unit)? = null,
    onNeutral: ((DialogInterface) -> Unit)? = null,
    onCancel: (() -> Unit)? = null
): AlertDialog {
    return MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setView(view)
        .apply {
            positiveButton?.let { setPositiveButton(it) { dialog, _ -> onPositive?.invoke(dialog) } }
            negativeButton?.let { setNegativeButton(it) { dialog, _ -> onNegative?.invoke(dialog) } }
            neutralButton?.let { setNeutralButton(it) { dialog, _ -> onNeutral?.invoke(dialog) } }
        }
        .setCancelable(cancelable)
        .setOnCancelListener { onCancel?.invoke() }
        .show()
}

// ============== Progress Dialog ==============

fun Context.showProgressDialog(
    title: String? = null,
    message: String? = null,
    cancelable: Boolean = false
): AlertDialog {
    val progressBar = ProgressBar(this).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        isIndeterminate = true
    }
    
    val layout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = android.view.Gravity.CENTER
        setPadding(48, 24, 48, 24)
        addView(progressBar)
        
        message?.let {
            val textView = TextView(this@showProgressDialog).apply {
                text = it
                textSize = 16f
                setPadding(0, 24, 0, 0)
            }
            addView(textView)
        }
    }
    
    return MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setView(layout)
        .setCancelable(cancelable)
        .show()
}

// ============== Date/Time Picker Dialogs ==============

fun Context.showDatePickerDialog(
    year: Int,
    month: Int,
    dayOfMonth: Int,
    onDateSet: (year: Int, month: Int, dayOfMonth: Int) -> Unit,
    onCancel: (() -> Unit)? = null
) {
    android.app.DatePickerDialog(
        this,
        { _, y, m, d -> onDateSet(y, m, d) },
        year,
        month,
        dayOfMonth
    ).apply {
        setOnCancelListener { onCancel?.invoke() }
        show()
    }
}

fun Context.showTimePickerDialog(
    hourOfDay: Int,
    minute: Int,
    is24HourView: Boolean = true,
    onTimeSet: (hourOfDay: Int, minute: Int) -> Unit,
    onCancel: (() -> Unit)? = null
) {
    android.app.TimePickerDialog(
        this,
        { _, h, m -> onTimeSet(h, m) },
        hourOfDay,
        minute,
        is24HourView
    ).apply {
        setOnCancelListener { onCancel?.invoke() }
        show()
    }
}

// ============== Dialog Extensions ==============

fun Dialog.setWidthPercent(percent: Int) {
    val metrics = context.resources.displayMetrics
    val width = (metrics.widthPixels * (percent / 100.0)).toInt()
    window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
}

fun Dialog.setHeightPercent(percent: Int) {
    val metrics = context.resources.displayMetrics
    val height = (metrics.heightPixels * (percent / 100.0)).toInt()
    window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, height)
}

fun Dialog.setFullScreen() {
    window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
}

fun Dialog.setMaxWidth() {
    val metrics = context.resources.displayMetrics
    val width = (metrics.widthPixels * 0.9).toInt()
    window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
}

fun Dialog.disableCancel() {
    setCancelable(false)
    setCanceledOnTouchOutside(false)
}

fun Dialog.showKeyboard() {
    window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
}

// ============== AlertDialog Builder Extensions ==============

fun AlertDialog.Builder.setupWithViewBinding(
    title: String? = null,
    message: String? = null,
    view: View,
    positiveButton: String? = null,
    negativeButton: String? = null,
    neutralButton: String? = null,
    cancelable: Boolean = true,
    onPositive: ((DialogInterface) -> Unit)? = null,
    onNegative: ((DialogInterface) -> Unit)? = null,
    onNeutral: ((DialogInterface) -> Unit)? = null
): AlertDialog.Builder {
    title?.let { setTitle(it) }
    message?.let { setMessage(it) }
    setView(view)
    positiveButton?.let { setPositiveButton(it, onPositive) }
    negativeButton?.let { setNegativeButton(it, onNegative) }
    neutralButton?.let { setNeutralButton(it, onNeutral) }
    setCancelable(cancelable)
    return this
}

// ============== Extension properties ==============

val Dialog.isShowing: Boolean
    get() = this.isShowing

val Dialog.windowManager: WindowManager?
    get() = window?.windowManager

fun Dialog.dismissSafely() {
    try {
        if (isShowing && !isFinishing()) {
            dismiss()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun Dialog.isFinishing(): Boolean {
    return when (val context = context) {
        is android.app.Activity -> context.isFinishing
        else -> false
    }
}
