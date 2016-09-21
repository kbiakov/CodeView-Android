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
import io.github.kbiakov.codeview.highlight.CodeHighlighter
import io.github.kbiakov.codeview.highlight.ColorThemeData
import io.github.kbiakov.codeview.highlight.MonoFontCache
import io.github.kbiakov.codeview.highlight.color
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

    var highlighter: Highlighter
        internal set

    protected var lines: List<String> = ArrayList() //items
    protected var droppedLines: List<String>? = null

    private var footerEntities: HashMap<Int, List<T>> = HashMap()

    constructor(context: Context, h: Highlighter) {
        this.context = context
        this.highlighter = h

        prepareCodeLines()
    }

    /**
     * Adapter constructor.
     *
     * @param content Context
     * @param content Code content
     * @param isShowFull Do you want to show all code content?
     * @param maxLines Max lines to show (when limit is reached, rest is dropped)
     * @param shortcutNote When rest lines is dropped, note is shown as last string
     * @param listener Listener to code line clicks
     */
    constructor(context: Context,
                content: String,
                theme: ColorThemeData,
                isShowFull: Boolean = true,
                maxLines: Int = MAX_SHORTCUT_LINES,
                shortcutNote: String = context.getString(R.string.show_all),
                listener: OnCodeLineClickListener? = null) {
        this.context = context

        highlighter = Highlighter(context)

        highlighter.code = content
        highlighter.maxLines = maxLines
        highlighter.lineClickListener = listener
        highlighter.theme = theme
        highlighter.shortcut = !isShowFull
        highlighter.shortcutNote = shortcutNote

        prepareCodeLines()
    }

    fun isShorted(): Boolean = droppedLines != null

    //todo: showAllNotes()

    /**
     * Split code content by lines. If listing must not be shown full it shows
     * only necessary lines & rest are dropped (and stores in named variable).
     */
    internal fun prepareCodeLines() {
        val allLines = extractLines(highlighter.code)
        val isFullShowing = !highlighter.shortcut || allLines.size <= highlighter.maxLines // limit is not reached

        if (isFullShowing) {
            lines = allLines
            return
        } // else

        val resultLines = ArrayList(allLines.subList(0, highlighter.maxLines))

        if (!isFullShowing) {
            droppedLines = ArrayList(allLines.subList(highlighter.maxLines, allLines.lastIndex))
            if (highlighter.shortcutNote != null) {
                resultLines.add(highlighter.shortcutNote!!.toUpperCase())
            }
        }

        lines = resultLines
    }

    // - Adapter interface

    /**
     * Update code.
     */
    fun updateCode(newContent: String) {
        highlighter.code = newContent
        prepareCodeLines()
        notifyDataSetChanged()
    }

    /**
     * Update code with new Highlighter.
     */
    fun updateCode(h: Highlighter) {
        highlighter = h
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
        var notes = footerEntities[num] ?: ArrayList()
        footerEntities.put(num, notes + entity)
        notifyDataSetChanged()//todo: replace with notifyItemInserted()
    }

    /**
     * Highlight code content.
     *
     * @param onReady Callback when content is highlighted
     * @param language Programming language to highlight
     */
    fun highlight(onReady: () -> Unit) {
        async() {
            val classifiedLanguage = highlighter.language ?: classifyContent()
            highlighting(classifiedLanguage, onReady)
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
            processor.classify(highlighter.code).get()
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
        //todo: !!!performance (1) highlight next 10 then repeat (1)
        val code = CodeHighlighter.highlight(language, highlighter.code, highlighter.theme)

        updateContent(code, onReady)
    }

    /**
     * Return control to UI-thread when highlighted content is ready.
     * @param onUpdated Control callback
     */
    private fun updateContent(code: String, onUpdated: () -> Unit) {
        highlighter.code = code
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
        lineView.setBackgroundColor(highlighter.theme.bgContent.color())

        val tvLineNum = lineView.findViewById(R.id.tv_line_num) as TextView
        tvLineNum.typeface = monoTypeface()
        tvLineNum.setTextColor(highlighter.theme.numColor.color())
        tvLineNum.setBackgroundColor(highlighter.theme.bgNum.color())

        val tvLineContent = lineView.findViewById(R.id.tv_line_content) as TextView
        tvLineContent.typeface = monoTypeface()

        val holder = ViewHolder(lineView)
        holder.setIsRecyclable(false)
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val codeLine = lines[position]
        holder.mItem = codeLine

        if (highlighter.lineClickListener != null) {
            holder.itemView.setOnClickListener {
                highlighter.lineClickListener?.onLineClicked(position, codeLine)
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
        holder.tvLineContent.setTextColor(highlighter.theme.noteColor.color())

        if (highlighter.shortcut && position == MAX_SHORTCUT_LINES) {
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
        var tvLineNum: TextView
        var tvLineContent: TextView
        var llLineFooter: LinearLayout

        var mItem: String? = null

        init {
            tvLineNum = itemView.findViewById(R.id.tv_line_num) as TextView
            tvLineContent = itemView.findViewById(R.id.tv_line_content) as TextView
            llLineFooter = itemView.findViewById(R.id.ll_line_footer) as LinearLayout
        }

        override fun toString() = "${super.toString()} '$mItem'"
    }
}