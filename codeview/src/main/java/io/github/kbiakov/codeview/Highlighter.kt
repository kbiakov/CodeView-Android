package io.github.kbiakov.codeview

import android.content.Context
import io.github.kbiakov.codeview.highlight.ColorTheme
import io.github.kbiakov.codeview.highlight.ColorThemeData

data class Highlighter(val context: Context) {

    var theme: ColorThemeData = ColorTheme.DEFAULT.theme()
    var code = ""
    var language: String? = null
    var codeResId = 0
    var shortcut = false
    var maxLines = 0
    var shortcutNote: String? = null
    var shortcutNoteResId = 0
    var lineClickListener: OnCodeLineClickListener? = null
    var shadows = false

    // - updated

    var themeUpdated = false
    var codeUpdated = false
    var languageUpdated = false
    var codeResIdUpdated = false
    var shortcutUpdated = false
    var maxLinesUpdated = false
    var shortcutNoteUpdated = false
    var shortcutNoteResIdUpdated = false
    var lineClickListenerUpdated = false
    var shadowsUpdated = true

    fun language(language: String): Highlighter {
        this.language = language
        languageUpdated = true
        return this
    }

    fun code(code: String): Highlighter {
        this.code = code
        codeUpdated = true
        return this
    }

    fun code(codeResId: Int): Highlighter {
        this.code = context.getString(codeResId)
        codeResIdUpdated = true
        return this
    }

    fun shortcut(shortcut: Boolean): Highlighter {
        this.shortcut = shortcut
        shortcutUpdated = true
        return this
    }

    fun maxLines(maxLines: Int): Highlighter {
        this.maxLines = maxLines
        maxLinesUpdated = true
        return this
    }

    fun shortcutNote(shortcutNote: String): Highlighter {
        this.shortcutNote = shortcutNote
        shortcutNoteUpdated = true
        return this
    }

    fun shortcutNote(shortcutNoteResId: Int): Highlighter {
        this.shortcutNote = context.getString(shortcutNoteResId)
        shortcutNoteResIdUpdated = true
        return this
    }

    fun theme(theme: ColorTheme): Highlighter {
        return this.theme(theme.theme())
    }

    fun theme(theme: ColorThemeData): Highlighter {
        this.theme = theme
        themeUpdated = true
        return this
    }

    fun lineClickListener(listener: OnCodeLineClickListener): Highlighter {
        lineClickListener = listener
        lineClickListenerUpdated = true
        return this
    }

    fun shadows(shadows: Boolean = true): Highlighter {
        this.shadows = shadows
        shadowsUpdated = true
        return this
    }

    /**
     * Highlight code finally.
     */
    fun highlight(codeView: CodeView) {
        codeView.init(this)
    }

    /**
     * Update highlighter.
     */
    fun update(newSettings: Highlighter): Highlighter {
        if (newSettings.themeUpdated) {
            theme = newSettings.theme
        }
        if (newSettings.codeUpdated) {
            code = newSettings.code
        }
        if (newSettings.languageUpdated) {
            language = newSettings.language
        }
        if (newSettings.codeResIdUpdated) {
            codeResId = newSettings.codeResId
        }
        if (newSettings.shortcutUpdated) {
            shortcut = newSettings.shortcut
        }
        if (newSettings.maxLinesUpdated) {
            maxLines = newSettings.maxLines
        }
        if (newSettings.shortcutNoteUpdated) {
            shortcutNote = newSettings.shortcutNote
        }
        if (newSettings.shortcutNoteResIdUpdated) {
            shortcutNoteResId = newSettings.shortcutNoteResId
        }
        if (newSettings.lineClickListenerUpdated) {
            lineClickListener = newSettings.lineClickListener
        }
        if (newSettings.shadowsUpdated) {
            shadows = newSettings.shadows
        }

        return this
    }
}