package ua.makostream.gesturedtw.ui

/** One-time message for the user; the Activity turns it into a localized string. */
sealed interface UiMessage {
    data class Saved(val gestureId: String, val sampleCount: Int) : UiMessage
    data class Deleted(val gestureId: String) : UiMessage
    data object TooShort : UiMessage
    data object SensorUnavailable : UiMessage
}

data class GestureUiState(
    val isRecording: Boolean = false,
    val templateCounts: Map<String, Int> = emptyMap(),
    val message: UiMessage? = null
)