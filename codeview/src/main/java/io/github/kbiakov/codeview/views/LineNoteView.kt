package io.github.kbiakov.codeview.views

import android.content.Context
import android.widget.TextView
import io.github.kbiakov.codeview.R
import io.github.kbiakov.codeview.dpToPx

/**
 * @class LineNoteView
 *
 * Note view for code line. Default footer view.
 *
 * @author Kirill Biakov
 */
class LineNoteView(context: Context?) : TextView(context) {

    companion object Factory {
        /**
         * Simple factory method to create note view.
         *
         * @param context Context
         * @param text Note text
         * @param isFirst Is first footer view
         * @param bgColor Background color
         * @param textColor Text Color
         * @return Created line note view
         */
        fun create(context: Context, text: String, isFirst: Boolean, bgColor: Int, textColor: Int): LineNoteView {
            val noteView = LineNoteView(context)
            noteView.textSize = 12f
            noteView.text = text
            noteView.setTextColor(textColor)
            noteView.setBackgroundColor(bgColor)

            val dp8 = dpToPx(context, 8)

            val leftPadding = context.resources.getDimension(
                    R.dimen.line_num_width).toInt() + dpToPx(context, 14)

            val topPadding = if (isFirst) dp8 else 0

            noteView.setPadding(leftPadding, topPadding, dp8, dp8)

            return noteView
        }
    }
}
