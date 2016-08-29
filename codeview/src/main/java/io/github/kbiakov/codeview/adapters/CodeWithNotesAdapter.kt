package io.github.kbiakov.codeview.adapters

import android.content.Context
import io.github.kbiakov.codeview.highlight.color
import io.github.kbiakov.codeview.views.LineNoteView

/**
 *
 */
class CodeWithNotesAdapter : AbstractCodeAdapter<String> {
    /**
     * Default constructor.
     */
    constructor(context: Context, content: String) : super(context, content)

    /**
     * Add note to code line.
     *
     * @param num Line number
     * @param entity Note content
     */
    override fun createFooter(context: Context, entity: String) =
            LineNoteView.create(context,
                    noteText = entity,
                    bgColor = colorTheme.bgNum.color(),
                    textColor = colorTheme.noteColor.color())
}
