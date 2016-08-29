package io.github.kbiakov.codeview.adapters

import android.content.Context
import io.github.kbiakov.codeview.highlight.color
import io.github.kbiakov.codeview.views.LineNoteView

/**
 * @class CodeWithNotesAdapter
 *
 * Default code content adapter.
 *
 * @author Kirill Biakov
 */
class CodeWithNotesAdapter : AbstractCodeAdapter<String> {
    /**
     * Default constructor.
     */
    constructor(context: Context, content: String) : super(context, content)

    /**
     * Create footer view.
     *
     * @param context Context
     * @param entity Note content
     */
    override fun createFooter(context: Context, entity: String) =
            LineNoteView.create(context,
                    text = entity,
                    bgColor = colorTheme.bgNum.color(),
                    textColor = colorTheme.noteColor.color())
}
