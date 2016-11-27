package io.github.kbiakov.codeview

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.Spanned
import android.util.TypedValue
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors

object Consts {
    val ALPHA = 0.7F
    val DELAY = 250L
}

/**
 * Get px by dip value.
 *
 * @param context Context
 * @param dp Dip value
 * @return Converted to px value
 */
fun dpToPx(context: Context, dp: Int): Int =
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
 * Get HTML from string.
 *
 * @param content Source
 * @return Spanned HTML string
 */
@Suppress("deprecation")
fun html(content: String): Spanned =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
            Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY)
        else
            Html.fromHtml(content)

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
     * Delayed block call.
     *
     * @param body Operation body
     * @param delayMs Delay in m
     */
    fun delayed(delayMs: Long = Consts.DELAY, body: () -> Unit) =
            Handler().postDelayed(body, delayMs)

    // - Extensions for block manipulations

    fun (() -> Unit).ui(isUi: Boolean = true) {
        if (isUi) ui {
            this()
        } else this()
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
            val input = context.assets.open(path + '/' + filename)

            BufferedReader(InputStreamReader(input, "UTF-8")).useLines {
                it.forEach { line ->
                    content += line
                }
            }
        }

        return content
    }
}
