package com.sonde.mentalfitness.data.local.user

import com.sonde.mentalfitness.data.Result
import com.sonde.mentalfitness.domain.model.User

interface UserLocalDataSource {
    suspend fun insertUser(user: User): Result<Unit>
    suspend fun insetUserPin(pin: String): Result<Unit>
    suspend fun getUserPin(): Result<String>
    suspend fun getUser(): Result<User>
    suspend fun getNoPinOption(): Result<Boolean>
    suspend fun setNoPinOption(): Result<Unit>
    fun isQuestionnaireSkipped(): Boolean
    fun setQuestionnaireSkipped(isSkipped: Boolean)
}