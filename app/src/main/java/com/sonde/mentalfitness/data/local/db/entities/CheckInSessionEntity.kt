package com.sonde.mentalfitness.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checkin_sessions")
data class CheckInSessionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "perform_at") val performAt: String,
    @ColumnInfo(name = "questionnaire_detail") val questionnaireDetail: String?,
    @ColumnInfo(name = "score_detail") val scoreDetail: String?
)