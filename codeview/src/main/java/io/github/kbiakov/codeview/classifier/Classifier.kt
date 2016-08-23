package io.github.kbiakov.codeview.classifier

import java.lang.Float.*
import java.util.*

/**
 * Original Java Naive Bayes Classifier was rewritten in Kotlin
 *
 * @see repository github.com/ptnplanet/Java-Naive-Bayes-Classifier
 * @author Philipp Nolte, Kirill Biakov
 */

/**
 * A basic wrapper reflecting a classification.
 * It will store both feature set and resulting classification.
 *
 * @param featureSet The feature class.
 * @param category The category class.
 */
data class Classification<T, K>(val featureSet: Collection<T>,
                                val category: K,
                                val probability: Float = 1f)

/**
 * Simple interface defining the method to calculate the feature probability.
 *
 * @param featureSet The feature class.
 * @param category The category class.
 */
interface IFeatureProbability<T, K> {
    fun featureProbability(feature: T, category: K): Float
}

/**
 * Abstract base extended by any concrete classifier.  It implements the basic
 * functionality for storing categories or features and can be used to calculate
 * basic probabilities – both category and feature probabilities. The classify
 * function has to be implemented by the concrete classifier class.
 *
 * @param featureSet The feature class.
 * @param category The category class.
 */
abstract class Classifier<T, K> : IFeatureProbability<T, K> {
    /**
     * The initial memory capacity or how many classifications are memorized.
     */
    var memoryCapacity = 1000
        /**
         * Sets the memory's capacity.  If the new value is less than the old
         * value, the memory will be truncated accordingly.
         *
         * @param memoryCapacity The new memory capacity.
         */
        set(memoryCapacity) {
            for (i in memoryCapacity downTo memoryCapacity + 1)
                memoryQueue.poll()

            field = memoryCapacity
        }

    /**
     * A dictionary mapping features to their number of occurrences in each known category.
     */
    var featureCountPerCategory: HashMap<K, HashMap<T, Int>>

    /**
     * A dictionary mapping features to their number of occurrences.
     */
    var totalFeatureCount: HashMap<T, Int>

    /**
     * A dictionary mapping categories to their number of occurrences.
     */
    var totalCategoryCount: HashMap<K, Int>

    /**
     * The classifier's memory. It will forget old classifications as soon as they become too old.
     */
    var memoryQueue: Queue<Classification<T, K>>

    init {
        featureCountPerCategory = HashMap<K, HashMap<T, Int>>(
                Classifier.INIT_CATEGORY_MAP_CAPACITY)
        totalFeatureCount = HashMap<T, Int>(
                Classifier.INIT_FEATURE_MAP_CAPACITY)
        totalCategoryCount = HashMap<K, Int>(
                Classifier.INIT_CATEGORY_MAP_CAPACITY)
        memoryQueue = LinkedList<Classification<T, K>>()
    }

    /**
     * @return The `Set` of features the classifier knows about.
     */
    val features: Set<T>
        get() = totalFeatureCount.keys

    /**
     * @return The `Set` of categories the classifier knows about.
     */
    val categories: Set<K>
        get() = totalCategoryCount.keys

    /**
     * Retrieves the total number of categories the classifier knows about.
     *
     * @return The total category count.
     */
    val categoriesTotal: Int
        get() = totalCategoryCount.values.reduce { prev, next -> prev + next }

    /**
     * Increments the count of a given feature in the given category.  This is
     * equal to telling the classifier, that this feature has occurred in this
     * category.
     *
     * @param feature The feature, which count to increase.
     * @param category The category the feature occurred in.
     */
    fun incrementFeature(feature: T, category: K) {
        val features = featureCountPerCategory[category] ?:
                HashMap<T, Int>(INIT_FEATURE_MAP_CAPACITY)
        featureCountPerCategory[category] = HashMap<T, Int>(features)

        var count = features[feature] ?: 0
        features[feature] = ++count

        var totalCount = totalFeatureCount[feature] ?: 0
        totalFeatureCount[feature] = ++totalCount
    }

    /**
     * Increments the count of a given category.  This is equal to telling the
     * classifier, that this category has occurred once more.
     *
     * @param category The category, which count to increase.
     */
    fun incrementCategory(category: K) {
        var count = totalCategoryCount[category] ?: 0
        totalCategoryCount[category] = ++count
    }

    /**
     * Decrements the count of a given feature in the given category.  This is
     * equal to telling the classifier that this feature was classified once in
     * the category.
     *
     * @param feature The feature to decrement the count for.
     * @param category The category.
     */
    fun decrementFeature(feature: T, category: K) {
        val features = featureCountPerCategory[category] ?: return
        var count = features[feature] ?: return

        if (count == 1) {
            features.remove(feature)

            if (features.isEmpty())
                featureCountPerCategory.remove(category)
        } else
            features.put(feature, --count)

        var totalCount = totalFeatureCount[feature] ?: return

        if (totalCount == 1)
            totalFeatureCount.remove(feature)
        else
            totalFeatureCount.put(feature, --totalCount)
    }

    /**
     * Decrements the count of a given category.  This is equal to telling the
     * classifier, that this category has occurred once less.
     *
     * @param category The category, which count to increase.
     */
    fun decrementCategory(category: K) {
        var count = totalCategoryCount[category] ?: return

        if (count == 1)
            totalCategoryCount.remove(category)
        else
            totalCategoryCount.put(category, --count)
    }

    /**
     * Retrieves the number of occurrences of the given feature in the given category.
     *
     * @param feature The feature, which count to retrieve.
     * @param category The category, which the feature occurred in.
     * @return The number of occurrences of the feature in the category.
     */
    fun featureCount(feature: T, category: K): Int {
        val features = featureCountPerCategory[category] ?: return 0
        val count = features[feature]
        return count ?: 0
    }

    /**
     * Retrieves the number of occurrences of the given category.
     *
     * @param category The category, which count should be retrieved.
     * @return The number of occurrences.
     */
    fun categoryCount(category: K) = totalCategoryCount[category] ?: 0

    /**
     * {@inheritDoc}
     */
    override fun featureProbability(feature: T, category: K): Float =
        if (categoryCount(category) == 0)
            0f
        else
            featureCount(feature, category).toFloat() / categoryCount(category)

    /**
     * Retrieves the weighed average `P(feature|category)` with
     * overall weight of `1.0` and an assumed probability of
     * `0.5`. The probability defaults to the overall feature
     * probability.
     *
     * @see de.daslaboratorium.machinelearning.classifier.Classifier.featureProbability
     * @see de.daslaboratorium.machinelearning.classifier.Classifier.featureWeighedAverage
     *
     * @param feature The feature, which probability to calculate.
     * @param category The category.
     * @return The weighed average probability.
     */
    fun featureWeighedAverage(feature: T, category: K): Float =
            featureWeighedAverage(feature, category,
                    weight = 1f,
                    assumedProbability = .5f)

    /**
     * Retrieves the weighed average `P(feature|category)` with
     * overall weight of `1.0`, an assumed probability of
     * `0.5` and the given object to use for probability calculation.
     *
     * @see de.daslaboratorium.machinelearning.classifier.Classifier.featureWeighedAverage
     *
     * @param feature The feature, which probability to calculate.
     * @param category The category.
     * @param calculator The calculating object.
     * @return The weighed average probability.
     */
    fun featureWeighedAverage(feature: T, category: K,
                              calculator: IFeatureProbability<T, K>): Float =
            featureWeighedAverage(feature, category, calculator, 1f, .5f)

    /**
     * Retrieves the weighed average `P(feature|category)` with
     * the given weight and an assumed probability of `0.5` and the
     * given object to use for probability calculation.
     *
     * @see de.daslaboratorium.machinelearning.classifier.Classifier.featureWeighedAverage
     *
     * @param feature The feature, which probability to calculate.
     * @param category The category.
     * @param calculator The calculating object.
     * @param weight The feature weight.
     * @return The weighed average probability.
     */
    fun featureWeighedAverage(feature: T, category: K,
                              calculator: IFeatureProbability<T, K>,
                              weight: Float): Float =
            featureWeighedAverage(feature, category, calculator, weight, .5f)

    /**
     * Retrieves the weighed average `P(feature|category)` with
     * the given weight, the given assumed probability and the given object to
     * use for probability calculation.
     *
     * @param feature The feature, which probability to calculate.
     * @param category The category.
     * @param calculator The calculating object.
     * @param weight The feature weight.
     * @param assumedProbability The assumed probability.
     * @return The weighed average probability.
     */
    fun featureWeighedAverage(feature: T, category: K,
                              calculator: IFeatureProbability<T, K>? = null,
                              weight: Float, assumedProbability: Float): Float {
        /*
         * Use the given calculating object or the default method to calculate
         * the probability that the given feature occurred in the given
         * category.
         */
        val basicProbability = if (calculator == null)
            featureProbability(feature, category)
        else
            calculator.featureProbability(feature, category)

        val totals = totalFeatureCount[feature] ?: 0
        return (weight * assumedProbability + totals * basicProbability) / (weight + totals)
    }

    /**
     * Train the classifier by telling it that the given features resulted in
     * the given category.
     *
     * @param category The category the features belong to.
     * @param features The features that resulted in the given category.
     */
    fun learn(category: K, features: Collection<T>) = learn(Classification(features, category))

    /**
     * Train the classifier by telling it that the given features resulted in
     * the given category.
     *
     * @param classification The classification to learn.
     */
    fun learn(classification: Classification<T, K>) {
        classification.featureSet.forEach { feature ->
            incrementFeature(feature, classification.category)
        }
        incrementCategory(classification.category)

        memoryQueue.offer(classification)

        if (memoryQueue.size > memoryCapacity) {
            val toForget = memoryQueue.remove()

            toForget.featureSet.forEach { feature ->
                decrementFeature(feature, toForget.category)
            }
            decrementCategory(toForget.category)
        }
    }

    /**
     * The classify method. It will retrieve the most likely category for the
     * features given and depends on the concrete classifier implementation
     *
     * @param features The features to classify
     * @return The category most likely
     */
    abstract fun classify(features: Collection<T>): Classification<T, K>?

    companion object {
        /**
         * Initial capacity of category dictionaries.
         */
        private val INIT_CATEGORY_MAP_CAPACITY = 16

        /**
         * Initial capacity of feature dictionaries. It should be quite big, because
         * the features will quickly outnumber the categories.
         */
        private val INIT_FEATURE_MAP_CAPACITY = 32
    }
}

/**
 * A concrete implementation of the abstract Classifier class. The Bayes
 * classifier implements a naive Bayes approach to classifying a given set
 * of features: classify(feat1,...,featN) = argmax(P(cat)*PROD(P(featI|cat)
 *
 * @see http://en.wikipedia.org/wiki/Naive_Bayes_classifier
 *
 * @param  The feature class.
 * @param  The category class.
 */
class BayesClassifier<T, K> : Classifier<T, K>() {
    /**
     * Calculates the product of all feature probabilities: PROD(P(featI|cat)
     *
     * @param features The set of features to use.
     * @param category The category to test for.
     * @return The product of all feature probabilities.
     */
    private fun featuresProbabilityProduct(features: Collection<T>, category: K) =
            features.fold(1f) { acc, feature ->
                acc * featureWeighedAverage(feature, category)
            }

    /**
     * Calculates the probability that the features can be classified as the
     * category given.
     *
     * @param features The set of features to use.
     * @param category The category to test for.
     * @return The probability that the features can be classified as the category.
     */
    private fun categoryProbability(features: Collection<T>, category: K) =
            categoryCount(category) / categoriesTotal *
                    featuresProbabilityProduct(features, category)

    /**
     * Retrieves a sorted `Set` of probabilities that the given set
     * of features is classified as the available categories.
     *
     * @param features The set of features to use.
     * @return A sorted `Set` of category-probability-entries.
     */
    private fun categoryProbabilities(features: Collection<T>): SortedSet<Classification<T, K>> {
        /*
         * Sort the set according to the possibilities. Because we have to sort
         * by the mapped value and not by the mapped key, we can not use a
         * sorted tree (TreeMap) and we have to use a set-entry approach to
         * achieve the desired functionality. A custom comparator is therefore
         * needed.
         */
        val probabilities = TreeSet<Classification<T, K>>(
                Comparator { o1, o2 ->
                    var toReturn = compare(o1.probability, o2.probability)

                    if (toReturn == 0 && o1.category != o2.category)
                        toReturn = -1

                    toReturn
                })

        categories.forEach { category ->
            val probability = categoryProbability(features, category)
            probabilities.add(Classification(features, category, probability))
        }

        return probabilities
    }

    /**
     * Classifies the given set of features.
     *
     * @return The category the set of features is classified as.
     */
    override fun classify(features: Collection<T>) = categoryProbabilities(features).last()

    /**
     * Classifies the given set of features. and return the full details of the classification.
     *
     * @return The set of categories the set of features is classified as.
     */
    fun classifyDetailed(features: Collection<T>) = categoryProbabilities(features)
}
