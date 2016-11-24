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
     * Code adapter accessor.
     */
    private fun getAdapter() = vCodeList.adapter as? AbstractCodeAdapter<*>

    /**
     * Highlight code by defined programming language.
     * It holds the placeholder on the view until code is not highlighted.
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
    private fun prepare() {
        setAdapter(CodeWithNotesAdapter(context))
    }

    /**
     * Initialize with options.
     *
     * @param opts Options
     */
    fun setOptions(opts: Options) {
        setAdapter(CodeWithNotesAdapter(context, opts))
    }

    /**
     * Initialize with adapter.
     *
     * @param adapter Adapter
     */
    fun setAdapter(adapter: AbstractCodeAdapter<*>) {
        vCodeList.adapter = adapter
        setupShadows(adapter.opts.shadows)
        highlight()
    }

    /**
     * Set code content.
     * At this point view should be prepared, otherwise it
     * will be configured automatically with default params.
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
     * @param code Code content
     * @param language Programming language
     */
    fun setCode(code: String, language: String) {
        getAdapter() ?: setOptions(Options(context, language = language))
        getAdapter()?.updateCode(code)
    }
}

/**
 * Provide listener to code line clicks.
 */
interface OnCodeLineClickListener {
    fun onLineClicked(n: Int, line: String)
}
