package com.example.activelife.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.sqrt

class DevicePostureSensor(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val liveState: Flow<String> = callbackFlow {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]

                    // Calculate total movement magnitude
                    val magnitude = sqrt((x * x + y * y + z * z).toDouble())

                    // State Logic based on Gravity and Acceleration
                    val state = when {
                        // ACTIVE: Significant movement / bouncing (Walking/Running)
                        magnitude > 12.0 || magnitude < 7.0 -> "ACTIVE"

                        // STILL: Flat on a table (Z-axis takes all gravity ~9.8, X and Y are near 0)
                        z > 8.5 && Math.abs(x) < 2.0 && Math.abs(y) < 2.0 -> "STILL"

                        // READY: In hand / tilted (Gravity is distributed across axes)
                        else -> "READY"
                    }

                    trySend(state)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        // We use SENSOR_DELAY_NORMAL to save battery
        sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_NORMAL)

        awaitClose { sensorManager.unregisterListener(listener) }
    }
}