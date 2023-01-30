package com.sonde.mentalfitness.data.local.checkin


import com.sonde.mentalfitness.data.Result
import com.sonde.mentalfitness.data.local.db.dao.CheckInSessionDao
import com.sonde.mentalfitness.data.local.db.entities.CheckInSessionEntity
import com.sonde.mentalfitness.data.local.sharedpref.SharedPreferenceService
import com.sonde.mentalfitness.data.model.checkin.CheckInConfigResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class CheckInLocalDataSourceImpl(
    private val checkInSessionDao: CheckInSessionDao,
    private val sharedPref: SharedPreferenceService,
) : CheckInLocalDataSource {
    override suspend fun fetchCheckInConfiguration(checkInName: String): Result<CheckInConfigResponse> {
        return Result.Success(sharedPref.getCheckInConfigData())
    }

    override suspend fun addCheckInSession(
        checkInSessionId: String,
        performedAt: String,
        questionnaireData: String
    ): Flow<Result<Unit>> {
        val checkInEntity =
            CheckInSessionEntity(checkInSessionId, performedAt, questionnaireData, null)
        return flowOf(Result.Success(checkInSessionDao.insert(checkInEntity)))
    }

    override suspend fun updateCheckInSession(
        checkInSessionId: String,
        scoreDetail: String
    ): Flow<Result<Unit>> {
        return flowOf(Result.Success(checkInSessionDao.update(checkInSessionId, scoreDetail)))
    }
}