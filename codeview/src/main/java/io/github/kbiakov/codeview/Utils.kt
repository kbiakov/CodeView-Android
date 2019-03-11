package io.github.kbiakov.codeview

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.util.TypedValue
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors

object Const {
    val DefaultDelay = 250L

    object Alpha {
        val Visible = 1f
        val Initial = 0.7f

        val AlmostInvisible = 0.1f
        val Invisible = 0f
    }
}

/**
 * Get px by dip value.
 *
 * @param context Context
 * @param dp Dip value
 * @return Converted to px value
 */
fun dpToPx(context: Context, dp: Int) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(), context.resources.displayMetrics).toInt()

/**
 * Split string by space.
 *
 * @param source Source
 * @return Split string
 */
fun spaceSplit(source: String) = source.split("\\s".toRegex())

/**
 * Split string for lines.
 *
 * @param source Source
 * @return Split string
 */
fun extractLines(source: String) = listOf(*source.split("\n").toTypedArray())

/**
 * Slice list by index.
 *
 * @param idx Index to slice
 * @return Pair of lists with head and tail
 */
fun <T> List<T>.slice(idx: Int) = Pair(subList(0, idx), subList(idx, lastIndex))

/**
 * Get HTML from string.
 *
 * @param content Source
 * @return Spanned HTML string
 */
@Suppress("deprecation")
fun html(content: String): Spanned {
    val spanned = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
        Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY)
    else
        Html.fromHtml(content)
    val spaces = content.startSpacesForTaggedString()
    return SpannableString(TextUtils.concat(spaces, spanned))
}

private fun String.startSpacesForTaggedString(): String {
    val startIdx = indexOf('>') + 1
    val escaped = substring(startIdx)
    val count = escaped.indexOf(escaped.trim())
    return " ".repeat(count)
}

object Thread {
    /**
     * Perform async operation.
     *
     * @param body Operation body
     */
    fun async(body: () -> Unit) {
        Executors.newSingleThreadExecutor().submit(body)
    }

    /**
     * Perform UI operation.
     *
     * @param body Operation body
     */
    fun ui(body: () -> Unit) {
        Handler(Looper.getMainLooper()).post(body)
    }

    /**
     * Perform async and UI operations sequentially.
     *
     * @param asyncBody Async operation body
     * @param uiBody UI operation body
     */
    fun <T> asyncUi(asyncBody: () -> T, uiBody: (T) -> Unit) = async { asyncBody().also { ui { uiBody(it)} } }

    /**
     * Delayed block call.
     *
     * @param body Operation body
     * @param delayMs Delay in m
     */
    fun delayed(delayMs: Long = Const.DefaultDelay, body: () -> Unit) {
        Handler().postDelayed(body, delayMs)
    }

    // - Extensions for block manipulations

    fun (() -> Unit).ui(isUi: Boolean = true) {
        if (isUi) ui(this) else this()
    }
}

object Files {
    /**
     * Get list of files in folder by path.
     *
     * @param context Context
     * @param path Path
     * @return List of files
     */
    fun ls(context: Context, path: String) = context.assets.list(path)

    /**
     * Merge files into one string by path.
     *
     * @param context Context
     * @param path Path
     * @return Merged content
     */
    fun content(context: Context, path: String): String {
        var content = ""

        ls(context, path).forEach { filename ->
            val input = context.assets.open("$path/$filename")

            BufferedReader(InputStreamReader(input, "UTF-8")).useLines {
                content += it.reduce { acc, line -> acc + line }
            }
        }
        return content
    }
}
