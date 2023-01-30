package com.sonde.mentalfitness.data.local.sharedpref

import com.sonde.mentalfitness.data.model.checkin.CheckInConfigResponse
import com.sonde.mentalfitness.domain.model.SegmentScore
import java.util.Date

interface SharedPreferenceService {

    fun clearPrefs()

    fun setIdToken(idToken: String)
    fun getIdToken(): String

    fun setCheckInConfigData(checkInConfigData: CheckInConfigResponse)

    fun setCheckInConfigData(checkInConfig: String)

    fun getCheckInConfigData(): CheckInConfigResponse

    fun setUserPin(pin: String)

    fun getUserPin(): String

    fun setNoPinOption()

    fun getNoPinOption(): Boolean

    fun isQuestionnaireSkipped(): Boolean

    fun setQuestionnaireSkipped(isSkipped: Boolean)

    fun isVoiceEnrollmentDone(): Boolean

    fun setVoiceEnrollmentDone();

    fun isRecordingServiceRunning(): Boolean

    fun setRecordingServiceRunning(isRunning: Boolean)

    fun isSoundTriggerServiceRunning(): Boolean

    fun setSoundTriggerServiceRunning(isRunning: Boolean)

    fun setSegmentScoreList(segmentScoreList: ArrayList<SegmentScore>)

    fun getSegmentScoreList(): ArrayList<SegmentScore>

    fun clearSegmentScoreList()

    fun setTotalTimeElapsed()

    fun getTotalTimeElapsed(): Long

    fun setDemoType(demoType: String)

    fun getDemoType(): String
}