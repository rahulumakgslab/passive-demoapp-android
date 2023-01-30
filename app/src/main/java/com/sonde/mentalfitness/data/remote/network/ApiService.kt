package com.sonde.mentalfitness.data.remote.network

import com.sonde.mentalfitness.data.model.checkin.CheckInConfigResponse
import com.sonde.mentalfitness.data.model.checkin.queanswers.QuestionnaireAnswersRequest
import com.sonde.mentalfitness.data.model.checkin.session.CheckInCreateSessionRequestData
import com.sonde.mentalfitness.data.model.checkin.session.CreateCheckInSessionResponseData
import okhttp3.ResponseBody
import retrofit2.http.*

const val AUTHORIZATION = "Authorization";

interface ApiService {

    @POST("user-management/users/login")
    fun login()

    // Check-In
    @GET("check-in/name/{checkInName}")
    suspend fun fetchCheckInConfiguration(
        @Path("checkInName", encoded = true) checkInName: String
    ): CheckInConfigResponse

    @POST("/check-in/{checkInInstanceId}/sessions")
    suspend fun createCheckInSession(
        @Path("checkInInstanceId") checkInName: String,
        @Body checkInCreateSessionRequestData: CheckInCreateSessionRequestData
    ): CreateCheckInSessionResponseData

    @PUT("/check-in/sessions/{sessionId}/questionnaire-response")
    suspend fun submitQuestionnaireAnswer(
        @Path("sessionId") sessionId: String,
        @Body questionnaireAnswersRequest: QuestionnaireAnswersRequest
    ) : ResponseBody
}