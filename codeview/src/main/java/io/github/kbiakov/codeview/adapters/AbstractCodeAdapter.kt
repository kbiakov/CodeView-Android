package io.github.kbiakov.codeview.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import io.github.kbiakov.codeview.*
import io.github.kbiakov.codeview.Thread.async
import io.github.kbiakov.codeview.Thread.ui
import io.github.kbiakov.codeview.classifier.CodeClassifier
import io.github.kbiakov.codeview.classifier.CodeProcessor
import io.github.kbiakov.codeview.highlight.*
import java.util.*

/**
 * @class AbstractCodeAdapter
 *
 * Basic adapter for code view.
 *
 * @author Kirill Biakov
 */
abstract class AbstractCodeAdapter<T> : RecyclerView.Adapter<AbstractCodeAdapter.ViewHolder> {

    protected val context: Context
    protected var lines: List<String> = ArrayList() // items
    protected var droppedLines: List<String>? = null

    internal var options: Options

    private var footerEntities: HashMap<Int, List<T>> = HashMap()

    constructor(context: Context) {
        this.context = context
        this.options = Options(context)
        prepareCodeLines()
    }

    constructor(context: Context, code: String) {
        this.context = context
        this.options = Options(context, code)
        prepareCodeLines()
    }

    constructor(context: Context, options: Options) {
        this.context = context
        this.options = options
        prepareCodeLines()
    }

    /**
     * Split code content by lines. If listing must not be shown full, it shows
     * only necessary lines & the rest are dropped (and stores in named variable).
     */
    internal fun prepareCodeLines() {
        val allLines = extractLines(options.code)
        val isFullShowing = !options.shortcut || allLines.size <= options.maxLines // limit is not reached

        if (isFullShowing)
            lines = allLines
        else {
            val resultLines = ArrayList(allLines.subList(0, options.maxLines))
            resultLines.add(options.shortcutNote.toUpperCase())
            lines = resultLines

            droppedLines = ArrayList(allLines.subList(options.maxLines, allLines.lastIndex))
        }
    }

    // - Adapter interface

    /**
     * Update code.
     */
    internal fun updateCode(newContent: String) {
        options.code = newContent
        prepareCodeLines()
        notifyDataSetChanged()
    }

    /**
     * Update code with new Highlighter.
     */
    internal fun updateCode(opts: Options) {
        options = opts
        prepareCodeLines()
        notifyDataSetChanged()
    }

    /**
     * Add footer entity for code line.
     *
     * @param num Line number
     * @param entity Footer entity
     */
    fun addFooterEntity(num: Int, entity: T) {
        val notes = footerEntities[num] ?: ArrayList()
        footerEntities.put(num, notes + entity)
        notifyDataSetChanged() // TODO: replace with notifyItemInserted()
    }

    /**
     * Highlight code content.
     *
     * @param onReady Callback when content is highlighted
     */
    internal fun highlight(onReady: () -> Unit) {
        async() {
            val language = options.language ?: classifyContent()
            highlighting(language, onReady)
        }
    }

    /**
     * Mapper from entity to footer view.
     *
     * @param context Context
     * @param entity Entity to init view
     * @param isFirst Is first footer view
     * @return Footer view
     */
    abstract fun createFooter(context: Context, entity: T, isFirst: Boolean): View

    // - Helpers (for accessors)

    /**
     * Classify current code content.
     *
     * @return Classified language
     */
    private fun classifyContent(): String {
        val processor = CodeProcessor.getInstance(context)

        return if (processor.isTrained)
            processor.classify(options.code).get()
        else
            CodeClassifier.DEFAULT_LANGUAGE
    }

    /**
     * Highlight code content by language.
     *
     * @param language Language to highlight
     * @param onReady Callback
     */
    private fun highlighting(language: String, onReady: () -> Unit) {
        // TODO: highlight by 10 lines
        val code = CodeHighlighter.highlight(language, options.code, options.theme)

        updateContent(code, onReady)
    }

    /**
     * Return control to UI-thread when highlighted content is ready.
     * @param onUpdated Control callback
     */
    private fun updateContent(code: String, onUpdated: () -> Unit) {
        options.code = code
        prepareCodeLines()

        ui {
            onUpdated()
        }
    }

    private fun monoTypeface() = MonoFontCache.getInstance(context).typeface

    // - View holder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val lineView = inflater.inflate(R.layout.item_code_line, parent, false)
        lineView.setBackgroundColor(options.theme.bgContent.color())

        val tvLineNum = lineView.findViewById(R.id.tv_line_num) as TextView
        tvLineNum.typeface = monoTypeface()
        tvLineNum.setTextColor(options.theme.numColor.color())
        tvLineNum.setBackgroundColor(options.theme.bgNum.color())

        val tvLineContent = lineView.findViewById(R.id.tv_line_content) as TextView
        tvLineContent.typeface = monoTypeface()

        val holder = ViewHolder(lineView)
        holder.setIsRecyclable(false)
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val codeLine = lines[position]
        holder.mItem = codeLine

        options.lineClickListener?.let {
            holder.itemView.setOnClickListener {
                options.lineClickListener?.onLineClicked(position, codeLine)
            }
        }

        setupLine(position, codeLine, holder)
        displayLineFooter(position, holder)
        addExtraPadding(position, holder)
    }

    override fun getItemCount() = lines.size

    private fun Int.isFirst() = this == 0
    private fun Int.isLast() = this == itemCount - 1
    private fun Int.isJustFirst() = isFirst() && !isLast()
    private fun Int.isJustLast() = isLast() && !isFirst()
    private fun Int.isBorder() = isFirst() || isLast()

    // - Helpers (for view holder)

    private fun setupLine(position: Int, line: String, holder: ViewHolder) {
        holder.tvLineContent.text = html(line)
        holder.tvLineContent.setTextColor(options.theme.noteColor.color())

        if (options.shortcut && position == MAX_SHORTCUT_LINES) {
            holder.tvLineNum.textSize = 10f
            holder.tvLineNum.text = context.getString(R.string.dots)
        } else {
            holder.tvLineNum.textSize = 12f
            holder.tvLineNum.text = "${position + 1}"
        }
    }

    private fun displayLineFooter(position: Int, holder: ViewHolder) {
        val entityList = footerEntities[position]

        holder.llLineFooter.removeAllViews()

        entityList?.let {
            holder.llLineFooter.visibility = if (it.isNotEmpty()) View.VISIBLE else View.GONE

            var isFirst = true

            it.forEach { entity ->
                val footerView = createFooter(context, entity, isFirst)
                holder.llLineFooter.addView(footerView)
                isFirst = false
            }
        }
    }

    private fun addExtraPadding(position: Int, holder: ViewHolder) {
        if (position.isBorder()) {
            val dp8 = dpToPx(context, 8)
            val topPadding = if (position.isJustFirst()) dp8 else 0
            val bottomPadding = if (position.isJustLast()) dp8 else 0
            holder.tvLineNum.setPadding(0, topPadding, 0, bottomPadding)
            holder.tvLineContent.setPadding(0, topPadding, 0, bottomPadding)
        } else {
            holder.tvLineNum.setPadding(0, 0, 0, 0)
            holder.tvLineContent.setPadding(0, 0, 0, 0)
        }
    }

    companion object {
        internal const val MAX_SHORTCUT_LINES = 6
    }

    /**
     * View holder for code adapter.
     * Stores all views related to code line layout.
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLineNum: TextView
        val tvLineContent: TextView
        val llLineFooter: LinearLayout

        var mItem: String? = null

        init {
            tvLineNum = itemView.findViewById(R.id.tv_line_num) as TextView
            tvLineContent = itemView.findViewById(R.id.tv_line_content) as TextView
            llLineFooter = itemView.findViewById(R.id.ll_line_footer) as LinearLayout
        }

        override fun toString() = "${super.toString()} '$mItem'"
    }
}

/**
 * @class Options
 *
 * All options in one place.
 *
 * @param context Context
 * @param code Code content
 * @param language Programming language to highlight
 * @param theme Color theme
 * @param shadows Is border shadows needed?
 * @param maxLines Max lines to show (when limit is reached, rest is dropped)
 * @param shortcut Do you want to show shortcut of code listing?
 * @param shortcutNote When rest lines is dropped, note is shown as last string
 * @param lineClickListener Listener to code line clicks
 *
 * @author Kirill Biakov
 */
data class Options(
        val context: Context,
        var code: String = "",
        var language: String? = null,
        var theme: ColorThemeData = ColorTheme.DEFAULT.theme(),
        var shadows: Boolean = false,
        var shortcut: Boolean = false,
        var shortcutNote: String = context.getString(R.string.show_all),
        var maxLines: Int = 0,
        var lineClickListener: OnCodeLineClickListener? = null) {

    fun withCode(code: String): Options {
        this.code = code
        return this
    }

    fun withLanguage(language: String): Options {
        this.language = language
        return this
    }

    fun withTheme(theme: ColorTheme): Options {
        this.theme = theme.theme()
        return this
    }

    fun withTheme(theme: ColorThemeData): Options {
        this.theme = theme
        return this
    }

    fun withShadows(): Options {
        this.shadows = true
        return this
    }

    fun withoutShadows(): Options {
        this.shadows = false
        return this
    }

    fun shortcut(maxLines: Int, shortcutNote: String): Options {
        this.shortcut = true
        this.maxLines = maxLines
        this.shortcutNote = shortcutNote
        return this
    }

    fun addLineClickListener(listener: OnCodeLineClickListener): Options {
        this.lineClickListener = listener
        return this
    }

    fun removeLineClickListener(): Options {
        this.lineClickListener = null
        return this
    }

    companion object Default {
        fun get(context: Context): Options = Options(context)
    }
}
