package com.sonde.mentalfitness.data.local.checkin

import com.sonde.mentalfitness.data.Result
import com.sonde.mentalfitness.data.model.checkin.CheckInConfigResponse
import kotlinx.coroutines.flow.Flow

interface CheckInLocalDataSource {

    suspend fun fetchCheckInConfiguration(checkInName: String): Result<CheckInConfigResponse>

    suspend fun addCheckInSession(
        checkInSessionId: String,
        performedAt: String,
        questionnaireData: String
    ): Flow<Result<Unit>>

    suspend fun updateCheckInSession(checkInSessionId: String, scoreDetail : String) : Flow<Result<Unit>>
}