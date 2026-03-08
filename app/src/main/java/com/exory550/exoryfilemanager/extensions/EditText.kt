package com.exory550.exoryfilemanager.extensions

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Extension functions for EditText and TextInputEditText
 */

/**
 * Get text from EditText safely
 */
val EditText.safeText: String
    get() = text?.toString() ?: ""

/**
 * Get trimmed text from EditText
 */
val EditText.trimmedText: String
    get() = safeText.trim()

/**
 * Check if EditText is empty
 */
val EditText.isEmpty: Boolean
    get() = safeText.isEmpty()

/**
 * Check if EditText is not empty
 */
val EditText.isNotEmpty: Boolean
    get() = safeText.isNotEmpty()

/**
 * Check if EditText is blank
 */
val EditText.isBlank: Boolean
    get() = safeText.isBlank()

/**
 * Check if EditText is not blank
 */
val EditText.isNotBlank: Boolean
    get() = safeText.isNotBlank()

/**
 * Set text safely (handles null)
 */
fun EditText.setTextSafely(text: String?) {
    setText(text ?: "")
}

/**
 * Set text and keep cursor at end
 */
fun EditText.setTextAndKeepCursor(text: String) {
    val selection = selectionEnd
    setText(text)
    if (selection in 0 until text.length) {
        setSelection(selection)
    } else {
        setSelection(text.length)
    }
}

/**
 * Append text and keep cursor at end
 */
fun EditText.appendAndKeepCursor(text: CharSequence) {
    val start = length()
    append(text)
    setSelection(start + text.length)
}

/**
 * Clear text
 */
fun EditText.clear() {
    text?.clear()
}

/**
 * Select all text
 */
fun EditText.selectAllText() {
    selectAll()
}

/**
 * Get selected text
 */
val EditText.selectedText: String
    get() = text?.subSequence(selectionStart, selectionEnd)?.toString() ?: ""

/**
 * Replace selected text
 */
fun EditText.replaceSelectedText(replacement: String) {
    val start = selectionStart
    val end = selectionEnd
    if (start >= 0 && end >= 0) {
        text?.replace(minOf(start, end), maxOf(start, end), replacement)
    }
}

/**
 * Show keyboard
 */
fun EditText.showKeyboard() {
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

/**
 * Hide keyboard
 */
fun EditText.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
    clearFocus()
}

/**
 * Set input type for numbers only
 */
fun EditText.setNumbersOnly(decimalAllowed: Boolean = false) {
    inputType = if (decimalAllowed) {
        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
    } else {
        InputType.TYPE_CLASS_NUMBER
    }
}

/**
 * Set input type for email
 */
fun EditText.setEmailInput() {
    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
}

/**
 * Set input type for password
 */
fun EditText.setPasswordInput() {
    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
    transformationMethod = PasswordTransformationMethod.getInstance()
}

/**
 * Set input type for visible password
 */
fun EditText.setVisiblePasswordInput() {
    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
}

/**
 * Set input type for phone
 */
fun EditText.setPhoneInput() {
    inputType = InputType.TYPE_CLASS_PHONE
}

/**
 * Set max length for input
 */
fun EditText.setMaxLength(maxLength: Int) {
    filters = arrayOf(InputFilter.LengthFilter(maxLength))
}

/**
 * Add text changed listener
 */
inline fun EditText.onTextChanged(crossinline listener: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
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
inline fun EditText.onTextChangedDebounced(
    debounceMs: Long = 500,
    crossinline listener: (String) -> Unit
) {
    var handler = android.os.Handler(android.os.Looper.getMainLooper())
    var runnable: Runnable? = null
    
    addTextChangedListener(object : TextWatcher {
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
 * Add editor action listener
 */
inline fun EditText.onEditorAction(crossinline listener: (actionId: Int) -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        listener(actionId)
        true
    }
}

/**
 * Handle IME action done
 */
inline fun EditText.onDone(crossinline listener: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            listener()
            true
        } else {
            false
        }
    }
}

/**
 * Handle IME action next
 */
inline fun EditText.onNext(crossinline listener: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            listener()
            true
        } else {
            false
        }
    }
}

/**
 * Handle IME action search
 */
inline fun EditText.onSearch(crossinline listener: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            hideKeyboard()
            listener()
            true
        } else {
            false
        }
    }
}

/**
 * Handle back button press
 */
inline fun EditText.onBackPressed(crossinline listener: () -> Unit) {
    setOnKeyListener { _, keyCode, event ->
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            listener()
            true
        } else {
            false
        }
    }
}

/**
 * Validate email format
 */
fun EditText.isValidEmail(): Boolean {
    val email = trimmedText
    return email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

/**
 * Validate phone number format
 */
fun EditText.isValidPhone(): Boolean {
    val phone = trimmedText
    return phone.isNotEmpty() && phone.length >= 10 && phone.all { it.isDigit() }
}

/**
 * Validate URL format
 */
fun EditText.isValidUrl(): Boolean {
    val url = trimmedText
    return url.isNotEmpty() && android.util.Patterns.WEB_URL.matcher(url).matches()
}

/**
 * Validate minimum length
 */
fun EditText.hasMinLength(minLength: Int): Boolean {
    return trimmedText.length >= minLength
}

/**
 * Validate maximum length
 */
fun EditText.hasMaxLength(maxLength: Int): Boolean {
    return trimmedText.length <= maxLength
}

/**
 * Validate contains letters
 */
fun EditText.containsLetters(): Boolean {
    return trimmedText.any { it.isLetter() }
}

/**
 * Validate contains numbers
 */
fun EditText.containsNumbers(): Boolean {
    return trimmedText.any { it.isDigit() }
}

/**
 * Validate contains uppercase
 */
fun EditText.containsUppercase(): Boolean {
    return trimmedText.any { it.isUpperCase() }
}

/**
 * Validate contains lowercase
 */
fun EditText.containsLowercase(): Boolean {
    return trimmedText.any { it.isLowerCase() }
}

/**
 * Validate contains special characters
 */
fun EditText.containsSpecialCharacters(): Boolean {
    return trimmedText.any { !it.isLetterOrDigit() }
}

/**
 * Set error with color
 */
fun EditText.setErrorWithColor(error: String?, @ColorInt color: Int) {
    this.error = error
    setTextColor(color)
}

/**
 * Clear error
 */
fun EditText.clearError() {
    this.error = null
}

/**
 * Set drawable start
 */
fun EditText.setDrawableStart(@DrawableRes drawableRes: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableRes)
    setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
}

/**
 * Set drawable end
 */
fun EditText.setDrawableEnd(@DrawableRes drawableRes: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableRes)
    setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
}

/**
 * Set drawable top
 */
fun EditText.setDrawableTop(@DrawableRes drawableRes: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableRes)
    setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
}

/**
 * Set drawable bottom
 */
fun EditText.setDrawableBottom(@DrawableRes drawableRes: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableRes)
    setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawable)
}

/**
 * Extension functions for TextInputLayout
 */

/**
 * Get text from TextInputEditText inside TextInputLayout
 */
val TextInputLayout.text: String
    get() = editText?.safeText ?: ""

/**
 * Set text to TextInputEditText inside TextInputLayout
 */
fun TextInputLayout.setText(text: String?) {
    editText?.setText(text)
}

/**
 * Check if TextInputLayout is empty
 */
val TextInputLayout.isEmpty: Boolean
    get() = editText?.isEmpty ?: true

/**
 * Check if TextInputLayout is not empty
 */
val TextInputLayout.isNotEmpty: Boolean
    get() = editText?.isNotEmpty ?: false

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
 * Add text changed listener to TextInputLayout's EditText
 */
inline fun TextInputLayout.onTextChanged(crossinline listener: (String) -> Unit) {
    editText?.onTextChanged(listener)
}

/**
 * Validate TextInputLayout has minimum length
 */
fun TextInputLayout.hasMinLength(minLength: Int): Boolean {
    return editText?.hasMinLength(minLength) ?: false
}

/**
 * Extension functions for TextView
 */

/**
 * Get text from TextView safely
 */
val TextView.safeText: String
    get() = text?.toString() ?: ""

/**
 * Set text with formatting
 */
fun TextView.setTextWithFormat(format: String, vararg args: Any?) {
    text = String.format(format, *args)
}

/**
 * Set text with resource formatting
 */
fun TextView.setTextWithFormat(formatRes: Int, vararg args: Any?) {
    text = context.getString(formatRes, *args)
}

/**
 * Append line
 */
fun TextView.appendLine(text: CharSequence) {
    append(text)
    append("\n")
}

/**
 * Extension functions for common input validation
 */

/**
 * Validation result class
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

/**
 * Validate EditText with multiple rules
 */
fun EditText.validate(vararg rules: ValidationRule): ValidationResult {
    for (rule in rules) {
        val result = rule.validate(trimmedText)
        if (!result.isValid) {
            return result
        }
    }
    return ValidationResult(true)
}

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
        return if (android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
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
        return if (input.length >= 10 && input.all { it.isDigit() }) {
            ValidationResult(true)
        } else {
            ValidationResult(false, errorMessage)
        }
    }
}

/**
 * Password strength rule
 */
class PasswordStrengthRule(
    private val minStrength: Int = 3,
    private val errorMessage: String = "Password is too weak"
) : ValidationRule {
    override fun validate(input: String): ValidationResult {
        val strength = calculatePasswordStrength(input)
        return if (strength >= minStrength) {
            ValidationResult(true)
        } else {
            ValidationResult(false, errorMessage)
        }
    }
    
    private fun calculatePasswordStrength(password: String): Int {
        var strength = 0
        
        if (password.length >= 8) strength++
        if (password.any { it.isDigit() }) strength++
        if (password.any { it.isLowerCase() }) strength++
        if (password.any { it.isUpperCase() }) strength++
        if (password.any { !it.isLetterOrDigit() }) strength++
        
        return strength
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
