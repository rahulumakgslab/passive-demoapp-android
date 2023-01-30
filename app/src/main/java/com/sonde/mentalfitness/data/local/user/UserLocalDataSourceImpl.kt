package com.sonde.mentalfitness.data.local.user

import com.sonde.mentalfitness.data.Result
import com.sonde.mentalfitness.data.local.db.dao.UserDao
import com.sonde.mentalfitness.data.local.db.entities.UserEntity
import com.sonde.mentalfitness.data.local.sharedpref.SharedPreferenceService
import com.sonde.mentalfitness.domain.model.User

class UserLocalDataSourceImpl(
    private val userDao: UserDao,
    private val sharedPrefService: SharedPreferenceService
) : UserLocalDataSource {
    override suspend fun insertUser(user: User): Result<Unit> {
        val userEntity = UserEntity(user.firstName, user.lastName, user.sex, user.birthYear,user.email,user.pin)
        return Result.Success(userDao.insert(userEntity))
    }

    override suspend fun insetUserPin(pin: String): Result<Unit> {
        return Result.Success(sharedPrefService.setUserPin(pin))
    }

    override suspend fun getUserPin(): Result<String> {
        return Result.Success(sharedPrefService.getUserPin())
    }

    override suspend fun getUser(): Result<User> {
        val userEntity = userDao.getUser()
        return Result.Success(
            User(
                userEntity.firstName,
                userEntity.lastName,
                userEntity.sex,
                userEntity.birthYear,
                userEntity.email,
                userEntity.pin
            )
        )
    }

    override suspend fun getNoPinOption(): Result<Boolean> {
        return Result.Success(sharedPrefService.getNoPinOption())
    }

    override suspend fun setNoPinOption(): Result<Unit> {
        return Result.Success(sharedPrefService.setNoPinOption())
    }

    override fun isQuestionnaireSkipped(): Boolean {
        return sharedPrefService.isQuestionnaireSkipped()
    }

    override fun setQuestionnaireSkipped(isSkipped: Boolean) {
        sharedPrefService.setQuestionnaireSkipped(isSkipped)
    }
}