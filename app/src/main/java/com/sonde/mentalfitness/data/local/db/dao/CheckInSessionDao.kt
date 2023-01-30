package com.sonde.mentalfitness.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.sonde.mentalfitness.data.local.db.entities.CheckInSessionEntity

@Dao
interface CheckInSessionDao {
    @Insert
    suspend fun insert(checkInDetail: CheckInSessionEntity)

    @Delete
    suspend fun delete(checkInDetail: CheckInSessionEntity)

    @Query("update checkin_sessions set score_detail = :scoreDetail where id =:id")
    suspend fun update(id: String, scoreDetail: String)
}