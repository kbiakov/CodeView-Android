package io.github.kbiakov.codeview.adapters

import android.content.Context
import io.github.kbiakov.codeview.views.DiffModel
import io.github.kbiakov.codeview.views.LineDiffView

/**
 * @class CodeWithDiffsAdapter
 *
 * Code content adapter with ability to add diffs (additions & deletions) in footer.
 *
 * @author Kirill Biakov
 */
class CodeWithDiffsAdapter : AbstractCodeAdapter<DiffModel> {
    /**
     * Default constructor.
     */
    constructor(context: Context, content: String) : super(context, content)

    /**
     * Create footer view.
     *
     * @param context Context
     * @param entity Note content
     * @param isFirst Is first footer
     */
    override fun createFooter(context: Context, entity: DiffModel, isFirst: Boolean) =
            LineDiffView.create(context, entity)
}
