package com.sonde.mentalfitness.data.local.sharedpref

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sonde.mentalfitness.Constants
import com.sonde.mentalfitness.data.model.checkin.CheckInConfigResponse
import com.sonde.mentalfitness.domain.model.SegmentScore
import java.util.Date


private const val PREFS_NAME = "mental_fitness_prefs"
private const val KEY_ID_TOKEN = "id_token"
private const val KEY_CHECK_IN_CONFIG = "check_in_config"
private const val KEY_USER_PIN = "user_pin"
private const val KEY_NO_PIN_OPTION = "no_pin_option"
private const val KEY_QUESIONNAIRE_SKIPPED = "is_questionnaire_skipped"
private const val KEY_VOICE_ENROLLMENT = "is_voice_enrollment_done"
private const val KEY_RECORDING_SERVICE = "is_recording_service_running"
private const val KEY_SOUND_TRIGGER_SERVICE = "is_sound_trigger_service_running"
private const val KEY_SEGMENT_SCORE = "segment_score"
private const val KEY_TOTAL_TIME_ELAPSED = "total_time_elapsed"
private const val KEY_DEMO_TYPE = "demo_type"

class SharedPreferenceServiceImpl constructor(context: Context) : SharedPreferenceService {

    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun setUserPin(pin: String) {
        preferences.edit().putString(KEY_USER_PIN, pin).apply()
    }

    override fun getUserPin(): String {
        return preferences.getString(KEY_USER_PIN, "") ?: ""
    }

    override fun setNoPinOption() {
        preferences.edit().putBoolean(KEY_NO_PIN_OPTION, true).apply()
    }

    override fun getNoPinOption(): Boolean {
        return preferences.getBoolean(KEY_NO_PIN_OPTION, false)
    }

    override fun isQuestionnaireSkipped(): Boolean {
        return preferences.getBoolean(KEY_QUESIONNAIRE_SKIPPED, false)
    }

    override fun setQuestionnaireSkipped(isSkipped: Boolean) {
        return preferences.edit().putBoolean(KEY_QUESIONNAIRE_SKIPPED, isSkipped).apply()
    }

    override fun isVoiceEnrollmentDone(): Boolean {
        return preferences.getBoolean(KEY_VOICE_ENROLLMENT, false)
    }

    override fun setVoiceEnrollmentDone() {
        preferences.edit().putBoolean(KEY_VOICE_ENROLLMENT, true).apply()
    }

    override fun isRecordingServiceRunning(): Boolean {
        return preferences.getBoolean(KEY_RECORDING_SERVICE, false)
    }

    override fun setRecordingServiceRunning(isRunning: Boolean) {
        preferences.edit().putBoolean(KEY_RECORDING_SERVICE, isRunning).apply()
    }

    override fun isSoundTriggerServiceRunning(): Boolean {
        return preferences.getBoolean(KEY_SOUND_TRIGGER_SERVICE, false)
    }

    override fun setSoundTriggerServiceRunning(isRunning: Boolean) {
        preferences.edit().putBoolean(KEY_SOUND_TRIGGER_SERVICE, isRunning).apply()
    }

    override fun setSegmentScoreList(segmentScoreList: ArrayList<SegmentScore>) {
        val jsonStr = Gson().toJson(segmentScoreList)
        preferences.edit().putString(KEY_SEGMENT_SCORE, jsonStr).apply()
    }

    override fun getSegmentScoreList(): ArrayList<SegmentScore> {
        val jsonStr = preferences.getString(KEY_SEGMENT_SCORE, null)
        if (jsonStr != null) {
            val type = object : TypeToken<ArrayList<SegmentScore?>?>() {}.type
            return Gson().fromJson(jsonStr, type)
        } else {
            val blankString = """[]"""
            val type = object : TypeToken<ArrayList<SegmentScore?>?>() {}.type
            return Gson().fromJson(blankString, type)
        }

    }

    override fun clearSegmentScoreList() {
        val jsonStr = """[]"""
        preferences.edit().putString(KEY_SEGMENT_SCORE, jsonStr).apply()
    }

    override fun setTotalTimeElapsed() {
        preferences.edit().putLong(KEY_TOTAL_TIME_ELAPSED, Date().time).apply()
    }

    override fun getTotalTimeElapsed(): Long {
        return preferences.getLong(KEY_TOTAL_TIME_ELAPSED, Date().time)
    }

    override fun setDemoType(demoType: String) {
        preferences.edit().putString(KEY_DEMO_TYPE, demoType).apply()
    }

    override fun getDemoType(): String {
        return preferences.getString(KEY_DEMO_TYPE, Constants.APP_LEVEL_DEMO) ?: Constants.APP_LEVEL_DEMO
    }

    override fun clearPrefs() {
        preferences.edit().clear().apply()
    }

    override fun setIdToken(idToken: String) {
        preferences[KEY_ID_TOKEN] = idToken
    }

    override fun getIdToken(): String {
        return preferences[KEY_ID_TOKEN] ?: ""
    }

    override fun setCheckInConfigData(checkInConfigData: CheckInConfigResponse) {
        val jsonStr = Gson().toJson(checkInConfigData)
        preferences.edit().putString(KEY_CHECK_IN_CONFIG, jsonStr).apply()
    }

    override fun setCheckInConfigData(checkInConfig: String) {
        preferences.edit().putString(KEY_CHECK_IN_CONFIG, checkInConfig).apply()
    }

    override fun getCheckInConfigData(): CheckInConfigResponse {
        val jsonStr = preferences.getString(KEY_CHECK_IN_CONFIG, null)
        return Gson().fromJson(jsonStr, CheckInConfigResponse::class.java)
    }
}

/**
 * SharedPreferences extension function, to listen the edit() and apply() fun calls
 * on every SharedPreferences operation.
 */
private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
    val editor = this.edit()
    operation(editor)
    editor.apply()
}

/**
 * puts a key value pair in shared prefs if doesn't exists, otherwise updates value on given [key]
 */
private operator fun SharedPreferences.set(key: String, value: Any?) {
    when (value) {
        is String? -> edit { it.putString(key, value) }
        is Int -> edit { it.putInt(key, value) }
        is Boolean -> edit { it.putBoolean(key, value) }
        is Float -> edit { it.putFloat(key, value) }
        is Long -> edit { it.putLong(key, value) }
        else -> throw UnsupportedOperationException("Not yet implemented")
    }
}

/**
 * finds value on given key.
 * [T] is the type of value
 * @param defaultValue optional default value - will take null for strings, false for bool and -1 for numeric values if [defaultValue] is not specified
 */
private inline operator fun <reified T : Any> SharedPreferences.get(
    key: String,
    defaultValue: T? = null
): T? {
    return when (T::class) {
        String::class -> getString(key, defaultValue as? String) as T?
        Int::class -> getInt(key, defaultValue as? Int ?: -1) as T?
        Boolean::class -> getBoolean(key, defaultValue as? Boolean ?: false) as T?
        Float::class -> getFloat(key, defaultValue as? Float ?: -1f) as T?
        Long::class -> getLong(key, defaultValue as? Long ?: -1) as T?
        else -> throw UnsupportedOperationException("Not yet implemented")
    }
}