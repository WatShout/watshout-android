package com.watshout.watshout;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

class Barometer implements SensorEventListener {

    private float m_value;

    private static Barometer ourInstance = new Barometer();

    public static Barometer getInstance() {
        return ourInstance;
    }

    private Barometer() {
        m_value = 0;
    }

    public float getValue(){
        return m_value;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        m_value = event.values[0];
    }
}