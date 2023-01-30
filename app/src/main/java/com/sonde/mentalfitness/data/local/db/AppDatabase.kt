package com.sonde.mentalfitness.data.local.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sonde.mentalfitness.MentalFitnessApplication
import com.sonde.mentalfitness.data.local.db.dao.CheckInSessionDao
import com.sonde.mentalfitness.data.local.db.dao.UserDao
import com.sonde.mentalfitness.data.local.db.entities.CheckInSessionEntity
import com.sonde.mentalfitness.data.local.db.entities.UserEntity
import com.sonde.mentalfitness.presentation.utils.security.EncryptionUtil
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [UserEntity::class, CheckInSessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun checkInDetailsDao(): CheckInSessionDao

    companion object {
        private val factory = SupportFactory(SQLiteDatabase.getBytes(EncryptionUtil.getSecretKey().toString().toCharArray()))
        val db = Room.databaseBuilder(
            MentalFitnessApplication.applicationContext(),
            AppDatabase::class.java, "sonde-mf-db"
        )/*.openHelperFactory(factory)*/
            .build()
    }
}