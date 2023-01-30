package com.sonde.mentalfitness.data.repository

import com.sonde.mentalfitness.data.Result
import com.sonde.mentalfitness.data.local.user.UserLocalDataSource
import com.sonde.mentalfitness.data.remote.user.UserRemoteDataSource
import com.sonde.mentalfitness.domain.model.User
import com.sonde.mentalfitness.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UserRepositoryImpl(
    private val userRemoteDataSource: UserRemoteDataSource,
    private val userLocalDataSource: UserLocalDataSource
) : UserRepository {
    override suspend fun signupUser(user: User): Flow<Result<Unit>> {
        return flowOf(userLocalDataSource.insertUser(user))
    }

    override suspend fun setAccessPin(pin: String): Flow<Result<Unit>> {
        return flowOf(userLocalDataSource.insetUserPin(pin))
    }

    override suspend fun getUserPin(): Flow<Result<String>> {
        return flowOf(userLocalDataSource.getUserPin())
    }

    override suspend fun getUser(): Flow<Result<User>> {
        return flowOf(userLocalDataSource.getUser())
    }

    override suspend fun getNoPinOption(): Flow<Result<Boolean>> {
        return flowOf(userLocalDataSource.getNoPinOption())
    }

    override fun isQuestionnaireSkipped(): Boolean {
        return userLocalDataSource.isQuestionnaireSkipped()
    }

    override fun setQuestionnaireSkipped(isSkipped: Boolean) {
        userLocalDataSource.setQuestionnaireSkipped(isSkipped)
    }

    override suspend fun setNoPinOption(): Flow<Result<Unit>> {
        return flowOf(userLocalDataSource.setNoPinOption())
    }


}