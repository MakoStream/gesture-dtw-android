package ua.makostream.gesturedtw.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import timber.log.Timber
import ua.makostream.gesturedtw.domain.model.AccelerometerSample

/**
 * Reads the device accelerometer.
 *
 * Prefers TYPE_LINEAR_ACCELERATION (accelerometer data with gravity already removed,
 * so the phone's orientation does not distort the gesture). If the device does not
 * have it, falls back to the raw TYPE_ACCELEROMETER.
 */
class AndroidSensorSource(context: Context) : SensorSource {

    private val sensorManager =
        context.applicationContext.getSystemService(SensorManager::class.java)

    private var activeListener: SensorEventListener? = null

    override fun start(listener: (AccelerometerSample) -> Unit): Boolean {
        if (activeListener != null) {
            Timber.w("Sensor source already started")
            return true
        }

        val linear = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        val sensor = linear ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensor == null) {
            Timber.w("No accelerometer available on this device")
            return false
        }
        if (linear == null) {
            Timber.w("Linear acceleration not available, using raw accelerometer (includes gravity)")
        }

        val eventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                listener(
                    AccelerometerSample(
                        x = event.values[0],
                        y = event.values[1],
                        z = event.values[2],
                        timestampNanos = event.timestamp
                    )
                )
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }

        val registered = sensorManager.registerListener(
            eventListener, sensor, SensorManager.SENSOR_DELAY_GAME
        )
        if (!registered) {
            Timber.e("Failed to register listener for sensor '%s'", sensor.name)
            return false
        }

        activeListener = eventListener
        Timber.i("Sensor started: '%s'", sensor.name)
        return true
    }

    override fun stop() {
        activeListener?.let {
            sensorManager.unregisterListener(it)
            Timber.i("Sensor stopped")
        }
        activeListener = null
    }
}