package ua.makostream.gesturedtw.data.sensor

import ua.makostream.gesturedtw.domain.model.AccelerometerSample

/** A source of accelerometer readings (real sensor or a fake one for tests). */
interface SensorSource {

    /**
     * Starts delivering samples to [listener].
     * @return false if the source is unavailable (e.g. the device has no accelerometer)
     */
    fun start(listener: (AccelerometerSample) -> Unit): Boolean

    fun stop()
}