package ua.makostream.gesturedtw.domain.dtw

import kotlin.math.abs
import timber.log.Timber
import ua.makostream.gesturedtw.domain.model.AccelerometerSample

/**
 * Dynamic Time Warping distance between two sequences.
 *
 * @param metric distance between two single samples
 * @param window optional Sakoe-Chiba band half-width: limits how far the alignment
 *               may drift from the diagonal (faster, avoids absurd alignments).
 *               null means no limit.
 */
class DtwComparator(
    private val metric: DistanceMetric,
    private val window: Int? = null
) : SequenceComparator {

    init {
        require(window == null || window >= 0) { "window must be >= 0" }
    }

    override fun distance(a: List<AccelerometerSample>, b: List<AccelerometerSample>): Double {
        require(a.isNotEmpty() && b.isNotEmpty()) { "sequences must not be empty" }

        val n = a.size
        val m = b.size
        // The band must be wide enough to reach the bottom-right corner.
        val w = window?.let { maxOf(it, abs(n - m)) } ?: maxOf(n, m)

        var prev = DoubleArray(m + 1) { INF }
        var curr = DoubleArray(m + 1) { INF }
        prev[0] = 0.0

        for (i in 1..n) {
            curr.fill(INF)
            val from = maxOf(1, i - w)
            val to = minOf(m, i + w)
            for (j in from..to) {
                val cost = metric.distance(a[i - 1], b[j - 1])
                curr[j] = cost + minOf(prev[j], curr[j - 1], prev[j - 1])
            }
            val tmp = prev
            prev = curr
            curr = tmp
        }

        val result = prev[m]
        Timber.v("DTW distance=%.4f (n=%d, m=%d, window=%s)", result, n, m, window)
        return result
    }

    private companion object {
        const val INF = Double.POSITIVE_INFINITY
    }
}