package com.exory550.exoryfilemanager.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.exory550.exoryfilemanager.R
import java.io.File

class Breadcrumbs @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {

    private val container: LinearLayout
    private var onBreadcrumbClickListener: ((String) -> Unit)? = null
    private var currentPath: String = ""

    init {
        isHorizontalScrollBarEnabled = false
        container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT
            )
        }
        addView(container)
    }

    fun setPath(path: String) {
        currentPath = path
        container.removeAllViews()

        val parts = path.split(File.separator).filter { it.isNotEmpty() }
        var cumulativePath = ""

        addRootBreadcrumb()

        parts.forEachIndexed { index, part ->
            cumulativePath += File.separator + part
            addBreadcrumb(part, cumulativePath, index == parts.size - 1)
        }

        post { fullScroll(FOCUS_RIGHT) }
    }

    private fun addRootBreadcrumb() {
        val rootView = createBreadcrumbView(
            text = context.getString(R.string.root),
            path = File.separator,
            isLast = currentPath == File.separator
        )
        container.addView(rootView)
    }

    private fun addBreadcrumb(text: String, path: String, isLast: Boolean) {
        val separator = createSeparatorView()
        container.addView(separator)

        val breadcrumb = createBreadcrumbView(text, path, isLast)
        container.addView(breadcrumb)
    }

    private fun createBreadcrumbView(text: String, path: String, isLast: Boolean): View {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_breadcrumb, container, false) as TextView

        view.text = text
        view.isSelected = isLast

        if (!isLast) {
            view.setOnClickListener {
                onBreadcrumbClickListener?.invoke(path)
            }
        }

        return view
    }

    private fun createSeparatorView(): View {
        return LayoutInflater.from(context)
            .inflate(R.layout.item_breadcrumb_separator, container, false)
    }

    fun setOnBreadcrumbClickListener(listener: (String) -> Unit) {
        onBreadcrumbClickListener = listener
    }

    fun getCurrentPath(): String = currentPath
}
