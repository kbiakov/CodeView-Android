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
open class CodeView : RelativeLayout {

    private val vShadowRight: View
    private val vShadowBottomLine: View
    private val vShadowBottomContent: View

    /**
     * Core view to draw code by lines.
     */
    private val vCodeList: RecyclerView

    fun getRecyclerView(): RecyclerView {
        return vCodeList
    }

    /**
     * Default constructor.
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val defaultAlpha = 0.7531f
        var animateOnStart = true

        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.CodeView, 0, 0)
        try {
            animateOnStart = a.getBoolean(R.styleable.CodeView_animateOnStart, animateOnStart)
        } finally {
            a.recycle()
        }
        animateOnStart = animateOnStart && visibility == View.VISIBLE

        alpha = if (animateOnStart) 0f else defaultAlpha

        inflate(context, R.layout.layout_code_view, this)

        if (animateOnStart) {
            animate().setDuration(Utils.DELAY * 5)
                    .alpha(defaultAlpha)
        }

        vShadowRight = findViewById(R.id.v_shadow_right)//todo: shadow color customization
        vShadowBottomLine = findViewById(R.id.v_shadow_bottom_line)//todo: shadow color customization
        vShadowBottomContent = findViewById(R.id.v_shadow_bottom_content)//todo: shadow color customization

        vCodeList = findViewById(R.id.rv_code_content) as RecyclerView
        vCodeList.layoutManager = LinearLayoutManager(context)
        vCodeList.isNestedScrollingEnabled = true
    }

    /**
     * Initialize RecyclerView with adapter
     * then start highlighting
     */
    fun init(h: Highlighter, adapter: AbstractCodeAdapter<*>) {
        if (h.code.isEmpty()) {
            throw IllegalStateException("Please set code() before init/highlight")
        }

        vCodeList.adapter = adapter

        setupShadows(adapter.highlighter.shadows)

        highlight()
    }

    /**
     * Initialize RecyclerView with adapter
     * then start highlighting
     */
    fun init(adapter: AbstractCodeAdapter<*>) {
        init(adapter.highlighter, adapter)
    }

    /**
     * Initialize RecyclerView with adapter
     * then start highlighting
     */
    fun init(h: Highlighter) {
        init(h, CodeWithNotesAdapter(context, h))
    }

    /**
     * Highlight code by defined programming language.
     * It holds the placeholder on the view until code is highlighted.
     */
    fun highlight() {
        if (vCodeList.adapter == null) {
            throw IllegalStateException("Please set adapter or use init(highlighter) before highlight()")
        }

        getAdapter()?.highlight() {
            animate().setDuration(Utils.DELAY * 2)
                    .alpha(.1f)

            delayed {
                animate().alpha(1f)
                vCodeList.adapter?.notifyDataSetChanged()
            }
        }
    }

    /**
     * Remove code listener.
     */
    fun removeLineClickListener() {
        getAdapter()?.highlighter?.lineClickListener = null
    }

    fun getAdapter() = vCodeList.adapter as? AbstractCodeAdapter<*>

    /**
     * Update code.
     */
    fun update(code: String) {
        getAdapter()?.updateCode(code)
    }

    /**
     * Update code.
     */
    fun update(h: Highlighter) {
        init(getAdapter()!!.highlighter.update(h))
    }

    // - Setup actions

    /**
     * Border shadows will shown if presented full code listing.
     * It helps user to see what part of code are scrolled & hidden.
     */
    private fun setupShadows(shadows: Boolean) {
        val visibility = if (shadows) VISIBLE else GONE

        vShadowRight.visibility = visibility
        vShadowBottomLine.visibility = visibility
        vShadowBottomContent.visibility = visibility
    }
}

/**
 * Provides listener to code line clicks.
 */
interface OnCodeLineClickListener {
    fun onLineClicked(n: Int, line: String)
}
