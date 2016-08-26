package io.github.kbiakov.codeview

import android.content.Context
import android.widget.TextView

/**
 * Simple note view for code line.
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
         * @param isFirst If is first note
         * @param bgColor Background color
         * @param textColor Text Color
         * @return Created line note view
         */
        fun create(context: Context, text: String, isFirst: Boolean,
                   bgColor: Int, textColor: Int): LineNoteView {
            val noteView = LineNoteView(context)
            noteView.textSize = 12f
            noteView.text = text
            noteView.setTextColor(textColor)
            noteView.setBackgroundColor(bgColor)

            val dp8 = dpToPx(context, 8)
            noteView.setPadding(dpToPx(context, 46), if (isFirst) dp8 else 0, dp8, dp8)

            return noteView
        }
    }
}
