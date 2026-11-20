package ua.makostream.gesturedtw.domain.model

/** Outcome of comparing a recorded movement with the known templates. */
sealed interface RecognitionResult {

    /** The closest template is within the threshold. */
    data class Match(val gestureId: String, val distance: Double) : RecognitionResult

    /** No template is close enough (or there are no templates). */
    data class Unknown(val bestDistance: Double?) : RecognitionResult
}