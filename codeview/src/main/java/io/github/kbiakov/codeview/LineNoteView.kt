package io.github.kbiakov.codeview

import android.content.Context
import android.widget.TextView

class LineNoteView(context: Context?) : TextView(context) {

    companion object Factory {
        fun create(context: Context, text: String, textColor: Int): LineNoteView {
            val noteView = LineNoteView(context)
            noteView.textSize = 12f
            noteView.text = text
            noteView.setTextColor(textColor)
            return noteView
        }
    }
}
