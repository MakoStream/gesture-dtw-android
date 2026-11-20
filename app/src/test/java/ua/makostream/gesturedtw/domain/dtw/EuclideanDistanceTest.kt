package ua.makostream.gesturedtw.domain.dtw

import org.junit.Assert.assertEquals
import org.junit.Test
import ua.makostream.gesturedtw.domain.model.AccelerometerSample

class EuclideanDistanceTest {

    private val metric = EuclideanDistance()

    @Test
    fun distanceToSelfIsZero() {
        val s = AccelerometerSample(1f, 2f, 3f)
        assertEquals(0.0, metric.distance(s, s), 1e-9)
    }

    @Test
    fun threeFourFiveTriangle() {
        val a = AccelerometerSample(0f, 0f, 0f)
        val b = AccelerometerSample(3f, 4f, 0f)
        assertEquals(5.0, metric.distance(a, b), 1e-9)
    }

    @Test
    fun usesAllThreeAxes() {
        val a = AccelerometerSample(0f, 0f, 0f)
        val b = AccelerometerSample(2f, 3f, 6f)
        assertEquals(7.0, metric.distance(a, b), 1e-9)
    }
}