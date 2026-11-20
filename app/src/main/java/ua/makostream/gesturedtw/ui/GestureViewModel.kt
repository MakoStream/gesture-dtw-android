package ua.makostream.gesturedtw.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import timber.log.Timber
import ua.makostream.gesturedtw.data.repository.GestureRepository
import ua.makostream.gesturedtw.data.sensor.SensorSource
import ua.makostream.gesturedtw.domain.model.AccelerometerSample
import ua.makostream.gesturedtw.domain.model.GestureTemplate

class GestureViewModel(
    private val sensor: SensorSource,
    private val repository: GestureRepository
) : ViewModel() {

    private val buffer = mutableListOf<AccelerometerSample>()
    private val _state = MutableLiveData(GestureUiState())
    val state: LiveData<GestureUiState> = _state

    private val current: GestureUiState get() = _state.value ?: GestureUiState()

    init {
        _state.value = current.copy(templateCounts = countTemplates())
    }

    fun startRecording() {
        if (current.isRecording) return
        buffer.clear()
        if (sensor.start { buffer += it }) {
            Timber.i("Recording started")
            _state.value = current.copy(isRecording = true)
        } else {
            Timber.w("Recording not started: sensor unavailable")
            _state.value = current.copy(message = UiMessage.SensorUnavailable)
        }
    }

    fun stopRecording(gestureId: String) {
        if (!current.isRecording) return
        sensor.stop()
        val samples = buffer.toList()
        buffer.clear()

        if (samples.size < MIN_SAMPLES) {
            Timber.w("Recording too short: %d samples (min %d)", samples.size, MIN_SAMPLES)
            _state.value = current.copy(isRecording = false, message = UiMessage.TooShort)
            return
        }

        repository.add(GestureTemplate(gestureId, samples))
        Timber.i("Recording saved for '%s': %d samples", gestureId, samples.size)
        _state.value = current.copy(
            isRecording = false,
            templateCounts = countTemplates(),
            message = UiMessage.Saved(gestureId, samples.size)
        )
    }

    /** Stops recording and throws the data away (e.g. the app went to background). */
    fun cancelRecording() {
        if (!current.isRecording) return
        sensor.stop()
        buffer.clear()
        Timber.i("Recording cancelled")
        _state.value = current.copy(isRecording = false)
    }

    fun deleteGesture(gestureId: String) {
        repository.removeAll(gestureId)
        _state.value = current.copy(
            templateCounts = countTemplates(),
            message = UiMessage.Deleted(gestureId)
        )
    }

    fun messageShown() {
        if (current.message != null) _state.value = current.copy(message = null)
    }

    override fun onCleared() {
        sensor.stop()
    }

    private fun countTemplates(): Map<String, Int> =
        repository.loadAll().groupingBy { it.gestureId }.eachCount()

    private companion object {
        /** About 0.2 s at SENSOR_DELAY_GAME; shorter recordings are accidental taps. */
        const val MIN_SAMPLES = 10
    }
}