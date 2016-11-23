package io.github.kbiakov.codeview.adapters

import android.content.Context
import io.github.kbiakov.codeview.Highlighter
import io.github.kbiakov.codeview.highlight.ColorThemeData
import io.github.kbiakov.codeview.highlight.color
import io.github.kbiakov.codeview.views.LineNoteView

/**
 * @class CodeWithNotesAdapter
 *
 * Default code content adapter.
 *
 * @author Kirill Biakov
 */
open class CodeWithNotesAdapter : AbstractCodeAdapter<String> {

    constructor(context: Context, h: Highlighter) : super(context, h)

    /**
     * Default constructor.
     */
    constructor(context: Context, content: String, colorTheme: ColorThemeData) : super(context, content, colorTheme)

    //todo: inflateFooter(int layoutId)

    /**
     * Create footer view.
     *
     * @param entity Note content
     * @param isFirst Is first footer view
     */
    override fun createFooter(context: Context, entity: String, isFirst: Boolean) =
            LineNoteView.create(context,
                    text = entity,
                    isFirst = isFirst,
                    bgColor = highlighter.theme.bgNum.color(),
                    textColor = highlighter.theme.noteColor.color())
}
