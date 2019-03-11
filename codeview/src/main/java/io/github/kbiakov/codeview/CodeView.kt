package io.github.kbiakov.codeview

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.kbiakov.codeview.Thread.delayed
import io.github.kbiakov.codeview.adapters.AbstractCodeAdapter
import io.github.kbiakov.codeview.adapters.CodeWithNotesAdapter
import io.github.kbiakov.codeview.adapters.Options
import io.github.kbiakov.codeview.highlight.ColorThemeData
import io.github.kbiakov.codeview.highlight.color

/**
 * @class CodeView
 *
 * Display code with syntax highlighting.
 *
 * @author Kirill Biakov
 */
class CodeView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val rvContent: RecyclerView
    private val shadows: Map<ShadowPosition, View>
    private val adapter get() = rvContent.adapter as? AbstractCodeAdapter<*>

    /**
     * Primary constructor.
     */
    init {
        inflate(context, R.layout.layout_code_view, this)
        attrs?.let(::checkStartAnimation)

        rvContent = findViewById<RecyclerView>(R.id.rv_content).apply {
            layoutManager = LinearLayoutManager(context)
            isNestedScrollingEnabled = true
        }

        shadows = mapOf(
                ShadowPosition.RightBorder to R.id.shadow_right_border,
                ShadowPosition.NumBottom to R.id.shadow_num_bottom,
                ShadowPosition.ContentBottom to R.id.shadow_content_bottom
        ).mapValues {
            findViewById<View>(it.value)
        }
    }

    private fun checkStartAnimation(attrs: AttributeSet) {
        if (visibility == VISIBLE && attrs.isAnimateOnStart(context)) {
            alpha = Const.Alpha.Invisible

            animate()
                    .setDuration(Const.DefaultDelay * 5)
                    .alpha(Const.Alpha.Initial)
        } else {
            alpha = Const.Alpha.Initial
        }
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
        } else {
            action()
        }
    }

    /**
     * Border shadows will shown if full listing presented.
     * It helps to see which part of code is scrolled & hidden.
     *
     * @param isVisible Is shadows visible
     */
    fun setupShadows(isVisible: Boolean) {
        val visibility = if (isVisible) VISIBLE else GONE
        val theme = optionsOrDefault.theme
        shadows.forEach { (pos, view) ->
            view.visibility = visibility
            view.setSafeBackground(pos.createShadow(theme))
        }
    }

    // - Options

    /**
     * View options accessor.
     */
    private val optionsOrDefault get() = adapter?.options ?: Options(context)

    /**
     * Initialize with options.
     *
     * @param options Options
     */
    fun setOptions(options: Options) = setAdapter(CodeWithNotesAdapter(context, options))

    /**
     * Update options or initialize if needed.
     *
     * @param options Options
     */
    fun updateOptions(options: Options) {
        adapter
                ?.let { it.options = options }
                ?: setOptions(options)

        setupShadows(options.shadows)
    }

    /**
     * Update options or initialize if needed.
     *
     * @param body Options mutator
     */
    fun updateOptions(body: Options.() -> Unit) =
            optionsOrDefault
                    .apply(body)
                    .apply(::updateOptions)

    // - Adapter

    /**
     * Initialize with adapter.
     *
     * Highlight code with defined programming language.
     * It holds the placeholder on view until code is not highlighted.
     *
     * @param adapter Adapter
     */
    fun setAdapter(adapter: AbstractCodeAdapter<*>) {
        rvContent.adapter = adapter.apply {
            highlight { checkHighlightAnimation(::notifyDataSetChanged) }
        }
    }

    /**
     * Update adapter or initialize if needed.
     *
     * @param adapter Adapter
     * @param isUseCurrent Use options that are already set or default
     */
    fun updateAdapter(adapter: AbstractCodeAdapter<*>, isUseCurrent: Boolean) {
        setAdapter(adapter.apply {
            if (isUseCurrent) {
                options = optionsOrDefault
            }
        })
    }

    // - Set code

    /**
     * Set code content. View is:
     * 1) not initialized (adapter or options is not set):
     *    prepare with default params & try to classify language
     * 2) initialized (with some params), language is:
     *    a) set: use defined
     *    b) not set: try to classify
     *
     * @param code Code content
     */
    fun setCode(code: String) = setCode(code, null)

    /**
     * Set code content. View is:
     * 1) not initialized: prepare with default params
     * 2) initialized (with some params): set new language
     *
     * @param code Code content
     * @param language Programming language
     */
    fun setCode(code: String, language: String? = null) {
        val options = optionsOrDefault.apply {
            this.language = language
        }
        (adapter ?: CodeWithNotesAdapter(context, options)
                .apply(::setAdapter))
                .updateCode(code)
    }

    companion object {

        private fun AttributeSet.isAnimateOnStart(context: Context) =
                context.theme.obtainStyledAttributes(this, R.styleable.CodeView, 0, 0).run {
                    val isAnimate = getBoolean(R.styleable.CodeView_animateOnStart, false)
                    recycle()
                    isAnimate
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
        }.let { (orientation, color) ->
            val colors = arrayOf(android.R.color.transparent, color)
            GradientDrawable(orientation, colors.map(Int::color).toIntArray())
        }
    }
}

/**
 * Provide listener to code line clicks.
 */
interface OnCodeLineClickListener {
    fun onCodeLineClicked(n: Int, line: String)
}
