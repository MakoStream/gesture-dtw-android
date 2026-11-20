package ua.makostream.gesturedtw.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlin.math.sin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ua.makostream.gesturedtw.data.repository.GestureRepository
import ua.makostream.gesturedtw.data.sensor.FakeSensorSource
import ua.makostream.gesturedtw.data.sensor.SensorSource
import ua.makostream.gesturedtw.domain.dtw.DtwComparator
import ua.makostream.gesturedtw.domain.dtw.EuclideanDistance
import ua.makostream.gesturedtw.domain.model.AccelerometerSample
import ua.makostream.gesturedtw.domain.model.GestureTemplate
import ua.makostream.gesturedtw.domain.model.RecognitionResult
import ua.makostream.gesturedtw.domain.recognizer.GestureRecognizer

class RecognitionViewModelTest {

    @get:Rule
    val instantExecutor = InstantTaskExecutorRule()

    private class InMemoryRepository(initial: List<GestureTemplate> = emptyList()) : GestureRepository {
        val items = initial.toMutableList()
        override fun loadAll() = items.toList()
        override fun add(template: GestureTemplate) {
            items += template
        }
        override fun removeAll(gestureId: String) {
            items.removeAll { it.gestureId == gestureId }
        }
    }

    private class UnavailableSensor : SensorSource {
        override fun start(listener: (AccelerometerSample) -> Unit) = false
        override fun stop() = Unit
    }

    private val recognizer = GestureRecognizer(DtwComparator(EuclideanDistance()), threshold = 0.5)

    private fun wave() = List(20) { AccelerometerSample(sin(it / 3f), 0f, 0f) }
    private fun far() = List(20) { AccelerometerSample(50f, 0f, 0f) }
    private fun repoWithWave() = InMemoryRepository(listOf(GestureTemplate("wave", wave())))

    @Test
    fun recognizesRecordedGesture() {
        val vm = RecognitionViewModel(FakeSensorSource(wave()), repoWithWave(), recognizer)

        vm.startRecognition()
        assertTrue(vm.state.value!!.isRecording)
        vm.finishRecognition()

        val state = vm.state.value!!
        assertFalse(state.isRecording)
        val result = state.result as RecognitionResult.Match
        assertEquals("wave", result.gestureId)
    }

    @Test
    fun farMovementIsUnknown() {
        val vm = RecognitionViewModel(FakeSensorSource(far()), repoWithWave(), recognizer)

        vm.startRecognition()
        vm.finishRecognition()

        assertTrue(vm.state.value!!.result is RecognitionResult.Unknown)
    }

    @Test
    fun withoutTemplatesRecognitionDoesNotStart() {
        val vm = RecognitionViewModel(FakeSensorSource(wave()), InMemoryRepository(), recognizer)

        vm.startRecognition()

        val state = vm.state.value!!
        assertFalse(state.isRecording)
        assertEquals(RecognitionMessage.NoTemplates, state.message)
    }

    @Test
    fun tooShortCaptureGivesMessageAndNoResult() {
        val vm = RecognitionViewModel(FakeSensorSource(wave().take(3)), repoWithWave(), recognizer)

        vm.startRecognition()
        vm.finishRecognition()

        val state = vm.state.value!!
        assertEquals(RecognitionMessage.TooShort, state.message)
        assertNull(state.result)
    }

    @Test
    fun unavailableSensorDoesNotStart() {
        val vm = RecognitionViewModel(UnavailableSensor(), repoWithWave(), recognizer)

        vm.startRecognition()

        assertFalse(vm.state.value!!.isRecording)
        assertEquals(RecognitionMessage.SensorUnavailable, vm.state.value!!.message)
    }

    @Test
    fun cancelledCaptureProducesNoResult() {
        val vm = RecognitionViewModel(FakeSensorSource(wave()), repoWithWave(), recognizer)

        vm.startRecognition()
        vm.cancelRecognition()

        assertFalse(vm.state.value!!.isRecording)
        assertNull(vm.state.value!!.result)
    }

    @Test
    fun templateCountIsLoadedOnCreation() {
        val vm = RecognitionViewModel(FakeSensorSource(emptyList()), repoWithWave(), recognizer)

        assertEquals(1, vm.state.value!!.templateCount)
    }
}