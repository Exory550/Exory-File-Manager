package com.exory550.exoryfilemanager.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.exory550.exoryfilemanager.R

/**
 * Generic confirmation dialog with customizable title, message, and actions
 */
class ConfirmationDialog(
    context: Context,
    private val config: Config
) : Dialog(context, R.style.Theme_ExoryFileManager_Dialog) {

    data class Config(
        val title: String? = null,
        val titleRes: Int? = null,
        val message: String? = null,
        val messageRes: Int? = null,
        val icon: Drawable? = null,
        val iconRes: Int? = null,
        val iconColor: Int? = null,
        val positiveText: String = context.getString(R.string.ok),
        val positiveTextRes: Int? = null,
        val negativeText: String? = context.getString(R.string.cancel),
        val negativeTextRes: Int? = null,
        val neutralText: String? = null,
        val neutralTextRes: Int? = null,
        val isCancelable: Boolean = true,
        val cancelOnTouchOutside: Boolean = true,
        val showCheckbox: Boolean = false,
        val checkboxText: String? = null,
        val checkboxTextRes: Int? = null,
        val checkboxChecked: Boolean = false,
        val destructiveAction: Boolean = false,
        val onPositive: (() -> Unit)? = null,
        val onNegative: (() -> Unit)? = null,
        val onNeutral: (() -> Unit)? = null,
        val onCheckboxChanged: ((Boolean) -> Unit)? = null,
        val onCancel: (() -> Unit)? = null,
        val onDismiss: (() -> Unit)? = null
    ) {
        constructor(builder: Builder) : this(
            title = builder.title,
            titleRes = builder.titleRes,
            message = builder.message,
            messageRes = builder.messageRes,
            icon = builder.icon,
            iconRes = builder.iconRes,
            iconColor = builder.iconColor,
            positiveText = builder.positiveText,
            positiveTextRes = builder.positiveTextRes,
            negativeText = builder.negativeText,
            negativeTextRes = builder.negativeTextRes,
            neutralText = builder.neutralText,
            neutralTextRes = builder.neutralTextRes,
            isCancelable = builder.isCancelable,
            cancelOnTouchOutside = builder.cancelOnTouchOutside,
            showCheckbox = builder.showCheckbox,
            checkboxText = builder.checkboxText,
            checkboxTextRes = builder.checkboxTextRes,
            checkboxChecked = builder.checkboxChecked,
            destructiveAction = builder.destructiveAction,
            onPositive = builder.onPositive,
            onNegative = builder.onNegative,
            onNeutral = builder.onNeutral,
            onCheckboxChanged = builder.onCheckboxChanged,
            onCancel = builder.onCancel,
            onDismiss = builder.onDismiss
        )
    }
    
    class Builder(private val context: Context) {
        var title: String? = null
        var titleRes: Int? = null
        var message: String? = null
        var messageRes: Int? = null
        var icon: Drawable? = null
        var iconRes: Int? = null
        var iconColor: Int? = null
        var positiveText: String = context.getString(R.string.ok)
        var positiveTextRes: Int? = null
        var negativeText: String? = context.getString(R.string.cancel)
        var negativeTextRes: Int? = null
        var neutralText: String? = null
        var neutralTextRes: Int? = null
        var isCancelable: Boolean = true
        var cancelOnTouchOutside: Boolean = true
        var showCheckbox: Boolean = false
        var checkboxText: String? = null
        var checkboxTextRes: Int? = null
        var checkboxChecked: Boolean = false
        var destructiveAction: Boolean = false
        var onPositive: (() -> Unit)? = null
        var onNegative: (() -> Unit)? = null
        var onNeutral: (() -> Unit)? = null
        var onCheckboxChanged: ((Boolean) -> Unit)? = null
        var onCancel: (() -> Unit)? = null
        var onDismiss: (() -> Unit)? = null
        
        fun setTitle(title: String): Builder = apply { this.title = title }
        fun setTitleRes(titleRes: Int): Builder = apply { this.titleRes = titleRes }
        fun setMessage(message: String): Builder = apply { this.message = message }
        fun setMessageRes(messageRes: Int): Builder = apply { this.messageRes = messageRes }
        fun setIcon(icon: Drawable): Builder = apply { this.icon = icon }
        fun setIconRes(iconRes: Int): Builder = apply { this.iconRes = iconRes }
        fun setIconColor(color: Int): Builder = apply { this.iconColor = color }
        fun setPositiveText(positiveText: String): Builder = apply { this.positiveText = positiveText }
        fun setPositiveTextRes(positiveTextRes: Int): Builder = apply { this.positiveTextRes = positiveTextRes }
        fun setNegativeText(negativeText: String?): Builder = apply { this.negativeText = negativeText }
        fun setNegativeTextRes(negativeTextRes: Int?): Builder = apply { this.negativeTextRes = negativeTextRes }
        fun setNeutralText(neutralText: String?): Builder = apply { this.neutralText = neutralText }
        fun setNeutralTextRes(neutralTextRes: Int?): Builder = apply { this.neutralTextRes = neutralTextRes }
        fun setCancelable(cancelable: Boolean): Builder = apply { this.isCancelable = cancelable }
        fun setCancelOnTouchOutside(cancelOnTouchOutside: Boolean): Builder = apply { this.cancelOnTouchOutside = cancelOnTouchOutside }
        fun setShowCheckbox(show: Boolean): Builder = apply { this.showCheckbox = show }
        fun setCheckboxText(checkboxText: String): Builder = apply { this.checkboxText = checkboxText }
        fun setCheckboxTextRes(checkboxTextRes: Int): Builder = apply { this.checkboxTextRes = checkboxTextRes }
        fun setCheckboxChecked(checked: Boolean): Builder = apply { this.checkboxChecked = checked }
        fun setDestructiveAction(destructive: Boolean): Builder = apply { this.destructiveAction = destructive }
        fun setOnPositive(listener: () -> Unit): Builder = apply { this.onPositive = listener }
        fun setOnNegative(listener: () -> Unit): Builder = apply { this.onNegative = listener }
        fun setOnNeutral(listener: () -> Unit): Builder = apply { this.onNeutral = listener }
        fun setOnCheckboxChanged(listener: (Boolean) -> Unit): Builder = apply { this.onCheckboxChanged = listener }
        fun setOnCancel(listener: () -> Unit): Builder = apply { this.onCancel = listener }
        fun setOnDismiss(listener: () -> Unit): Builder = apply { this.onDismiss = listener }
        
        fun build(): Config = Config(this)
        fun show() = ConfirmationDialog(context, build()).show()
    }
    
    private lateinit var binding: View
    private lateinit var ivIcon: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvMessage: TextView
    private lateinit var btnPositive: Button
    private lateinit var btnNegative: Button
    private lateinit var btnNeutral: Button
    private lateinit var btnClose: ImageView
    private lateinit var checkbox: android.widget.CheckBox
    private lateinit var checkboxContainer: LinearLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        
        setupViews()
        setupContent()
        setupListeners()
        setupWindowProperties()
    }
    
    private fun setupViews() {
        binding = LayoutInflater.from(context).inflate(R.layout.dialog_confirmation, null)
        setContentView(binding)
        
        ivIcon = binding.findViewById(R.id.ivIcon)
        tvTitle = binding.findViewById(R.id.tvTitle)
        tvMessage = binding.findViewById(R.id.tvMessage)
        btnPositive = binding.findViewById(R.id.btnPositive)
        btnNegative = binding.findViewById(R.id.btnNegative)
        btnNeutral = binding.findViewById(R.id.btnNeutral)
        btnClose = binding.findViewById(R.id.btnClose)
        checkbox = binding.findViewById(R.id.checkbox)
        checkboxContainer = binding.findViewById(R.id.checkboxContainer)
        
        // Set dialog width
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    
    private fun setupContent() {
        // Set title
        when {
            config.titleRes != null -> tvTitle.setText(config.titleRes)
            !config.title.isNullOrBlank() -> tvTitle.text = config.title
            else -> tvTitle.visibility = View.GONE
        }
        
        // Set message
        when {
            config.messageRes != null -> tvMessage.setText(config.messageRes)
            !config.message.isNullOrBlank() -> tvMessage.text = config.message
            else -> tvMessage.visibility = View.GONE
        }
        
        // Set icon
        when {
            config.iconRes != null -> {
                ivIcon.setImageResource(config.iconRes)
                ivIcon.visibility = View.VISIBLE
            }
            config.icon != null -> {
                ivIcon.setImageDrawable(config.icon)
                ivIcon.visibility = View.VISIBLE
            }
            else -> ivIcon.visibility = View.GONE
        }
        
        // Set icon color
        config.iconColor?.let { color ->
            ivIcon.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
        }
        
        // Set positive button
        when {
            config.positiveTextRes != null -> btnPositive.setText(config.positiveTextRes)
            else -> btnPositive.text = config.positiveText
        }
        
        // Set negative button
        if (config.negativeText != null || config.negativeTextRes != null) {
            when {
                config.negativeTextRes != null -> btnNegative.setText(config.negativeTextRes)
                else -> btnNegative.text = config.negativeText
            }
            btnNegative.visibility = View.VISIBLE
        } else {
            btnNegative.visibility = View.GONE
        }
        
        // Set neutral button
        if (config.neutralText != null || config.neutralTextRes != null) {
            when {
                config.neutralTextRes != null -> btnNeutral.setText(config.neutralTextRes)
                else -> btnNeutral.text = config.neutralText
            }
            btnNeutral.visibility = View.VISIBLE
        } else {
            btnNeutral.visibility = View.GONE
        }
        
        // Style for destructive action
        if (config.destructiveAction) {
            btnPositive.setTextColor(
                ContextCompat.getColor(context, R.color.error_color)
            )
        }
        
        // Setup checkbox
        if (config.showCheckbox) {
            checkboxContainer.visibility = View.VISIBLE
            when {
                config.checkboxTextRes != null -> checkbox.setText(config.checkboxTextRes)
                !config.checkboxText.isNullOrBlank() -> checkbox.text = config.checkboxText
            }
            checkbox.isChecked = config.checkboxChecked
        } else {
            checkboxContainer.visibility = View.GONE
        }
        
        // Close button visibility
        btnClose.visibility = if (config.isCancelable) View.VISIBLE else View.GONE
    }
    
    private fun setupListeners() {
        btnPositive.setOnClickListener {
            config.onPositive?.invoke()
            dismiss()
        }
        
        btnNegative.setOnClickListener {
            config.onNegative?.invoke()
            dismiss()
        }
        
        btnNeutral.setOnClickListener {
            config.onNeutral?.invoke()
            dismiss()
        }
        
        btnClose.setOnClickListener {
            dismiss()
        }
        
        checkbox.setOnCheckedChangeListener { _, isChecked ->
            config.onCheckboxChanged?.invoke(isChecked)
        }
        
        setOnCancelListener {
            config.onCancel?.invoke()
        }
        
        setOnDismissListener {
            config.onDismiss?.invoke()
        }
    }
    
    private fun setupWindowProperties() {
        setCancelable(config.isCancelable)
        setCanceledOnTouchOutside(config.cancelOnTouchOutside)
        
        window?.setBackgroundDrawable(
            ContextCompat.getDrawable(context, R.drawable.bg_dialog_rounded)
        )
    }
    
    companion object {
        /**
         * Show a simple confirmation dialog
         */
        fun show(
            context: Context,
            title: String,
            message: String,
            positiveText: String = context.getString(R.string.ok),
            negativeText: String? = context.getString(R.string.cancel),
            onPositive: () -> Unit,
            onNegative: (() -> Unit)? = null
        ) {
            Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveText(positiveText)
                .setNegativeText(negativeText)
                .setOnPositive(onPositive)
                .setOnNegative(onNegative)
                .show()
        }
        
        /**
         * Show a simple confirmation dialog with resource IDs
         */
        fun show(
            context: Context,
            titleRes: Int,
            messageRes: Int,
            positiveTextRes: Int = android.R.string.ok,
            negativeTextRes: Int? = android.R.string.cancel,
            onPositive: () -> Unit,
            onNegative: (() -> Unit)? = null
        ) {
            Builder(context)
                .setTitleRes(titleRes)
                .setMessageRes(messageRes)
                .setPositiveTextRes(positiveTextRes)
                .setNegativeTextRes(negativeTextRes)
                .setOnPositive(onPositive)
                .setOnNegative(onNegative)
                .show()
        }
        
        /**
         * Show a delete confirmation dialog
         */
        fun showDeleteConfirmation(
            context: Context,
            itemName: String,
            onConfirm: () -> Unit,
            onCancel: (() -> Unit)? = null
        ) {
            Builder(context)
                .setTitleRes(R.string.delete_confirmation)
                .setMessage(context.getString(R.string.delete_item_message, itemName))
                .setPositiveTextRes(R.string.delete)
                .setNegativeTextRes(R.string.cancel)
                .setDestructiveAction(true)
                .setIconRes(R.drawable.ic_delete)
                .setOnPositive(onConfirm)
                .setOnNegative(onCancel)
                .show()
        }
        
        /**
         * Show a discard changes confirmation dialog
         */
        fun showDiscardConfirmation(
            context: Context,
            onDiscard: () -> Unit,
            onCancel: (() -> Unit)? = null
        ) {
            Builder(context)
                .setTitleRes(R.string.discard_changes)
                .setMessageRes(R.string.discard_changes_message)
                .setPositiveTextRes(R.string.discard)
                .setNegativeTextRes(R.string.keep_editing)
                .setDestructiveAction(true)
                .setIconRes(R.drawable.ic_warning)
                .setOnPositive(onDiscard)
                .setOnNegative(onCancel)
                .show()
        }
        
        /**
         * Show an exit confirmation dialog
         */
        fun showExitConfirmation(
            context: Context,
            onExit: () -> Unit,
            onCancel: (() -> Unit)? = null
        ) {
            Builder(context)
                .setTitleRes(R.string.exit_app)
                .setMessageRes(R.string.exit_app_message)
                .setPositiveTextRes(R.string.exit)
                .setNegativeTextRes(R.string.cancel)
                .setIconRes(R.drawable.ic_exit)
                .setOnPositive(onExit)
                .setOnNegative(onCancel)
                .show()
        }
    }
}
