package com.sonde.mentalfitness.domain

import com.sonde.mentalfitness.domain.model.checkin.OptionModel
import com.sonde.mentalfitness.domain.model.checkin.session.SondePlatformDetailModel
import java.util.ArrayList

object CheckInSessionManager {


    var checkInSessionId: String? = null
    var sondePlatformDetail: SondePlatformDetailModel? = null

    var selectedFeelingAnswerOption: OptionModel? = null
    var selectedFeelingReasonList: ArrayList<OptionModel>? = null

    fun clearSession() {
        selectedFeelingReasonList = null
        selectedFeelingAnswerOption = null
        checkInSessionId = null
        sondePlatformDetail = null
    }
}