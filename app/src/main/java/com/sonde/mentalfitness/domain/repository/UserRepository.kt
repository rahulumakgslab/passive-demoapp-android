package com.sonde.mentalfitness.domain.repository

import com.sonde.mentalfitness.data.Result
import com.sonde.mentalfitness.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun signupUser(user: User): Flow<Result<Unit>>
    suspend fun setAccessPin(pin: String): Flow<Result<Unit>>
    suspend fun getUserPin(): Flow<Result<String>>
    suspend fun getUser(): Flow<Result<User>>
    suspend fun setNoPinOption(): Flow<Result<Unit>>
    suspend fun getNoPinOption(): Flow<Result<Boolean>>
    fun isQuestionnaireSkipped(): Boolean
    fun setQuestionnaireSkipped(isSkipped : Boolean)
}