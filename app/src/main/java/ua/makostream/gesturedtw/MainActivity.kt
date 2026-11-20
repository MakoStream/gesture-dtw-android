package ua.makostream.gesturedtw

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber
import ua.makostream.gesturedtw.ui.GestureCatalog
import ua.makostream.gesturedtw.ui.GestureUiState
import ua.makostream.gesturedtw.ui.GestureViewModel
import ua.makostream.gesturedtw.ui.GestureViewModelFactory
import ua.makostream.gesturedtw.ui.UiMessage

import android.content.Intent

class MainActivity : AppCompatActivity() {

    private val viewModel: GestureViewModel by viewModels {
        GestureViewModelFactory(applicationContext)
    }

    private lateinit var gestureSpinner: Spinner
    private lateinit var countText: TextView
    private lateinit var statusText: TextView
    private lateinit var recordButton: Button
    private lateinit var deleteButton: Button

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gestureSpinner = findViewById(R.id.gestureSpinner)
        countText = findViewById(R.id.countText)
        statusText = findViewById(R.id.statusText)
        recordButton = findViewById(R.id.recordButton)
        deleteButton = findViewById(R.id.deleteButton)

        val labels = GestureCatalog.all.map { getString(it.labelRes) }
        gestureSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)
        gestureSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                viewModel.state.value?.let { render(it) }
            }

            override fun onNothingSelected(p: AdapterView<*>?) = Unit
        }

        recordButton.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> viewModel.startRecording()
                MotionEvent.ACTION_UP -> {
                    v.performClick()
                    viewModel.stopRecording(selectedGestureId())
                }
                MotionEvent.ACTION_CANCEL -> viewModel.cancelRecording()
            }
            true
        }
        deleteButton.setOnClickListener { viewModel.deleteGesture(selectedGestureId()) }

        findViewById<Button>(R.id.openRecognitionButton).setOnClickListener {
            startActivity(Intent(this, RecognitionActivity::class.java))
        }
        
        viewModel.state.observe(this) { render(it) }
        Timber.d("MainActivity created")
    }

    override fun onPause() {
        super.onPause()
        viewModel.cancelRecording()
    }

    private fun selectedGestureId(): String =
        GestureCatalog.all[gestureSpinner.selectedItemPosition].id

    private fun render(state: GestureUiState) {
        val count = state.templateCounts[selectedGestureId()] ?: 0
        countText.text = getString(R.string.recordings_count, count)
        recordButton.setText(if (state.isRecording) R.string.record_release else R.string.record_hold)
        gestureSpinner.isEnabled = !state.isRecording
        deleteButton.isEnabled = !state.isRecording && count > 0

        if (state.isRecording) statusText.setText(R.string.status_recording)
        state.message?.let {
            statusText.text = messageText(it)
            viewModel.messageShown()
        }
    }

    private fun messageText(message: UiMessage): String = when (message) {
        is UiMessage.Saved -> getString(
            R.string.msg_saved, getString(GestureCatalog.labelFor(message.gestureId)), message.sampleCount
        )
        is UiMessage.Deleted -> getString(
            R.string.msg_deleted, getString(GestureCatalog.labelFor(message.gestureId))
        )
        is UiMessage.TooShort -> getString(R.string.msg_too_short)
        is UiMessage.SensorUnavailable -> getString(R.string.msg_sensor_unavailable)
    }
}