package ua.makostream.gesturedtw.ui

import ua.makostream.gesturedtw.domain.model.RecognitionResult

sealed interface RecognitionMessage {
    data object TooShort : RecognitionMessage
    data object SensorUnavailable : RecognitionMessage
    data object NoTemplates : RecognitionMessage
}

data class RecognitionUiState(
    val isRecording: Boolean = false,
    val templateCount: Int = 0,
    val result: RecognitionResult? = null,
    val message: RecognitionMessage? = null
)