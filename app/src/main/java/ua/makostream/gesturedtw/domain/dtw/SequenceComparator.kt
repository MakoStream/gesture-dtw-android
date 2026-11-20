package ua.makostream.gesturedtw.domain.dtw

import ua.makostream.gesturedtw.domain.model.AccelerometerSample

/** Measures how different two recorded movements are (0 = identical). */
interface SequenceComparator {
    fun distance(a: List<AccelerometerSample>, b: List<AccelerometerSample>): Double
}