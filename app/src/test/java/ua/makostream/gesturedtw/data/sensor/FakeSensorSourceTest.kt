package ua.makostream.gesturedtw.data.sensor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.makostream.gesturedtw.domain.model.AccelerometerSample

class FakeSensorSourceTest {

    private fun s(x: Float) = AccelerometerSample(x, 0f, 0f)

    @Test
    fun deliversAllSamplesInOrder() {
        val samples = listOf(s(1f), s(2f), s(3f))
        val received = mutableListOf<AccelerometerSample>()

        val started = FakeSensorSource(samples).start { received += it }

        assertTrue(started)
        assertEquals(samples, received)
    }

    @Test
    fun stopFromListenerHaltsDelivery() {
        val source = FakeSensorSource(listOf(s(1f), s(2f), s(3f), s(4f)))
        val received = mutableListOf<AccelerometerSample>()

        source.start {
            received += it
            if (received.size == 2) source.stop()
        }

        assertEquals(2, received.size)
    }
}