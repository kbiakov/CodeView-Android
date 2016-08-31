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
     * @param isFirst Is first footer view
     */
    override fun createFooter(context: Context, entity: String, isFirst: Boolean) =
            LineNoteView.create(context,
                    text = entity,
                    isFirst = isFirst,
                    bgColor = colorTheme.bgNum.color(),
                    textColor = colorTheme.noteColor.color())
}
