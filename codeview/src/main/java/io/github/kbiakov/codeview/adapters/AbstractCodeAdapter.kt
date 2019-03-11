package io.github.kbiakov.codeview.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.github.kbiakov.codeview.*
import io.github.kbiakov.codeview.Thread.asyncUi
import io.github.kbiakov.codeview.adapters.AbstractCodeAdapter.ViewHolderType.Companion.BordersCount
import io.github.kbiakov.codeview.adapters.AbstractCodeAdapter.ViewHolderType.Companion.LineStartIdx
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
    protected var lines: List<String> = ArrayList()
    protected var droppedLines: List<String>? = null

    internal var options: Options

    private var footerEntities = SparseArray<List<T>>()

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
        extractLines(options.code).apply {
            if (!options.shortcut || size <= options.maxLines) // limit is not reached, show full
                lines = this
            else slice(options.maxLines).let { (linesToShow, dropped) ->
                lines = linesToShow + options.shortcutNote.toUpperCase()
                droppedLines = dropped
            }
        }
    }

    // - Adapter interface

    /**
     * Update code.
     */
    internal fun updateCode(newCode: String) {
        options.code = newCode
        prepareCodeLines()
        notifyDataSetChanged()
    }

    /**
     * Update code with new Highlighter.
     */
    internal fun updateCode(newOptions: Options) {
        options = newOptions
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
    internal fun highlight(onReady: () -> Unit) = asyncUi({
        val language = options.language ?: classifyContent()

        // TODO: highlight by 10 lines
        CodeHighlighter.highlight(language, options.code, options.theme)
    }, {
        updateContent(it, onReady)
    })

    /**
     * Mapper from entity to footer view.
     *
     * @param context Context
     * @param entity Entity to setOptions view
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
    private fun classifyContent(): String = with(CodeProcessor.getInstance(context)) {
        if (isTrained) {
            classify(options.code).get()
        } else {
            CodeClassifier.DEFAULT_LANGUAGE
        }
    }

    /**
     * Return control to UI-thread when highlighted content is ready.
     * @param onUpdated Control callback
     */
    private fun updateContent(code: String, onUpdated: () -> Unit) {
        options.code = code
        options.isHighlighted = true
        prepareCodeLines()
        onUpdated()
    }

    // - View holder callbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            with(LayoutInflater.from(parent.context).inflate(R.layout.item_code_line, parent, false)) {
                setBackgroundColor(options.theme.bgContent.color())

                with(findViewById<TextView>(R.id.tv_line_num)) {
                    typeface = options.font
                    setTextColor(options.theme.numColor.color())
                    setBackgroundColor(options.theme.bgNum.color())
                }

                findViewById<TextView>(R.id.tv_line_content).typeface = options.font

                return if (viewType == ViewHolderType.Line.viewType) {
                    layoutParams.height = dpToPx(context, options.format.lineHeight)
                    LineViewHolder(this).apply { setIsRecyclable(false) }
                } else {
                    layoutParams.height = dpToPx(context, options.format.borderHeight)
                    BorderViewHolder(this)
                }
            }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        if (holder is LineViewHolder) {
            val num = pos - LineStartIdx
            holder.mItem = lines[num]

            bindClickListener(num, holder)
            setupContent(num, holder)
            displayFooter(num, holder)
        }
    }

    override fun getItemCount() = lines.size + BordersCount

    override fun getItemViewType(pos: Int) = ViewHolderType.get(pos, itemCount)

    // - Helpers (for view holder)

    private fun bindClickListener(pos: Int, holder: ViewHolder) {
        holder.itemView.setOnClickListener {
            options.lineClickListener?.onCodeLineClicked(pos, lines[pos])
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupContent(pos: Int, holder: ViewHolder) {
        val fontSize = options.format.fontSize

        with(holder.tvLineNum) {
            if (!options.shortcut || pos < MaxShortcutLines) {
                text = "${pos + 1}"
                textSize = fontSize
            } else {
                text = context.getString(R.string.dots)
                textSize = fontSize * Format.ShortcutScale
            }
        }

        with(holder.tvLineContent) {
            text = lines[pos].let { if (options.isHighlighted) html(it) else it }
            textSize = fontSize
            setTextColor(options.theme.noteColor.color())
        }
    }

    private fun displayFooter(pos: Int, holder: ViewHolder) = with(holder.llLineFooter) {
        removeAllViews()

        footerEntities[pos]?.let {
            visibility = if (it.isNotEmpty()) View.VISIBLE else View.GONE

            it.forEachIndexed { idx, entity ->
                addView(createFooter(context, entity, idx == 0))
            }
        }
    }

    companion object {
        private const val MaxShortcutLines = 6
    }

    // - View holder types

    enum class ViewHolderType(val viewType: Int) {
        Line(0), Border(1);

        companion object {
            const val LineStartIdx = 1
            const val BordersCount = 2

            private fun Int.lineEndIdx() = this - BordersCount

            fun get(pos: Int, n: Int) = when (pos) {
                in LineStartIdx .. n.lineEndIdx() ->
                    ViewHolderType.Line.viewType
                else ->
                    ViewHolderType.Border.viewType
            }
        }
    }

    /**
     * View holder for code adapter.
     * Stores all views related to code line layout.
     */
    open class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLineNum = itemView.findViewById(R.id.tv_line_num) as TextView
        val tvLineContent = itemView.findViewById(R.id.tv_line_content) as TextView
        val llLineFooter = itemView.findViewById(R.id.ll_line_footer) as LinearLayout

        var mItem: String? = null

        override fun toString() = "${super.toString()} '$mItem'"
    }

    private class LineViewHolder(itemView: View) : ViewHolder(itemView)

    /**
     * View holder for padding.
     * Stores all views related to code line layout.
     */
    private class BorderViewHolder(itemView: View) : ViewHolder(itemView)
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
 * @param font Font typeface
 * @param format How much space is content took?
 * @param animateOnHighlight Is animate on highlight?
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
        var font: Typeface = FontCache.get(context).getTypeface(context),
        var format: Format = Format.Compact,
        var animateOnHighlight: Boolean = true,
        var shadows: Boolean = false,
        var shortcut: Boolean = false,
        var shortcutNote: String = context.getString(R.string.show_all),
        var maxLines: Int = 0,
        var lineClickListener: OnCodeLineClickListener? = null
) {

    internal var isHighlighted: Boolean = false

    fun withCode(code: String) = apply { this.code = code }
    fun withCode(codeResId: Int) = apply { code = context.getString(codeResId) }
    fun setCode(codeResId: Int) { withCode(codeResId) }
    fun withLanguage(language: String) = apply { this.language = language }

    fun withTheme(theme: ColorThemeData) = apply { this.theme = theme }
    fun withTheme(theme: ColorTheme) = apply { this.theme = theme.theme() }
    fun setTheme(theme: ColorTheme) { withTheme(theme) }

    fun withFont(font: Font) = apply { this.font = font.get() }
    fun withFont(font: Typeface) = font saveAndThen { apply { this.font = font } }
    fun withFont(fontPath: String) = apply { this.font = fontPath.get() }
    fun setFont(fontPath: String) { withFont(fontPath) }
    fun setFont(font: Font) { withFont(font) }
    fun withFormat(format: Format) = apply { this.format = format }

    fun animateOnHighlight() = apply { animateOnHighlight = true }
    fun disableHighlightAnimation() = apply { animateOnHighlight = false }
    fun withShadows() = apply { shadows = true }
    fun withoutShadows() = apply { shadows = false }

    fun addCodeLineClickListener(listener: OnCodeLineClickListener) = apply { lineClickListener = listener }
    fun removeCodeLineClickListener() = apply { lineClickListener = null }

    fun shortcut(maxLines: Int, shortcutNote: String) = apply {
        this.shortcut = true
        this.maxLines = maxLines
        this.shortcutNote = shortcutNote
    }

    companion object Default {
        fun get(context: Context) = Options(context)
    }

    // - Font helpers

    private val fontCache = FontCache.get(context)
    private fun Font.get() = fontCache.getTypeface(context, this)
    private fun String.get() = fontCache.getTypeface(context, this)
    private infix fun <T> Typeface.saveAndThen(body: () -> T): T = fontCache.saveTypeface(this).let { body() }
}

data class Format(
        val scaleFactor: Float = 1f,
        val lineHeight: Int = (LineHeight * scaleFactor).toInt(),
        val borderHeight: Int = (BorderHeight * scaleFactor).toInt(),
        val fontSize: Float = FontSize.toFloat()
) {

    companion object Default {
        private const val LineHeight = 18
        private const val BorderHeight = 3
        private const val FontSize = 12

        internal const val ShortcutScale = 0.83f

        val ExtraCompact = Format(0.88f)
        val Compact = Format()
        val Medium = Format(1.33f)
    }
}
