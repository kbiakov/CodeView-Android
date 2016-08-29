package io.github.kbiakov.codeview.views

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
         * @param noteText Note text
         * @param bgColor Background color
         * @param textColor Text Color
         * @return Created line note view
         */
        fun create(context: Context, noteText: String, bgColor: Int, textColor: Int): LineNoteView {
            val noteView = LineNoteView(context)
            noteView.textSize = 12f
            noteView.text = noteText
            noteView.setTextColor(textColor)
            noteView.setBackgroundColor(bgColor)

            return noteView
        }
    }
}
