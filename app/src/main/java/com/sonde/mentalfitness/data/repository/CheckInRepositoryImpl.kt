package com.sonde.mentalfitness.data.repository

import com.google.gson.Gson
import com.sonde.mentalfitness.data.Result
import com.sonde.mentalfitness.data.local.checkin.CheckInLocalDataSource
import com.sonde.mentalfitness.data.mapper.CheckInMapper
import com.sonde.mentalfitness.data.remote.checkin.CheckInRemoteDataSource
import com.sonde.mentalfitness.domain.model.checkin.CheckInConfigModel
import com.sonde.mentalfitness.domain.model.checkin.queanswers.QuestionnaireAnswersModel
import com.sonde.mentalfitness.domain.model.checkin.session.CheckInCreateSessionRequestModel
import com.sonde.mentalfitness.domain.model.checkin.session.CheckInSessionModel
import com.sonde.mentalfitness.domain.repository.CheckInRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CheckInRepositoryImpl(
    private val checkInRemoteDataSource: CheckInRemoteDataSource,
    private val checkInLocalDataSource: CheckInLocalDataSource
) : CheckInRepository {

    override suspend fun getCheckInConfiguration(checkInName: String): Flow<Result<CheckInConfigModel>> {
        return flow {
            checkInLocalDataSource.fetchCheckInConfiguration(checkInName).let {
                when (it) {
                    is Result.Success -> {
                        emit(Result.Success(CheckInMapper().toModel(it.data)))
                    }
                    is Result.DataError -> {
                        emit(Result.DataError(it.code, it.errorResponse))
                    }
                }
            }
        }
    }

    override suspend fun createCheckInSession(
        checkInInstanceId: String,
        checkInCreateSessionRequestModel: CheckInCreateSessionRequestModel
    ): Flow<Result<CheckInSessionModel>> {
        return flow {
            val checkInCreateSessionRequestData =
                CheckInMapper().toData(checkInCreateSessionRequestModel)
            checkInRemoteDataSource.createCheckInSession(
                checkInInstanceId,
                checkInCreateSessionRequestData
            ).let {
                when (it) {
                    is Result.Success -> {
                        emit(Result.Success(CheckInMapper().toModel(it.data)))
                    }
                    is Result.DataError -> {
                        emit(Result.DataError(it.code, it.errorResponse))
                    }
                }
            }
        }
    }


    override suspend fun submitQuestionnaireAnswer(
        sessionId: String,
        questionnaireAnswersModel: QuestionnaireAnswersModel
    ): Flow<Result<Unit>> {

//            val userIdentifier = "5ae87cda1"
//            val questionnaireAnswersRequest =
//                CheckInMapper().toDataRequest(questionnaireAnswersModel, userIdentifier)
//
//            checkInRemoteDataSource.submitQuestionnaireAnswer(
//                sessionId,
//                questionnaireAnswersRequest
//            )
        val questionnaireDetail = Gson().toJson(questionnaireAnswersModel)
        return checkInLocalDataSource.addCheckInSession(
            sessionId,
            questionnaireAnswersModel.respondedAt,
            questionnaireDetail
        )

    }

    override suspend fun updateCheckInSession(
        checkInSessionId: String,
        scoreDetail: String
    ): Flow<Result<Unit>> {
        return checkInLocalDataSource.updateCheckInSession(checkInSessionId, scoreDetail)
    }
}