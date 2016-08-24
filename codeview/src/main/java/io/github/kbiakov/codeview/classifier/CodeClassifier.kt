package io.github.kbiakov.codeview.classifier

import android.content.Context
import android.util.Log
import io.github.kbiakov.codeview.Files
import io.github.kbiakov.codeview.spaceSplit

/**
 * @class CodeClassifier
 *
 * Code classifier based on Naive Bayes Classifier is necessary to define
 * what language is used in presented code snippet.
 *
 * @author Kirill Biakov
 */
object CodeClassifier {

    const val TAG = "CodeClassifier"

    /**
     * Default is the most popular programming language - JavaScript,
     * because it covers most of keywords & syntax constructions.
     *
     * (Highlighting should work not so bad even if this is not yours.)
     */
    const val DEFAULT_LANGUAGE = "js"

    private val TRAINING_SET_FOLDER = "training-set"

    private val classifier: BayesClassifier<String, String>

    /**
     * Create Naive Bayes classifier at start.
     */
    init {
        classifier = BayesClassifier()
    }

    /**
     * At this point all files with code listings prepared to process
     * by classifier. This operation often is very expensive & should
     * be performed asynchronously when app starts.
     *
     * @param context Context
     */
    fun train(context: Context) {
        Files.ls(context, TRAINING_SET_FOLDER).forEach { language ->
            val path = "$TRAINING_SET_FOLDER/$language"
            val content = Files.content(context, path)
            classifier.learn(language, spaceSplit(content))
        }

        Log.i(TAG, "Classifier trained")
    }

    /**
     * Try to define what language is used in code snippet.
     *
     * @param snippet Code snippet
     * @return Code language
     */
    fun classify(snippet: String): String {
        val feature = classifier.classify(spaceSplit(snippet))
        return feature?.category ?: DEFAULT_LANGUAGE
    }
}
