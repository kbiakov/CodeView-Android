package io.github.kbiakov.codeview.views

import android.content.Context
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.github.kbiakov.codeview.R
import io.github.kbiakov.codeview.highlight.FontCache

/**
 * @class CodeDiffView
 *
 * View to present code difference (additions & deletions).
 *
 * @author Kirill Biakov
 */
class LineDiffView(context: Context) : RelativeLayout(context) {

    private val tvLineDiff: TextView
    private val tvLineContent: TextView

    init {
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
        fun create(context: Context, model: DiffModel) = LineDiffView(context).apply {
            tvLineDiff.text = if (model.isAddition) "+" else "-"
            tvLineContent.text = model.content
            tvLineContent.typeface = FontCache.get(context).getTypeface(context)

            setBackgroundColor(ContextCompat.getColor(context,
                    if (model.isAddition)
                        R.color.diff_add_background
                    else
                        R.color.diff_del_background))
        }
    }
}

/**
 * Model for code difference (additions & deletions).
 *
 * @author Kirill Biakov
 */
data class DiffModel(val content: String, val isAddition: Boolean = true)
