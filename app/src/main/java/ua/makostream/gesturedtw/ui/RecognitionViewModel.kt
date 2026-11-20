package ua.makostream.gesturedtw.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import timber.log.Timber
import ua.makostream.gesturedtw.data.repository.GestureRepository
import ua.makostream.gesturedtw.data.sensor.SensorSource
import ua.makostream.gesturedtw.domain.model.AccelerometerSample
import ua.makostream.gesturedtw.domain.recognizer.GestureRecognizer

class RecognitionViewModel(
    private val sensor: SensorSource,
    private val repository: GestureRepository,
    private val recognizer: GestureRecognizer
) : ViewModel() {

    private val buffer = mutableListOf<AccelerometerSample>()
    private val _state = MutableLiveData(RecognitionUiState())
    val state: LiveData<RecognitionUiState> = _state

    private val current: RecognitionUiState get() = _state.value ?: RecognitionUiState()

    init {
        refreshTemplateCount()
    }

    /** Call when the screen becomes visible: templates may have been recorded meanwhile. */
    fun refreshTemplateCount() {
        _state.value = current.copy(templateCount = repository.loadAll().size)
    }

    fun startRecognition() {
        if (current.isRecording) return
        if (repository.loadAll().isEmpty()) {
            Timber.w("Recognition not started: no templates recorded")
            _state.value = current.copy(message = RecognitionMessage.NoTemplates)
            return
        }
        buffer.clear()
        if (sensor.start { buffer += it }) {
            Timber.i("Recognition capture started")
            _state.value = current.copy(isRecording = true, result = null)
        } else {
            Timber.w("Recognition not started: sensor unavailable")
            _state.value = current.copy(message = RecognitionMessage.SensorUnavailable)
        }
    }

    fun finishRecognition() {
        if (!current.isRecording) return
        sensor.stop()
        val samples = buffer.toList()
        buffer.clear()

        if (samples.size < MIN_SAMPLES) {
            Timber.w("Capture too short: %d samples (min %d)", samples.size, MIN_SAMPLES)
            _state.value = current.copy(isRecording = false, message = RecognitionMessage.TooShort)
            return
        }

        val result = recognizer.recognize(samples, repository.loadAll())
        _state.value = current.copy(isRecording = false, result = result)
    }

    fun cancelRecognition() {
        if (!current.isRecording) return
        sensor.stop()
        buffer.clear()
        Timber.i("Recognition capture cancelled")
        _state.value = current.copy(isRecording = false)
    }

    fun messageShown() {
        if (current.message != null) _state.value = current.copy(message = null)
    }

    override fun onCleared() {
        sensor.stop()
    }

    private companion object {
        const val MIN_SAMPLES = 10
    }
}