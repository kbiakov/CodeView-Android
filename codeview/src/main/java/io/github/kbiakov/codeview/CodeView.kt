package io.github.kbiakov.codeview

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import io.github.kbiakov.codeview.Thread.delayed
import io.github.kbiakov.codeview.adapters.AbstractCodeAdapter
import io.github.kbiakov.codeview.adapters.CodeWithNotesAdapter
import io.github.kbiakov.codeview.adapters.Options

/**
 * @class CodeView
 *
 * View for showing code content with syntax highlighting.
 *
 * @author Kirill Biakov
 */
class CodeView(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    private val vShadowRight: View
    private val vShadowBottomLine: View
    private val vShadowBottomContent: View

    private val vCodeList: RecyclerView

    /**
     * Primary constructor.
     */
    init {
        val isAnimateOnStart = visibility == VISIBLE && { ctx: Context, ats: AttributeSet ->
            val a = ctx.theme.obtainStyledAttributes(ats, R.styleable.CodeView, 0, 0)

            try {
                a.getBoolean(R.styleable.CodeView_animateOnStart, true)
            } finally {
                a.recycle()
            }
        }(context, attrs)

        alpha = if (isAnimateOnStart) 0f else Consts.ALPHA

        inflate(context, R.layout.layout_code_view, this)

        if (isAnimateOnStart)
            animate().setDuration(Consts.DELAY * 5)
                    .alpha(Consts.ALPHA)

        // TODO: add shadow color customization
        vShadowRight = findViewById(R.id.v_shadow_right)
        vShadowBottomLine = findViewById(R.id.v_shadow_bottom_line)
        vShadowBottomContent = findViewById(R.id.v_shadow_bottom_content)

        vCodeList = findViewById(R.id.rv_code_content) as RecyclerView
        vCodeList.layoutManager = LinearLayoutManager(context)
        vCodeList.isNestedScrollingEnabled = true
    }

    /**
     * Highlight code with defined programming language.
     * It holds the placeholder on view until code is not highlighted.
     */
    private fun highlight() {
        getAdapter()?.highlight {

            animate().setDuration(Consts.DELAY * 2)
                    .alpha(.1f)

            delayed {
                animate().alpha(1f)
                vCodeList.adapter?.notifyDataSetChanged()
            }
        }
    }

    /**
     * Border shadows will shown if presented full code listing.
     * It helps to see what part of code is scrolled & hidden.
     *
     * @param isShadows Is shadows needed
     */
    private fun setupShadows(isShadows: Boolean) {
        val visibility = if (isShadows) VISIBLE else GONE

        vShadowRight.visibility = visibility
        vShadowBottomLine.visibility = visibility
        vShadowBottomContent.visibility = visibility
    }

    /**
     * Prepare view with default adapter & options.
     */
    private fun prepare() = setAdapter(CodeWithNotesAdapter(context))

    /**
     * View options accessor.
     */
    fun getOptions(): Options? = getAdapter()?.options

    /**
     * Initialize with options.
     *
     * @param options Options
     */
    fun setOptions(options: Options) = setOptions(options, false)

    /**
     * Set options & initialize if needed.
     *
     * @param options Options
     * @param isSaveAdapter Save adapter?
     */
    fun setOptions(options: Options, isSaveAdapter: Boolean = true) {
        setAdapter(if (isSaveAdapter)
            getAdapter() ?: CodeWithNotesAdapter(context, options)
        else
            CodeWithNotesAdapter(context, options))
    }

    /**
     * Code adapter accessor.
     */
    fun getAdapter() = vCodeList.adapter as? AbstractCodeAdapter<*>

    /**
     * Initialize with adapter.
     *
     * @param adapter Adapter
     */
    fun setAdapter(adapter: AbstractCodeAdapter<*>) = setAdapter(adapter, false)

    /**
     * Set adapter & initialize if needed.
     *
     * @param adapter Adapter
     * @param isSaveOptions Save options?
     */
    fun setAdapter(adapter: AbstractCodeAdapter<*>, isSaveOptions: Boolean = true) {
        if (isSaveOptions)
            adapter.options = getOptions() ?: Options(context)

        vCodeList.adapter = adapter
        setupShadows(adapter.options.shadows)
        highlight()
    }

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
        getAdapter()!!.updateCode(code)
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
        val options = if (getAdapter() == null)
            Options(context)
        else
            getAdapter()!!.options

        setOptions(options.withLanguage(language))
        getAdapter()!!.updateCode(code)
    }
}

/**
 * Provide listener to code line clicks.
 */
interface OnCodeLineClickListener {
    fun onLineClicked(n: Int, line: String)
}
