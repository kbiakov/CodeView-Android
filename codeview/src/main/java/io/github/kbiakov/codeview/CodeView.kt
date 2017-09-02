package io.github.kbiakov.codeview

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import io.github.kbiakov.codeview.Thread.delayed
import io.github.kbiakov.codeview.adapters.AbstractCodeAdapter
import io.github.kbiakov.codeview.adapters.CodeWithNotesAdapter
import io.github.kbiakov.codeview.adapters.Options
import io.github.kbiakov.codeview.highlight.ColorThemeData
import io.github.kbiakov.codeview.highlight.color

/**
 * @class CodeView
 *
 * View for showing code content with syntax highlighting.
 *
 * @author Kirill Biakov
 */
class CodeView(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    private val vCodeList: RecyclerView
    private val vShadows: Map<ShadowPosition, View>

    /**
     * Primary constructor.
     */
    init {
        inflate(context, R.layout.layout_code_view, this)
        checkStartAnimation(attrs)

        vCodeList = findViewById(R.id.rv_code_content) as RecyclerView
        vCodeList.layoutManager = LinearLayoutManager(context)
        vCodeList.isNestedScrollingEnabled = true

        vShadows = mapOf(
                ShadowPosition.RightBorder to R.id.shadow_right_border,
                ShadowPosition.NumBottom to R.id.shadow_num_bottom,
                ShadowPosition.ContentBottom to R.id.shadow_content_bottom
        ).mapValues { findViewById(it.value) }
    }

    private fun checkStartAnimation(attrs: AttributeSet) {
        if (visibility == VISIBLE && attrs.isAnimateOnStart(context)) {
            alpha = Const.Alpha.Invisible

            animate()
                    .setDuration(Const.DefaultDelay * 5)
                    .alpha(Const.Alpha.Initial)
        } else
            alpha = Const.Alpha.Initial
    }

    private fun AbstractCodeAdapter<*>.checkHighlightAnimation(action: () -> Unit) {
        if (options.animateOnHighlight) {
            animate()
                    .setDuration(Const.DefaultDelay * 2)
                    .alpha(Const.Alpha.AlmostInvisible)
            delayed {
                animate().alpha(Const.Alpha.Visible)
                action()
            }
        } else action()
    }

    /**
     * Highlight code with defined programming language.
     * It holds the placeholder on view until code is not highlighted.
     */
    private fun highlight() {
        getAdapter()?.apply {
            highlight {
                checkHighlightAnimation(this::notifyDataSetChanged)
            }
        }
    }

    /**
     * Border shadows will shown if full listing presented.
     * It helps to see what part of code is scrolled & hidden.
     *
     * @param isVisible Is shadows visible
     */
    fun setupShadows(isVisible: Boolean) {
        val visibility = if (isVisible) VISIBLE else GONE
        val theme = getOptionsOrDefault().theme
        vShadows.forEach { (pos, view) ->
            view.visibility = visibility
            view.setSafeBackground(pos.createShadow(theme))
        }
    }

    // - Initialization

    /**
     * Prepare view with default adapter & options.
     */
    private fun prepare() = setAdapter(CodeWithNotesAdapter(context))

    /**
     * Initialize with options.
     *
     * @param options Options
     */
    fun setOptions(options: Options) = setAdapter(CodeWithNotesAdapter(context, options))

    /**
     * Initialize with adapter.
     *
     * @param adapter Adapter
     */
    fun setAdapter(adapter: AbstractCodeAdapter<*>) {
        vCodeList.adapter = adapter
        highlight()
    }

    // - Options

    /**
     * View options accessor.
     */
    fun getOptions() = getAdapter()?.options
    fun getOptionsOrDefault() = getOptions() ?: Options(context)

    /**
     * Update options or initialize if needed.
     *
     * @param options Options
     */
    fun updateOptions(options: Options) {
        getAdapter() ?: setOptions(options)
        getAdapter()?.options = options
        setupShadows(options.shadows)
    }

    fun updateOptions(body: Options.() -> Unit) {
        val options = getOptions() ?: getOptionsOrDefault()
        updateOptions(options.apply(body))
    }

    // - Adapter

    /**
     * Code adapter accessor.
     */
    fun getAdapter() = vCodeList.adapter as? AbstractCodeAdapter<*>

    /**
     * Update adapter or initialize if needed.
     *
     * @param adapter Adapter
     */
    fun updateAdapter(adapter: AbstractCodeAdapter<*>) {
        adapter.options = getOptionsOrDefault()
        setAdapter(adapter)
    }

    // - Set code

    /**
     * Set code content.
     *
     * There are two ways before code will be highlighted:
     * 1) view is not initialized (adapter or options are not set),
     *    prepare with default params & try to classify language
     * 2) view initialized with some params, language:
     *    a) is set: used defined programming language
     *    b) not set: try to classify
     *
     * @param code Code content
     */
    fun setCode(code: String) {
        getAdapter() ?: prepare()
        getAdapter()?.updateCode(code)
    }

    /**
     * Set code content.
     *
     * There are two ways before code will be highlighted:
     * 1) view is not initialized, prepare with default params
     * 2) view initialized with some params, set new language
     *
     * @param code Code content
     * @param language Programming language
     */
    fun setCode(code: String, language: String) {
        val options = getOptionsOrDefault()
        updateOptions(options.withLanguage(language))
        getAdapter()?.updateCode(code)
    }

    companion object {

        private fun AttributeSet.isAnimateOnStart(context: Context): Boolean {
            context.theme.obtainStyledAttributes(this, R.styleable.CodeView, 0, 0).apply {
                val flag = getBoolean(R.styleable.CodeView_animateOnStart, false)
                recycle()
                return@isAnimateOnStart flag
            }
            return false
        }

        private fun View.setSafeBackground(newBackground: Drawable) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                background = newBackground
            }
        }
    }

    private enum class ShadowPosition {
        RightBorder,
        NumBottom,
        ContentBottom;

        fun createShadow(theme: ColorThemeData) = when (this) {
            RightBorder -> GradientDrawable.Orientation.LEFT_RIGHT to theme.bgContent
            NumBottom -> GradientDrawable.Orientation.TOP_BOTTOM to theme.bgNum
            ContentBottom -> GradientDrawable.Orientation.TOP_BOTTOM to theme.bgContent
        }.let {
            val colors = arrayOf(android.R.color.transparent, it.second)
            GradientDrawable(it.first, colors.map(Int::color).toIntArray())
        }
    }
}

/**
 * Provide listener to code line clicks.
 */
interface OnCodeLineClickListener {
    fun onCodeLineClicked(n: Int, line: String)
}
