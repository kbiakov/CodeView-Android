package io.github.kbiakov.codeview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.RelativeLayout
import io.github.kbiakov.codeview.highlight.ColorTheme
import io.github.kbiakov.codeview.highlight.ColorThemeData
import io.github.kbiakov.codeview.highlight.color
import java.util.*

/**
 * @class CodeView
 *
 * Presents your code content.
 *
 * Before view built or started to, as the first step, placeholder
 * measures & prepare place for code view. Amount of view params is
 * not big, view has mutable state & non-standard initialization behavior.
 * That is why there is no usual & well-known Builder pattern implementation.
 *
 * To control interaction state, being & built, was selected tasks queue.
 * If user has already built view his task performs immediately, otherwise
 * it puts in queue to awaiting adapter creation & processing by build flow.
 * This helps to avoid errors & solve the init tasks in more elegant way.
 *
 * @author Kirill Biakov
 */
class CodeView : RelativeLayout {

    private val vPlaceholder: View
    private val vShadowRight: View
    private val vShadowBottomLine: View
    private val vShadowBottomContent: View

    /**
     * Core view to draw code by lines.
     */
    private val rvCodeContent: RecyclerView

    /**
     * View build tasks queue.
     */
    private val tasks: Queue<() -> Unit>

    /**
     * View state indicates that view in build state, or prepared to build
     * (and awaiting for build) or view was built & code is presented.
     */
    private var state: ViewState

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.layout_code_view, this, true)

        vPlaceholder = findViewById(R.id.v_placeholder)
        vShadowRight = findViewById(R.id.v_shadow_right)
        vShadowBottomLine = findViewById(R.id.v_shadow_bottom_line)
        vShadowBottomContent = findViewById(R.id.v_shadow_bottom_content)

        rvCodeContent = findViewById(R.id.rv_code_content) as RecyclerView
        rvCodeContent.layoutManager = LinearLayoutManager(context)
        rvCodeContent.isNestedScrollingEnabled = true

        tasks = LinkedList()

        state = ViewState.BUILD
    }

    /**
     * Code view states.
     */
    enum class ViewState {
        BUILD,
        PREPARE,
        PRESENTED
    }

    /**
     * Public getter for accessing view state.
     * It may be useful if code view state is unknown.
     * If code view was built it is not safe to use operations chaining.
     */
    fun getState() = state

    /**
     * Accessor/mutator to reduce frequently used actions.
     */
    var adapter: CodeContentAdapter
        get() {
            return rvCodeContent.adapter as CodeContentAdapter
        }
        set(adapter) {
            rvCodeContent.adapter = adapter
            state = ViewState.PRESENTED
        }

    // - Build processor

    /**
     * Add task to build queue. Otherwise (for prepared view) performs
     * task delayed or immediately (for built view).
     * A little part of view builder.
     *
     * @param task Task to process
     */
    private fun addTask(task: () -> Unit): CodeView {
        when (state) {
            ViewState.BUILD ->
                tasks.add(task)
            ViewState.PREPARE ->
                Thread.delayed(task)
            ViewState.PRESENTED ->
                task()
        }
        return this
    }

    /**
     * Process build tasks queue to build view.
     */
    private fun processBuildTasks() {
        while (tasks.isNotEmpty())
            tasks.poll()()
    }

    // - View builder

    /**
     * Specify color theme: syntax colors (need to highlighting) & related to
     * code view (numeration color & background, content backgrounds).
     *
     * @param colorTheme Default or custom color theme
     */

    // default color theme provided by enum
    fun setColorTheme(colorTheme: ColorTheme) = addTask {
        adapter.colorTheme = colorTheme.with()
    }

    // custom color theme provided by user
    fun setColorTheme(colorTheme: ColorThemeData) = addTask {
        adapter.colorTheme = colorTheme
    }

    /**
     * Highlight code by defined programming language.
     * It holds the placeholder on the view until code is highlighted.
     *
     * @param language Language to highlight
     */
    fun highlightCode(language: String) = addTask {
        adapter.highlightCode(language)
    }

    /**
     * Highlight code with trying to classify by code snippet.
     * It shows not highlighted code & then when classified refreshes view.
     */
    fun highlightCode() = addTask {
        adapter.highlightCode {
            refreshAnimated()
        }
    }

    /**
     * Useful in some cases if you want to listen user line clicks.
     * (May be you want to show alert, who knows?) ¯\_(ツ)_/¯
     *
     * @param listener Code line click listener
     */
    fun setCodeListener(listener: OnCodeLineClickListener) = addTask {
        adapter.codeListener = listener
    }

    /**
     * Control shadows visibility to provide more sensitive UI.
     *
     * @param isVisible Shadows visibility
     */
    fun setShadowsVisible(isVisible: Boolean = true) = addTask {
        val visibility = if (isVisible) View.VISIBLE else GONE
        vShadowRight.visibility = visibility
        vShadowBottomLine.visibility = visibility
        vShadowBottomContent.visibility = visibility
    }

    /**
     * Update code content if view was built or, finally, build code view.
     *
     * @param content Code content
     */
    fun setCodeContent(content: String) {
        when (state) {
            ViewState.BUILD ->
                build(content)
            ViewState.PREPARE ->
                Thread.delayed {
                    update(content)
                }
            ViewState.PRESENTED ->
                update(content)
        }
    }

    /**
     * Final step of view building.
     *
     * When layout have multiple code views it becomes a very expensive task.
     * Some task proceeds asynchronously, some not, but what is key point:
     * it should starts delayed a little bit to show necessary UI immediately.
     *
     * @param content Code content
     */
    private fun build(content: String) {
        val linesCount = extractLines(content).size
        measurePlaceholder(linesCount)
        state = ViewState.PREPARE

        Thread.delayed {
            rvCodeContent.adapter = CodeContentAdapter(context, content)
            processBuildTasks()
            setupShadows()
            hidePlaceholder()
            state = ViewState.PRESENTED
        }
    }

    /**
     * Hot view updating.
     *
     * @param content Code content
     */
    private fun update(content: String) {
        state = ViewState.PREPARE
        measurePlaceholder(extractLines(content).size)
        adapter.updateCodeContent(content)
        hidePlaceholder()
        state = ViewState.PRESENTED
    }

    // - Setup actions

    /**
     * Border shadows will shown if presented full code listing.
     * It helps user to see what part of content are scrolled & hidden.
     */
    private fun setupShadows() = setShadowsVisible(!adapter.isFullShowing)

    /**
     * Placeholder fills space at start and stretched to marked up view size
     * (by code lines count) because at this point it's not built yet.
     *
     * @param linesCount Count of lines to measure space for placeholder
     */
    private fun measurePlaceholder(linesCount: Int) {
        val lineHeight = dpToPx(context, 24)
        val topPadding = dpToPx(context, 8)

        // double padding (top & bottom), one is enough for single line view
        val padding = (if (linesCount > 1) 2 else 1) * topPadding

        val height = linesCount * lineHeight + padding

        vPlaceholder.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, height)
        vPlaceholder.visibility = View.VISIBLE
    }

    // - Animations

    private fun hidePlaceholder() = vPlaceholder.animate()
            .setDuration(350)
            .alpha(0f)
            .didAnimated {
                vPlaceholder.visibility = View.GONE
            }

    private fun refreshAnimated() = animate()
            .setDuration(150)
            .alpha(.2f)
            .didAnimated {
                adapter.notifyDataSetChanged()
                animate().alpha(1f)
            }
}

/**
 * Provides listener to code line clicks.
 */
interface OnCodeLineClickListener {
    fun onCodeLineClicked(n: Int, line: String)
}

/**
 * Extension for delayed block call.
 *
 * @param body Operation body
 */
fun Thread.delayed(body: () -> Unit) = Handler().postDelayed(body, 150)

/**
 * More readable form for animation listener (hi, iOS & Cocoa Touch!).
 *
 * @param handler Handler body
 */
fun ViewPropertyAnimator.didAnimated(handler: () -> Unit) =
        setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                handler()
            }
        })
