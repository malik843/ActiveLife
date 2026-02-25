package com.example.activelife

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log

public class SensorActivity(savedInstanceState1: Bundle?) : Activity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var stepSensor: Sensor

    override fun onCreate(savedInstanceState: Bundle?) {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        Log.d("SensorActivity", "onSensorChanged: ${event?.values?.get(0)}")
    }
}