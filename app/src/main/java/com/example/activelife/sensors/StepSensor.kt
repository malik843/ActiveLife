package com.example.activelife.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.LocalDate

class StepSensor(private val context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    val steps: Flow<Int> = callbackFlow {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val rawSteps = it.values[0].toInt()

                    // --- THE BASELINE LOGIC ---
                    val prefs = context.getSharedPreferences("ActiveLifePrefs", Context.MODE_PRIVATE)
                    val todayDate = LocalDate.now().toString()
                    val savedDate = prefs.getString("LastDate", "")

                    var baseline = prefs.getInt("BaselineSteps_${todayDate}", -1)

                    // If it's a new day, or we've never saved a baseline, set the current rawSteps as the new 0
                    if (savedDate != todayDate || baseline == -1) {
                        baseline = rawSteps
                        prefs.edit()
                            .putString("LastDate", todayDate)
                            .putInt("BaselineSteps_${todayDate}", baseline)
                            .apply()
                    }

                    // Calculate today's actual steps!
                    val actualStepsToday = rawSteps - baseline
                    // ---------------------------

                    trySend(actualStepsToday)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        // Register the listener
        sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_UI)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}