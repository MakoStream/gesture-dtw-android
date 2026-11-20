package ua.makostream.gesturedtw

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ua.makostream.gesturedtw.domain.model.RecognitionResult
import ua.makostream.gesturedtw.ui.GestureCatalog
import ua.makostream.gesturedtw.ui.RecognitionMessage
import ua.makostream.gesturedtw.ui.RecognitionUiState
import ua.makostream.gesturedtw.ui.RecognitionViewModel
import ua.makostream.gesturedtw.ui.RecognitionViewModelFactory

class RecognitionActivity : AppCompatActivity() {

    private val viewModel: RecognitionViewModel by viewModels {
        RecognitionViewModelFactory(applicationContext)
    }

    private lateinit var templatesText: TextView
    private lateinit var resultText: TextView
    private lateinit var recognizeButton: Button

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recognition)

        templatesText = findViewById(R.id.templatesText)
        resultText = findViewById(R.id.resultText)
        recognizeButton = findViewById(R.id.recognizeButton)

        recognizeButton.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> viewModel.startRecognition()
                MotionEvent.ACTION_UP -> {
                    v.performClick()
                    viewModel.finishRecognition()
                }
                MotionEvent.ACTION_CANCEL -> viewModel.cancelRecognition()
            }
            true
        }

        viewModel.state.observe(this) { render(it) }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshTemplateCount()
    }

    override fun onPause() {
        super.onPause()
        viewModel.cancelRecognition()
    }

    private fun render(state: RecognitionUiState) {
        templatesText.text = getString(R.string.recognition_templates, state.templateCount)
        recognizeButton.setText(
            if (state.isRecording) R.string.recognize_release else R.string.recognize_hold
        )

        val result = state.result
        if (state.isRecording) {
            resultText.setText(R.string.status_recording)
        } else if (result != null) {
            resultText.text = resultText(result)
        }

        state.message?.let {
            resultText.text = messageText(it)
            viewModel.messageShown()
        }
    }

    private fun resultText(result: RecognitionResult): String = when (result) {
        is RecognitionResult.Match -> getString(
            R.string.recognition_result,
            getString(GestureCatalog.labelFor(result.gestureId)),
            result.distance
        )
        is RecognitionResult.Unknown -> result.bestDistance
            ?.let { getString(R.string.result_unknown, it) }
            ?: getString(R.string.gesture_unknown)
    }

    private fun messageText(message: RecognitionMessage): String = when (message) {
        is RecognitionMessage.TooShort -> getString(R.string.msg_too_short)
        is RecognitionMessage.SensorUnavailable -> getString(R.string.msg_sensor_unavailable)
        is RecognitionMessage.NoTemplates -> getString(R.string.msg_no_templates)
    }
}