package com.exory550.exoryfilemanager.extensions

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Extension functions for TextInputLayout
 */

/**
 * Get text from TextInputLayout's EditText
 */
val TextInputLayout.text: String
    get() = editText?.text?.toString() ?: ""

/**
 * Get trimmed text from TextInputLayout's EditText
 */
val TextInputLayout.trimmedText: String
    get() = text.trim()

/**
 * Set text to TextInputLayout's EditText
 */
fun TextInputLayout.setText(text: String?) {
    editText?.setText(text)
}

/**
 * Set text with selection at end
 */
fun TextInputLayout.setTextAndKeepCursor(text: String) {
    editText?.setTextAndKeepCursor(text)
}

/**
 * Append text to TextInputLayout's EditText
 */
fun TextInputLayout.append(text: CharSequence) {
    editText?.append(text)
}

/**
 * Check if TextInputLayout's EditText is empty
 */
val TextInputLayout.isEmpty: Boolean
    get() = text.isEmpty()

/**
 * Check if TextInputLayout's EditText is not empty
 */
val TextInputLayout.isNotEmpty: Boolean
    get() = text.isNotEmpty()

/**
 * Check if TextInputLayout's EditText is blank
 */
val TextInputLayout.isBlank: Boolean
    get() = text.isBlank()

/**
 * Check if TextInputLayout's EditText is not blank
 */
val TextInputLayout.isNotBlank: Boolean
    get() = text.isNotBlank()

/**
 * Clear text in TextInputLayout's EditText
 */
fun TextInputLayout.clear() {
    editText?.text?.clear()
}

/**
 * Select all text in TextInputLayout's EditText
 */
fun TextInputLayout.selectAll() {
    editText?.selectAll()
}

/**
 * Set error with automatic visibility
 */
fun TextInputLayout.setErrorAndShow(error: String?) {
    this.error = error
    isErrorEnabled = error != null
}

/**
 * Clear error and disable error mode
 */
fun TextInputLayout.clearErrorAndDisable() {
    this.error = null
    isErrorEnabled = false
}

/**
 * Set helper text with automatic visibility
 */
fun TextInputLayout.setHelperTextAndShow(helperText: String?) {
    this.helperText = helperText
    isHelperTextEnabled = helperText != null
}

/**
 * Clear helper text and disable
 */
fun TextInputLayout.clearHelperTextAndDisable() {
    this.helperText = null
    isHelperTextEnabled = false
}

/**
 * Set placeholder text with automatic visibility
 */
fun TextInputLayout.setPlaceholderTextAndShow(placeholderText: String?) {
    this.placeholderText = placeholderText
    isPlaceholderEnabled = placeholderText != null
}

/**
 * Clear placeholder text and disable
 */
fun TextInputLayout.clearPlaceholderTextAndDisable() {
    this.placeholderText = null
    isPlaceholderEnabled = false
}

/**
 * Set start icon with tint
 */
fun TextInputLayout.setStartIconWithTint(@DrawableRes iconRes: Int, @ColorInt tintColor: Int? = null) {
    startIconDrawable = context.getDrawableCompat(iconRes)
    tintColor?.let { setStartIconTintList(android.content.res.ColorStateList.valueOf(it)) }
    isStartIconVisible = true
}

/**
 * Set end icon with tint
 */
fun TextInputLayout.setEndIconWithTint(@DrawableRes iconRes: Int, @ColorInt tintColor: Int? = null) {
    endIconDrawable = context.getDrawableCompat(iconRes)
    tintColor?.let { setEndIconTintList(android.content.res.ColorStateList.valueOf(it)) }
    isEndIconVisible = true
}

/**
 * Set end icon mode and tint
 */
fun TextInputLayout.setEndIconModeWithTint(
    @TextInputLayout.EndIconMode endIconMode: Int,
    @ColorInt tintColor: Int? = null
) {
    this.endIconMode = endIconMode
    tintColor?.let { setEndIconTintList(android.content.res.ColorStateList.valueOf(it)) }
}

/**
 * Set password visibility toggle with tint
 */
fun TextInputLayout.setPasswordVisibilityToggleWithTint(@ColorInt tintColor: Int? = null) {
    isPasswordVisibilityToggleEnabled = true
    tintColor?.let { setPasswordVisibilityToggleTintList(android.content.res.ColorStateList.valueOf(it)) }
}

/**
 * Add text changed listener
 */
inline fun TextInputLayout.onTextChanged(crossinline listener: (String) -> Unit) {
    editText?.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            listener(s?.toString() ?: "")
        }
    })
}

/**
 * Add text changed listener with debounce
 */
inline fun TextInputLayout.onTextChangedDebounced(
    debounceMs: Long = 500,
    crossinline listener: (String) -> Unit
) {
    var handler = android.os.Handler(android.os.Looper.getMainLooper())
    var runnable: Runnable? = null
    
    editText?.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            runnable?.let { handler.removeCallbacks(it) }
            runnable = Runnable {
                listener(s?.toString() ?: "")
            }
            handler.postDelayed(runnable!!, debounceMs)
        }
    })
}

/**
 * Validate with multiple rules
 */
fun TextInputLayout.validate(vararg rules: ValidationRule): ValidationResult {
    val text = trimmedText
    for (rule in rules) {
        val result = rule.validate(text)
        if (!result.isValid) {
            setErrorAndShow(result.errorMessage)
            return result
        }
    }
    clearErrorAndDisable()
    return ValidationResult(true)
}

/**
 * Validation result class
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

/**
 * Validation rule interface
 */
interface ValidationRule {
    fun validate(input: String): ValidationResult
}

/**
 * Required field rule
 */
class RequiredRule(private val errorMessage: String = "Field is required") : ValidationRule {
    override fun validate(input: String): ValidationResult {
        return if (input.isNotEmpty()) {
            ValidationResult(true)
        } else {
            ValidationResult(false, errorMessage)
        }
    }
}

/**
 * Minimum length rule
 */
class MinLengthRule(
    private val minLength: Int,
    private val errorMessage: String = "Minimum length is $minLength"
) : ValidationRule {
    override fun validate(input: String): ValidationResult {
        return if (input.length >= minLength) {
            ValidationResult(true)
        } else {
            ValidationResult(false, errorMessage)
        }
    }
}

/**
 * Maximum length rule
 */
class MaxLengthRule(
    private val maxLength: Int,
    private val errorMessage: String = "Maximum length is $maxLength"
) : ValidationRule {
    override fun validate(input: String): ValidationResult {
        return if (input.length <= maxLength) {
            ValidationResult(true)
        } else {
            ValidationResult(false, errorMessage)
        }
    }
}

/**
 * Email rule
 */
class EmailRule(private val errorMessage: String = "Invalid email address") : ValidationRule {
    override fun validate(input: String): ValidationResult {
        return if (input.isValidEmail()) {
            ValidationResult(true)
        } else {
            ValidationResult(false, errorMessage)
        }
    }
}

/**
 * Phone rule
 */
class PhoneRule(private val errorMessage: String = "Invalid phone number") : ValidationRule {
    override fun validate(input: String): ValidationResult {
        return if (input.isValidPhone()) {
            ValidationResult(true)
        } else {
            ValidationResult(false, errorMessage)
        }
    }
}

/**
 * Regex rule
 */
class RegexRule(
    private val pattern: Regex,
    private val errorMessage: String = "Invalid format"
) : ValidationRule {
    override fun validate(input: String): ValidationResult {
        return if (pattern.matches(input)) {
            ValidationResult(true)
        } else {
            ValidationResult(false, errorMessage)
        }
    }
}

/**
 * Get EditText from TextInputLayout safely
 */
val TextInputLayout.editTextSafely: TextInputEditText?
    get() = editText as? TextInputEditText

/**
 * Check if TextInputLayout has focus
 */
val TextInputLayout.hasFocus: Boolean
    get() = editText?.hasFocus() == true

/**
 * Request focus for TextInputLayout's EditText
 */
fun TextInputLayout.requestFocusAndShowKeyboard() {
    editText?.let {
        it.requestFocus()
        it.showKeyboard()
    }
}

/**
 * Clear focus for TextInputLayout's EditText
 */
fun TextInputLayout.clearFocusAndHideKeyboard() {
    editText?.let {
        it.clearFocus()
        it.hideKeyboard()
    }
}

/**
 * Set counter max length
 */
fun TextInputLayout.setCounterMaxLength(maxLength: Int) {
    counterMaxLength = maxLength
    isCounterEnabled = true
}

/**
 * Enable/disable counter
 */
fun TextInputLayout.setCounterEnabled(enabled: Boolean, maxLength: Int? = null) {
    isCounterEnabled = enabled
    maxLength?.let { counterMaxLength = it }
}

/**
 * Set box background color
 */
fun TextInputLayout.setBoxBackgroundColor(@ColorInt color: Int) {
    setBoxBackgroundColorResource(android.R.color.transparent)
    boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_FILLED
    // Note: This is a simplified version. For actual implementation,
    // you might need to use a ColorStateList or custom background
}

/**
 * Set box stroke color
 */
fun TextInputLayout.setBoxStrokeColor(@ColorInt color: Int) {
    defaultBoxStrokeColor = color
    boxStrokeColor = color
}

/**
 * Set focused box stroke color
 */
fun TextInputLayout.setFocusedBoxStrokeColor(@ColorInt color: Int) {
    setBoxStrokeColorStateList(android.content.res.ColorStateList.valueOf(color))
}

/**
 * Set hint text appearance
 */
fun TextInputLayout.setHintTextAppearance(@androidx.annotation.StyleRes styleRes: Int) {
    setHintTextAppearance(styleRes)
}

/**
 * Set helper text appearance
 */
fun TextInputLayout.setHelperTextAppearance(@androidx.annotation.StyleRes styleRes: Int) {
    setHelperTextAppearance(styleRes)
}

/**
 * Set error text appearance
 */
fun TextInputLayout.setErrorTextAppearance(@androidx.annotation.StyleRes styleRes: Int) {
    setErrorTextAppearance(styleRes)
}

/**
 * Set prefix text with color
 */
fun TextInputLayout.setPrefixTextWithColor(prefix: String?, @ColorInt color: Int? = null) {
    prefixText = prefix
    color?.let { setPrefixTextColor(android.content.res.ColorStateList.valueOf(it)) }
}

/**
 * Set suffix text with color
 */
fun TextInputLayout.setSuffixTextWithColor(suffix: String?, @ColorInt color: Int? = null) {
    suffixText = suffix
    color?.let { setSuffixTextColor(android.content.res.ColorStateList.valueOf(it)) }
}

/**
 * Enable/disable expandable hint
 */
fun TextInputLayout.setExpandableHintEnabled(enabled: Boolean) {
    isExpandedHintEnabled = enabled
}

/**
 * Get character count
 */
val TextInputLayout.characterCount: Int
    get() = text.length

/**
 * Get remaining characters (if counter is enabled)
 */
val TextInputLayout.remainingCharacters: Int
    get() = if (isCounterEnabled) counterMaxLength - characterCount else 0

/**
 * Check if input is valid
 */
fun TextInputLayout.isValid(vararg rules: ValidationRule): Boolean {
    return validate(*rules).isValid
}

/**
 * Clear all errors and helpers
 */
fun TextInputLayout.clearAllMessages() {
    clearErrorAndDisable()
    clearHelperTextAndDisable()
    clearPlaceholderTextAndDisable()
}
