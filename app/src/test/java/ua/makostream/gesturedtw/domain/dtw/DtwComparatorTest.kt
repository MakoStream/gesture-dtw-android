package ua.makostream.gesturedtw.domain.dtw

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.makostream.gesturedtw.domain.model.AccelerometerSample

class DtwComparatorTest {

    private val dtw = DtwComparator(EuclideanDistance())

    private fun seq(vararg xs: Float) = xs.map { AccelerometerSample(it, 0f, 0f) }

    @Test
    fun identicalSequencesHaveZeroDistance() {
        val a = seq(0f, 1f, 2f, 1f, 0f)
        assertEquals(0.0, dtw.distance(a, a), 1e-9)
    }

    @Test
    fun distanceIsSymmetric() {
        val a = seq(0f, 1f, 2f, 1f)
        val b = seq(0f, 2f, 2f, 0f, 1f)
        assertEquals(dtw.distance(a, b), dtw.distance(b, a), 1e-9)
    }

    @Test
    fun singleSamplesUseMetricDirectly() {
        assertEquals(3.0, dtw.distance(seq(1f), seq(4f)), 1e-9)
    }

    @Test
    fun timeStretchedSequenceHasZeroDistance() {
        val original = seq(0f, 1f, 2f, 1f, 0f)
        val stretched = original.flatMap { listOf(it, it) }
        assertEquals(0.0, dtw.distance(original, stretched), 1e-9)
    }

    @Test
    fun stretchedSequenceIsCloserThanDifferentGesture() {
        val original = seq(0f, 1f, 2f, 1f, 0f)
        val stretched = original.flatMap { listOf(it, it) }
        val different = seq(2f, 2f, -2f, -2f, 2f)

        assertTrue(dtw.distance(original, stretched) < dtw.distance(original, different))
    }

    @Test
    fun differentLengthsAreSupported() {
        val a = seq(0f, 1f, 2f)
        val b = seq(0f, 0.5f, 1f, 1.5f, 2f, 2f, 2f)
        assertTrue(dtw.distance(a, b).isFinite())
    }

    @Test
    fun narrowWindowStillReachesTheEnd() {
        val limited = DtwComparator(EuclideanDistance(), window = 1)
        val a = seq(0f, 1f, 2f)
        val b = seq(0f, 0f, 0f, 1f, 1f, 1f, 2f, 2f, 2f)
        assertTrue(limited.distance(a, b).isFinite())
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyFirstSequenceIsRejected() {
        dtw.distance(emptyList(), seq(1f))
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptySecondSequenceIsRejected() {
        dtw.distance(seq(1f), emptyList())
    }
}