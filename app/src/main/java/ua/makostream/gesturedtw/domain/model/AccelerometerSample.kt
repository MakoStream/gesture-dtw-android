package ua.makostream.gesturedtw.domain.model

/** One accelerometer reading: acceleration along x, y, z (m/s²) at a given time. */
data class AccelerometerSample(
    val x: Float,
    val y: Float,
    val z: Float,
    val timestampNanos: Long = 0L
)