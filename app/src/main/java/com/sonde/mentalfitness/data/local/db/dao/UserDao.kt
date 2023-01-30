package com.sonde.mentalfitness.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.sonde.mentalfitness.data.local.db.entities.UserEntity

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: UserEntity)

    @Delete
    suspend fun delete(user: UserEntity)

    @Query("SELECT * FROM users ORDER BY id DESC LIMIT 1")
    suspend fun getUser(): UserEntity
}