package ua.makostream.gesturedtw.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.io.File
import ua.makostream.gesturedtw.data.repository.JsonGestureRepository
import ua.makostream.gesturedtw.data.sensor.AndroidSensorSource
import ua.makostream.gesturedtw.domain.dtw.DtwComparator
import ua.makostream.gesturedtw.domain.dtw.EuclideanDistance
import ua.makostream.gesturedtw.domain.recognizer.GestureRecognizer

class RecognitionViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(RecognitionViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        val recognizer = GestureRecognizer(
            comparator = DtwComparator(EuclideanDistance()),
            threshold = THRESHOLD
        )
        return RecognitionViewModel(
            sensor = AndroidSensorSource(appContext),
            repository = JsonGestureRepository(File(appContext.filesDir, "templates.json")),
            recognizer = recognizer
        ) as T
    }

    companion object {
        /** Largest normalized DTW distance accepted as a match. Tune using the logs. */
        const val THRESHOLD = 3.0
    }
}