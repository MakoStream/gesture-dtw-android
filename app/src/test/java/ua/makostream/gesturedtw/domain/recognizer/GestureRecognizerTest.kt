package ua.makostream.gesturedtw.domain.recognizer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.makostream.gesturedtw.domain.dtw.DtwComparator
import ua.makostream.gesturedtw.domain.dtw.EuclideanDistance
import ua.makostream.gesturedtw.domain.model.AccelerometerSample
import ua.makostream.gesturedtw.domain.model.GestureTemplate
import ua.makostream.gesturedtw.domain.model.RecognitionResult

class GestureRecognizerTest {

    private fun seq(vararg xs: Float) = xs.map { AccelerometerSample(it, 0f, 0f) }

    private val wave = GestureTemplate("wave", seq(0f, 1f, 2f, 1f, 0f))
    private val shake = GestureTemplate("shake", seq(2f, 2f, -2f, -2f, 2f))
    private val templates = listOf(wave, shake)

    private val recognizer = GestureRecognizer(
        comparator = DtwComparator(EuclideanDistance()),
        threshold = 0.5
    )

    @Test
    fun recognizesExactTemplate() {
        val result = recognizer.recognize(wave.samples, templates)

        assertTrue(result is RecognitionResult.Match)
        assertEquals("wave", (result as RecognitionResult.Match).gestureId)
        assertEquals(0.0, result.distance, 1e-9)
    }

    @Test
    fun recognizesTimeStretchedGesture() {
        val stretched = wave.samples.flatMap { listOf(it, it) }

        val result = recognizer.recognize(stretched, templates)

        assertTrue(result is RecognitionResult.Match)
        assertEquals("wave", (result as RecognitionResult.Match).gestureId)
    }

    @Test
    fun picksTheClosestOfSeveralTemplates() {
        val result = recognizer.recognize(shake.samples, templates)

        assertEquals("shake", (result as RecognitionResult.Match).gestureId)
    }

    @Test
    fun returnsUnknownWhenAboveThreshold() {
        val far = seq(10f, 10f, 10f)

        val result = recognizer.recognize(far, templates)

        assertTrue(result is RecognitionResult.Unknown)
        assertNotNull((result as RecognitionResult.Unknown).bestDistance)
    }

    @Test
    fun returnsUnknownWithoutDistanceWhenNoTemplates() {
        val result = recognizer.recognize(wave.samples, emptyList())

        assertTrue(result is RecognitionResult.Unknown)
        assertNull((result as RecognitionResult.Unknown).bestDistance)
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyInputIsRejected() {
        recognizer.recognize(emptyList(), templates)
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeThresholdIsRejected() {
        GestureRecognizer(DtwComparator(EuclideanDistance()), threshold = -1.0)
    }
}