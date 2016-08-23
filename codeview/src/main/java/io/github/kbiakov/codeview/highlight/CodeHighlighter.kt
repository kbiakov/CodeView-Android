package io.github.kbiakov.codeview.highlight

import android.graphics.Color
import prettify.PrettifyParser
import syntaxhighlight.ParseResult
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
            val color = getColor(colorsMap, result)
            val content = parseContent(source, result)
            highlighted.append("<font color=\"$color\">$content</font>")
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
    private fun getColor(colorsMap: HashMap<String, String>, result: ParseResult) =
            colorsMap[result.styleKeys[0]] ?: colorsMap["pln"]

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
 * Convert hex int to color by adding alpha-channel.
 *
 * @return Color int
 */
fun Int.color() = try {
    Color.parseColor("#FF${Integer.toHexString(this)}")
} catch (e: IllegalArgumentException) {
    this
}

/**
 * Convert hex int to hex string.
 *
 * @return Hex string
 */
fun Int.hex() = "#${Integer.toHexString(this)}"
