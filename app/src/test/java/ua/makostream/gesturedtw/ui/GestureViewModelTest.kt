package ua.makostream.gesturedtw.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ua.makostream.gesturedtw.data.repository.GestureRepository
import ua.makostream.gesturedtw.data.sensor.FakeSensorSource
import ua.makostream.gesturedtw.data.sensor.SensorSource
import ua.makostream.gesturedtw.domain.model.AccelerometerSample
import ua.makostream.gesturedtw.domain.model.GestureTemplate

class GestureViewModelTest {

    @get:Rule
    val instantExecutor = InstantTaskExecutorRule()

    private class InMemoryRepository : GestureRepository {
        val items = mutableListOf<GestureTemplate>()
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

    private fun samples(n: Int) = List(n) { AccelerometerSample(it.toFloat(), 0f, 0f) }

    @Test
    fun recordingIsSavedAsTemplate() {
        val repo = InMemoryRepository()
        val vm = GestureViewModel(FakeSensorSource(samples(20)), repo)

        vm.startRecording()
        assertTrue(vm.state.value!!.isRecording)
        vm.stopRecording("shake")

        val state = vm.state.value!!
        assertFalse(state.isRecording)
        assertEquals(1, repo.items.size)
        assertEquals(1, state.templateCounts["shake"])
        assertEquals(UiMessage.Saved("shake", 20), state.message)
    }

    @Test
    fun tooShortRecordingIsNotSaved() {
        val repo = InMemoryRepository()
        val vm = GestureViewModel(FakeSensorSource(samples(3)), repo)

        vm.startRecording()
        vm.stopRecording("shake")

        assertTrue(repo.items.isEmpty())
        assertEquals(UiMessage.TooShort, vm.state.value!!.message)
    }

    @Test
    fun unavailableSensorDoesNotStartRecording() {
        val vm = GestureViewModel(UnavailableSensor(), InMemoryRepository())

        vm.startRecording()

        assertFalse(vm.state.value!!.isRecording)
        assertEquals(UiMessage.SensorUnavailable, vm.state.value!!.message)
    }

    @Test
    fun cancelledRecordingIsDiscarded() {
        val repo = InMemoryRepository()
        val vm = GestureViewModel(FakeSensorSource(samples(20)), repo)

        vm.startRecording()
        vm.cancelRecording()

        assertFalse(vm.state.value!!.isRecording)
        assertTrue(repo.items.isEmpty())
    }

    @Test
    fun deleteGestureRemovesItsTemplates() {
        val repo = InMemoryRepository()
        repo.add(GestureTemplate("shake", samples(15)))
        repo.add(GestureTemplate("circle", samples(15)))
        val vm = GestureViewModel(FakeSensorSource(emptyList()), repo)

        vm.deleteGesture("shake")

        assertEquals(mapOf("circle" to 1), vm.state.value!!.templateCounts)
        assertEquals(UiMessage.Deleted("shake"), vm.state.value!!.message)
    }

    @Test
    fun messageShownClearsMessage() {
        val vm = GestureViewModel(UnavailableSensor(), InMemoryRepository())
        vm.startRecording()

        vm.messageShown()

        assertEquals(null, vm.state.value!!.message)
    }
}