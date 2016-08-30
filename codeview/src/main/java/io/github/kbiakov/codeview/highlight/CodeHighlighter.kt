package io.github.kbiakov.codeview.highlight

import android.graphics.Color
import android.util.Log
import io.github.kbiakov.codeview.highlight.prettify.PrettifyParser
import io.github.kbiakov.codeview.highlight.parser.ParseResult
import java.util.*

/**
 * Code highlighter is parses content & inserts necessary font tags
 * accordingly to specified programming language & color theme.
 *
 * @author Kirill Biakov
 */
object CodeHighlighter {

    private val LT_BRACE = "<".toRegex()
    private val LT_REGULAR = "&lt;"
    private val LT_TMP = "^"

    private val parser = PrettifyParser()

    /**
     * Highlight code content.
     *
     * @param codeLanguage Programming language
     * @param rawSource Code source by one string
     * @param colorTheme Color theme (see below)
     * @return Highlighted code, string with necessary inserted color tags
     */
    fun highlight(codeLanguage: String, rawSource: String, colorTheme: ColorThemeData): String {
        val source = rawSource.escapeLT()
        val results = parser.parse(codeLanguage, source)
        val colorsMap = buildColorsMap(colorTheme)
        val highlighted = StringBuilder()

        results.forEach { result ->
            val color = colorsMap.getColor(result)
            val content = parseContent(source, result)
            //Log.e("!!!", content)

            highlighted.append(content.withFontParams(color))
        }

        return highlighted.toString()
    }

    // - Helpers

    /**
     * Parse user input by extracting highlighted content.
     *
     * @param codeContent Code content
     * @param result Syntax unit
     * @return Parsed content to highlight
     */
    private fun parseContent(codeContent: String, result: ParseResult): String {
        val length = result.offset + result.length
        val content = codeContent.substring(result.offset, length)
        return content.expandLT()
    }

    /**
     * Color accessor from built color map for selected color theme.
     *
     * @param colorsMap Colors map built from color theme
     * @param result Syntax unit
     * @return Color for syntax unit
     */
    private fun HashMap<String, String>.getColor(result: ParseResult) =
            this[result.styleKeys[0]] ?: this["pln"]

    /**
     * Build fast accessor (as map) for selected color theme.
     *
     * @param colorTheme Color theme
     * @return Colors map built from color theme
     */
    private fun buildColorsMap(colorTheme: ColorThemeData) =
            object : HashMap<String, String>() {
                init {
                    val syntaxColors = colorTheme.syntaxColors

                    put("typ", syntaxColors.type.hex())
                    put("kwd", syntaxColors.keyword.hex())
                    put("lit", syntaxColors.literal.hex())
                    put("com", syntaxColors.comment.hex())
                    put("str", syntaxColors.string.hex())
                    put("pun", syntaxColors.punctuation.hex())
                    put("pln", syntaxColors.plain.hex())
                    put("tag", syntaxColors.tag.hex())
                    put("dec", syntaxColors.declaration.hex())
                    put("src", syntaxColors.plain.hex())
                    put("atn", syntaxColors.attrName.hex())
                    put("atv", syntaxColors.attrValue.hex())
                    put("nocode", syntaxColors.plain.hex())
                }
            }

    // - Escaping/extracting "lower then" symbol

    private fun String.escapeLT() = replace(LT_BRACE, LT_TMP)
    private fun String.expandLT() = replace(LT_TMP, LT_REGULAR)
}

/**
 * Color theme presets.
 */
enum class ColorTheme(
        val syntaxColors: SyntaxColors = SyntaxColors(),
        val numColor: Int,
        val bgContent: Int,
        val bgNum: Int,
        val noteColor: Int) {

    SOLARIZED_LIGHT(
            numColor = 0x93A1A1,
            bgContent = 0xFDF6E3,
            bgNum = 0xEEE8D5,
            noteColor = 0x657B83),

    MONOKAI(
            syntaxColors = SyntaxColors(
                    type = 0xA7E22E,
                    keyword = 0xFA2772,
                    literal = 0x66D9EE,
                    comment = 0x76715E,
                    string = 0xE6DB74,
                    punctuation = 0xC1C1C1,
                    plain = 0xF8F8F0,
                    tag = 0xF92672,
                    declaration = 0xFA2772,
                    attrName = 0xA6E22E,
                    attrValue = 0xE6DB74),
            numColor = 0x48483E,
            bgContent = 0x272822,
            bgNum = 0x272822,
            noteColor = 0xCFD0C2),

    DEFAULT(
            numColor = 0x99A8B7,
            bgContent = 0xE9EDF4,
            bgNum = 0xF2F2F6,
            noteColor = 0x4C5D6E);

    /**
     * Decompose preset color theme to data.
     * Use this form for using from Kotlin.
     */
    fun with(
            mySyntaxColors: SyntaxColors = syntaxColors,
            myNumColor: Int = numColor,
            myBgContent: Int = bgContent,
            myBgNum: Int = bgNum,
            myNoteColor: Int = noteColor
    ) = ColorThemeData(
            mySyntaxColors,
            myNumColor,
            myBgContent,
            myBgNum,
            myNoteColor)

    /**
     * Decompose preset color theme to data.
     * Use this form for using from Java.
     */
    fun withSyntaxColors(mySyntaxColors: SyntaxColors) =
            with(mySyntaxColors = mySyntaxColors)
    fun withNumColor(myNumColor: Int) =
            with(myNumColor = myNumColor)
    fun withBgContent(myBgContent: Int) =
            with(myBgContent = myBgContent)
    fun withBgNum(myBgNum: Int) =
            with(myBgNum = myBgNum)
    fun withNoteColor(myNoteColor: Int) =
            with(myNoteColor = myNoteColor)
}

/**
 * Custom color theme.
 */
data class ColorThemeData(
        val syntaxColors: SyntaxColors = SyntaxColors(),
        val numColor: Int,
        val bgContent: Int,
        val bgNum: Int,
        val noteColor: Int)

/**
 * Colors for highlighting code units.
 */
data class SyntaxColors(
        val type: Int = 0x859900,
        val keyword: Int = 0x268BD2,
        val literal: Int = 0x269186,
        val comment: Int = 0x93A1A1,
        val string: Int = 0x269186,
        val punctuation: Int = 0x586E75,
        val plain: Int = 0x586E75,
        val tag: Int = 0x859900,
        val declaration: Int = 0x268BD2,
        val attrName: Int = 0x268BD2,
        val attrValue: Int = 0x269186)

/**
 * @return Converted hex int to color by adding alpha-channel
 */
fun Int.color() = try {
    Color.parseColor("#FF${Integer.toHexString(this)}")
} catch (e: IllegalArgumentException) {
    this
}

/**
 * @return Converted hex int to hex string
 */
fun Int.hex() = "#${Integer.toHexString(this)}"

/**
 * @return Is value equals to found or not condition
 */
fun Int.isFound() = this >= 0
fun Int.notFound() = this == -1

/**
 * @return String with applied font params
 */
fun String.withFontParams(color: String?): String {
    val parametrizedString = StringBuilder()

    var idx = 0
    var newIdx = indexOf("\n")

    if (newIdx.notFound())
        parametrizedString.append(inFontTag(color))
    else while (newIdx.isFound()) {
        if (idx > 0)
            parametrizedString.append("\n")

        val part = substring(idx..newIdx - 1).inFontTag(color)
        parametrizedString.append(part)

        idx = newIdx
        newIdx = indexOf("\n", idx + 1)
    }

    Log.e("!!!", parametrizedString.toString())

    return parametrizedString.toString()
}

/**
 * @return String surrounded by font tag
 */
fun String.inFontTag(color: String?) =
        "<font color=\"$color\">${
            if (startsWith("\n") && length > 2)
                substring(2)
            else this
        }</font>"
