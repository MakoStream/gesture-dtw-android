package ua.makostream.gesturedtw.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.io.File
import ua.makostream.gesturedtw.data.repository.JsonGestureRepository
import ua.makostream.gesturedtw.data.sensor.AndroidSensorSource

class GestureViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(GestureViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return GestureViewModel(
            sensor = AndroidSensorSource(appContext),
            repository = JsonGestureRepository(File(appContext.filesDir, "templates.json"))
        ) as T
    }
}