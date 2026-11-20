package ua.makostream.gesturedtw.data.sensor

import ua.makostream.gesturedtw.domain.model.AccelerometerSample

/** Replays prepared samples synchronously. Used in tests instead of a real sensor. */
class FakeSensorSource(private val samples: List<AccelerometerSample>) : SensorSource {

    @Volatile
    private var running = false

    override fun start(listener: (AccelerometerSample) -> Unit): Boolean {
        running = true
        for (sample in samples) {
            if (!running) break
            listener(sample)
        }
        running = false
        return true
    }

    override fun stop() {
        running = false
    }
}