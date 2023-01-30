package com.sonde.mentalfitness.domain.repository

import com.sonde.mentalfitness.data.Result
import com.sonde.mentalfitness.domain.model.checkin.CheckInConfigModel
import com.sonde.mentalfitness.domain.model.checkin.queanswers.QuestionnaireAnswersModel
import com.sonde.mentalfitness.domain.model.checkin.session.CheckInCreateSessionRequestModel
import com.sonde.mentalfitness.domain.model.checkin.session.CheckInSessionModel
import kotlinx.coroutines.flow.Flow

interface CheckInRepository {

    suspend fun getCheckInConfiguration(checkInName: String): Flow<Result<CheckInConfigModel>>

    suspend fun createCheckInSession(
        checkInInstanceId: String,
        checkInCreateSessionRequestModel: CheckInCreateSessionRequestModel
    ): Flow<Result<CheckInSessionModel>>


    suspend fun submitQuestionnaireAnswer(
        sessionId: String,
        questionnaireAnswersModel: QuestionnaireAnswersModel
    )
            : Flow<Result<Unit>>

    suspend fun updateCheckInSession(checkInSessionId: String, scoreDetail : String) : Flow<Result<Unit>>
}
