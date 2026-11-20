package ua.makostream.gesturedtw.domain.dtw

import kotlin.math.sqrt
import ua.makostream.gesturedtw.domain.model.AccelerometerSample

/** Euclidean distance in 3D: sqrt(dx² + dy² + dz²). */
class EuclideanDistance : DistanceMetric {
    override fun distance(a: AccelerometerSample, b: AccelerometerSample): Double {
        val dx = (a.x - b.x).toDouble()
        val dy = (a.y - b.y).toDouble()
        val dz = (a.z - b.z).toDouble()
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}