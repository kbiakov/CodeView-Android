package io.github.kbiakov.codeview.views

import android.content.Context
import android.widget.TextView

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
         * @param bgColor Background color
         * @param textColor Text Color
         * @return Created line note view
         */
        fun create(context: Context, text: String, bgColor: Int, textColor: Int): LineNoteView {
            val noteView = LineNoteView(context)
            noteView.textSize = 12f
            noteView.text = text
            noteView.setTextColor(textColor)
            noteView.setBackgroundColor(bgColor)

            return noteView
        }
    }
}
