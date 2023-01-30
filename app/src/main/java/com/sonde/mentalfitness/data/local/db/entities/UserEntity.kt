package com.sonde.mentalfitness.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "first_name") val firstName: String,
    @ColumnInfo(name = "last_name") val lastName: String?,
    @ColumnInfo(name = "sex") val sex: String,
    @ColumnInfo(name = "birth_year") val birthYear: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "pin") val pin: Int

) {
    constructor(firstName: String, lastName: String?, sex: String, birthYear: String,email: String,pin: Int) :
            this(0, firstName, lastName, sex, birthYear,email,pin)
}