package ua.makostream.gesturedtw.domain.recognizer

import timber.log.Timber
import ua.makostream.gesturedtw.domain.dtw.SequenceComparator
import ua.makostream.gesturedtw.domain.model.AccelerometerSample
import ua.makostream.gesturedtw.domain.model.GestureTemplate
import ua.makostream.gesturedtw.domain.model.RecognitionResult

/**
 * Finds the template closest to a recorded movement.
 *
 * The raw DTW distance grows with the length of the sequences, so it is divided
 * by (n + m) to get a length-independent value that can be compared with [threshold].
 *
 * @param comparator how two sequences are compared (DTW in this app)
 * @param threshold the largest normalized distance still accepted as a match
 */
class GestureRecognizer(
    private val comparator: SequenceComparator,
    private val threshold: Double
) {

    init {
        require(threshold >= 0.0) { "threshold must be >= 0" }
    }

    fun recognize(
        input: List<AccelerometerSample>,
        templates: List<GestureTemplate>
    ): RecognitionResult {
        require(input.isNotEmpty()) { "input must not be empty" }

        if (templates.isEmpty()) {
            Timber.w("No templates available, cannot recognize")
            return RecognitionResult.Unknown(bestDistance = null)
        }

        val scored = templates.map { template ->
            val raw = comparator.distance(input, template.samples)
            val normalized = raw / (input.size + template.samples.size)
            Timber.d("Template '%s': normalized distance = %.4f", template.gestureId, normalized)
            template.gestureId to normalized
        }

        val (bestId, bestDistance) = scored.minByOrNull { it.second }!!

        return if (bestDistance <= threshold) {
            Timber.i("Recognized '%s' (distance %.4f)", bestId, bestDistance)
            RecognitionResult.Match(bestId, bestDistance)
        } else {
            Timber.i("Unknown gesture (best distance %.4f > threshold %.4f)", bestDistance, threshold)
            RecognitionResult.Unknown(bestDistance)
        }
    }
}