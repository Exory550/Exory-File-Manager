package com.exory550.exoryfilemanager.helpers

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.exory550.exoryfilemanager.R
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.getkeepsafe.taptargetview.TapTargetView

class TutorialBuilder(private val activity: Activity) {

    private val targets = mutableListOf<TapTarget>()
    private var onComplete: (() -> Unit)? = null
    private var onCancel: (() -> Unit)? = null

    fun addTarget(
        view: View,
        title: String,
        description: String,
        icon: Int? = null,
        tintTarget: Boolean = true,
        targetRadius: Int = 60
    ): TutorialBuilder {
        val target = TapTarget.forView(view, title, description)
            .targetRadius(targetRadius)
            .tintTarget(tintTarget)
            .transparentTarget(true)
            .cancelable(false)
            .drawShadow(true)
            .id(targets.size)

        icon?.let {
            target.icon(it)
        }

        targets.add(target)
        return this
    }

    fun addTarget(
        viewId: Int,
        title: String,
        description: String,
        icon: Int? = null,
        tintTarget: Boolean = true,
        targetRadius: Int = 60
    ): TutorialBuilder {
        val view = activity.findViewById<View>(viewId) ?: return this
        return addTarget(view, title, description, icon, tintTarget, targetRadius)
    }

    fun addTarget(
        view: View,
        titleRes: Int,
        descriptionRes: Int,
        icon: Int? = null,
        tintTarget: Boolean = true,
        targetRadius: Int = 60
    ): TutorialBuilder {
        return addTarget(
            view,
            activity.getString(titleRes),
            activity.getString(descriptionRes),
            icon,
            tintTarget,
            targetRadius
        )
    }

    fun addTarget(
        viewId: Int,
        titleRes: Int,
        descriptionRes: Int,
        icon: Int? = null,
        tintTarget: Boolean = true,
        targetRadius: Int = 60
    ): TutorialBuilder {
        return addTarget(viewId, activity.getString(titleRes), activity.getString(descriptionRes), icon, tintTarget, targetRadius)
    }

    fun addTargetWithOverlay(
        parent: ViewGroup,
        targetView: View,
        title: String,
        description: String,
        overlayColor: Int = R.color.tutorial_overlay
    ): TutorialBuilder {
        val overlay = FrameLayout(activity).apply {
            setBackgroundColor(ContextCompat.getColor(activity, overlayColor))
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            alpha = 0f
        }
        parent.addView(overlay)

        val target = TapTarget.forView(targetView, title, description)
            .targetRadius(60)
            .tintTarget(true)
            .transparentTarget(true)
            .cancelable(false)
            .outerCircleColor(R.color.tutorial_circle)
            .targetCircleColor(R.color.tutorial_target)

        overlay.animate().alpha(0.5f).duration = 300

        target.id = targets.size
        targets.add(target)

        return this
    }

    fun addSequenceTarget(
        views: List<View>,
        titles: List<String>,
        descriptions: List<String>
    ): TutorialBuilder {
        views.forEachIndexed { index, view ->
            if (index < titles.size && index < descriptions.size) {
                addTarget(view, titles[index], descriptions[index])
            }
        }
        return this
    }

    fun onComplete(listener: () -> Unit): TutorialBuilder {
        onComplete = listener
        return this
    }

    fun onCancel(listener: () -> Unit): TutorialBuilder {
        onCancel = listener
        return this
    }

    fun build(): TapTargetSequence {
        val sequence = TapTargetSequence(activity)
            .targets(*targets.toTypedArray())
            .listener(object : TapTargetSequence.Listener {
                override fun onSequenceFinish() {
                    onComplete?.invoke()
                }

                override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {
                }

                override fun onSequenceCanceled(lastTarget: TapTarget?) {
                    onCancel?.invoke()
                }
            })
            .considerOuterCircleCancel(false)
            .continueOnCancel(false)

        return sequence
    }

    fun show() {
        if (targets.isNotEmpty()) {
            build().start()
        }
    }

    fun clear() {
        targets.clear()
        onComplete = null
        onCancel = null
    }

    companion object {
        fun with(activity: Activity): TutorialBuilder {
            return TutorialBuilder(activity)
        }

        fun showTutorial(
            activity: Activity,
            vararg steps: Pair<View, Pair<String, String>>
        ) {
            val builder = TutorialBuilder(activity)
            steps.forEach { (view, texts) ->
                builder.addTarget(view, texts.first, texts.second)
            }
            builder.show()
        }

        fun showTutorial(
            activity: Activity,
            vararg steps: Pair<Int, Pair<Int, Int>>
        ) {
            val builder = TutorialBuilder(activity)
            steps.forEach { (viewId, textRes) ->
                builder.addTarget(viewId, textRes.first, textRes.second)
            }
            builder.show()
        }

        fun createHomeTutorial(activity: Activity): TutorialBuilder {
            return TutorialBuilder(activity)
                .addTarget(R.id.toolbar, R.string.tutorial_toolbar_title, R.string.tutorial_toolbar_desc)
                .addTarget(R.id.fab_add, R.string.tutorial_fab_title, R.string.tutorial_fab_desc)
                .addTarget(R.id.navigation_view, R.string.tutorial_nav_title, R.string.tutorial_nav_desc)
                .addTarget(R.id.recyclerView, R.string.tutorial_files_title, R.string.tutorial_files_desc)
        }

        fun createSearchTutorial(activity: Activity): TutorialBuilder {
            return TutorialBuilder(activity)
                .addTarget(R.id.action_search, R.string.tutorial_search_title, R.string.tutorial_search_desc)
                .addTarget(R.id.search_view, R.string.tutorial_search_input_title, R.string.tutorial_search_input_desc)
                .addTarget(R.id.recyclerView, R.string.tutorial_search_results_title, R.string.tutorial_search_results_desc)
        }

        fun createSelectionTutorial(activity: Activity): TutorialBuilder {
            return TutorialBuilder(activity)
                .addTarget(R.id.recyclerView, R.string.tutorial_selection_title, R.string.tutorial_selection_desc)
                .addTarget(R.id.action_copy, R.string.tutorial_copy_title, R.string.tutorial_copy_desc)
                .addTarget(R.id.action_cut, R.string.tutorial_cut_title, R.string.tutorial_cut_desc)
                .addTarget(R.id.action_delete, R.string.tutorial_delete_title, R.string.tutorial_delete_desc)
                .addTarget(R.id.action_share, R.string.tutorial_share_title, R.string.tutorial_share_desc)
        }
    }
}
