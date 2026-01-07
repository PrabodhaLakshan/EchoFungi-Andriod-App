package com.example.myshroom

import android.content.Context
import android.content.SharedPreferences

class PreferenceHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("EchoPrefs", Context.MODE_PRIVATE)

    companion object {
        const val BLYNK_TOKEN = "blynk_token"
        const val TEMP_PIN = "temp_pin"
        const val HUMIDITY_PIN = "humidity_pin"
        const val RELAY_PIN = "relay_pin"
        const val BUZZER_PIN = "buzzer_pin"
        const val SCHEDULE_START_TIME = "schedule_start_time"
        const val SCHEDULE_END_TIME = "schedule_end_time"
        const val AUTO_MODE_ENABLED = "auto_mode_enabled"
        const val MAX_TEMP = "max_temp"
    }

    fun saveBlynkToken(token: String) {
        prefs.edit().putString(BLYNK_TOKEN, token).apply()
    }

    fun getBlynkToken(): String? {
        return prefs.getString(BLYNK_TOKEN, null)
    }

    fun savePin(pinType: String, pinValue: String) {
        prefs.edit().putString(pinType, pinValue).apply()
    }

    fun getPin(pinType: String): String? {
        return prefs.getString(pinType, null)
    }

    fun saveScheduleStartTime(time: String) {
        prefs.edit().putString(SCHEDULE_START_TIME, time).apply()
    }

    fun getScheduleStartTime(): String? {
        return prefs.getString(SCHEDULE_START_TIME, null)
    }

    fun saveScheduleEndTime(time: String) {
        prefs.edit().putString(SCHEDULE_END_TIME, time).apply()
    }

    fun getScheduleEndTime(): String? {
        return prefs.getString(SCHEDULE_END_TIME, null)
    }

    fun setAutoModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(AUTO_MODE_ENABLED, enabled).apply()
    }

    fun isAutoModeEnabled(): Boolean {
        return prefs.getBoolean(AUTO_MODE_ENABLED, false)
    }

    fun saveMaxTemp(temp: Float) {
        prefs.edit().putFloat(MAX_TEMP, temp).apply()
    }

    fun getMaxTemp(): Float {
        return prefs.getFloat(MAX_TEMP, 30f)
    }
}

