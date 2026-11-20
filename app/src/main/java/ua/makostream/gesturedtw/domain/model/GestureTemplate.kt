package ua.makostream.gesturedtw.domain.model

/**
 * A reference recording of a gesture.
 * [gestureId] is a stable key (e.g. "shake", "circle"), not a display name:
 * the UI maps it to a localized string.
 */
data class GestureTemplate(
    val gestureId: String,
    val samples: List<AccelerometerSample>
) {
    init {
        require(gestureId.isNotBlank()) { "gestureId must not be blank" }
        require(samples.isNotEmpty()) { "samples must not be empty" }
    }
}