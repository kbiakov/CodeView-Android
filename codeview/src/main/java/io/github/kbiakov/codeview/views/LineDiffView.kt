package io.github.kbiakov.codeview.views

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import io.github.kbiakov.codeview.R
import io.github.kbiakov.codeview.highlight.MonoFontCache

/**
 * @class CodeDiffView
 *
 * View to present code difference (additions & deletions).
 *
 * @author Kirill Biakov
 */
class LineDiffView : RelativeLayout {

    private val tvLineDiff: TextView
    private val tvLineContent: TextView

    /**
     * Default constructor.
     */
    constructor(context: Context) : super(context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.item_code_diff, this, true)

        tvLineDiff = findViewById(R.id.tv_line_diff) as TextView
        tvLineContent = findViewById(R.id.tv_line_content) as TextView
    }

    companion object Factory {
        /**
         * Simple factory method to create code diff view.
         *
         * @param context Context
         * @param model Diff model
         * @return Created line diff view
         */
        fun create(context: Context, model: DiffModel): LineDiffView {
            val diffView = LineDiffView(context)
            diffView.tvLineDiff.text = if (model.isAddition) "+" else "-"
            diffView.tvLineContent.text = model.content
            diffView.tvLineContent.typeface = MonoFontCache.getInstance(context).typeface

            diffView.setBackgroundColor(ContextCompat.getColor(context,
                    if (model.isAddition)
                        R.color.diff_add_background
                    else R.color.diff_del_background))

            return diffView
        }
    }
}

/**
 * Model to provide code difference (additions & deletions).
 *
 * @author Kirill Biako
 */
data class DiffModel(val content: String, val isAddition: Boolean = true)
