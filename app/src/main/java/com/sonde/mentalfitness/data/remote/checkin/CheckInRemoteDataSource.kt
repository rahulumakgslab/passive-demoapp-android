package com.sonde.mentalfitness.data.remote.checkin

import com.sonde.mentalfitness.data.Result
import com.sonde.mentalfitness.data.model.checkin.CheckInConfigResponse
import com.sonde.mentalfitness.data.model.checkin.queanswers.QuestionnaireAnswersRequest
import com.sonde.mentalfitness.data.model.checkin.session.CheckInCreateSessionRequestData
import com.sonde.mentalfitness.data.model.checkin.session.CreateCheckInSessionResponseData
import okhttp3.ResponseBody

interface CheckInRemoteDataSource {
    suspend fun fetchCheckInConfiguration(checkInName: String): Result<CheckInConfigResponse>

    suspend fun createCheckInSession(
        checkInInstanceId: String,
        checkInCreateSessionRequestData: CheckInCreateSessionRequestData
    ): Result<CreateCheckInSessionResponseData>

    suspend fun submitQuestionnaireAnswer(
        sessionId: String,
        questionnaireAnswersRequest: QuestionnaireAnswersRequest
    ): Result<ResponseBody>
}