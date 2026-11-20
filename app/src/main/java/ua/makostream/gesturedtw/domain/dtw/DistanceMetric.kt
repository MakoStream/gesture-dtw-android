package ua.makostream.gesturedtw.domain.dtw

import ua.makostream.gesturedtw.domain.model.AccelerometerSample

/** Distance between two single accelerometer readings. */
interface DistanceMetric {
    fun distance(a: AccelerometerSample, b: AccelerometerSample): Double
}