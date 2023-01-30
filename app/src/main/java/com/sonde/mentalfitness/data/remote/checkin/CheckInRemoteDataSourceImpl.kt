package com.sonde.mentalfitness.data.remote.checkin


import com.sonde.mentalfitness.data.CoroutinesExtension.safeApiCall
import com.sonde.mentalfitness.data.Result
import com.sonde.mentalfitness.data.model.checkin.CheckInConfigResponse
import com.sonde.mentalfitness.data.model.checkin.queanswers.QuestionnaireAnswersRequest
import com.sonde.mentalfitness.data.model.checkin.session.CheckInCreateSessionRequestData
import com.sonde.mentalfitness.data.model.checkin.session.CreateCheckInSessionResponseData
import com.sonde.mentalfitness.data.remote.network.ApiService
import kotlinx.coroutines.CoroutineDispatcher
import okhttp3.ResponseBody

class CheckInRemoteDataSourceImpl(
    private val apiService: ApiService,
    private val ioDispatcher: CoroutineDispatcher
) : CheckInRemoteDataSource {

    override suspend fun fetchCheckInConfiguration(checkInName: String): Result<CheckInConfigResponse> {
        return safeApiCall(ioDispatcher) {
            apiService.fetchCheckInConfiguration(checkInName)
        }
    }

    override suspend fun createCheckInSession(
        checkInInstanceId: String,
        checkInCreateSessionRequestData: CheckInCreateSessionRequestData
    ): Result<CreateCheckInSessionResponseData> {
        return safeApiCall(ioDispatcher) {
            apiService.createCheckInSession(checkInInstanceId, checkInCreateSessionRequestData)
        }
    }

    override suspend fun submitQuestionnaireAnswer(
        sessionId: String,
        questionnaireAnswersRequest: QuestionnaireAnswersRequest
    ): Result<ResponseBody> {
        return safeApiCall(ioDispatcher) {
            apiService.submitQuestionnaireAnswer(sessionId, questionnaireAnswersRequest)
        }.let {
            when (it) {
                is Result.Success -> {
                    Result.Success(it.data)
                }
                is Result.DataError -> {
                    Result.DataError(it.code, it.errorResponse)
                }
            }
        }
    }
}