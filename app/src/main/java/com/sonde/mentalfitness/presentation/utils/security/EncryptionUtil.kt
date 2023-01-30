package com.sonde.mentalfitness.presentation.utils.security

import com.sonde.mentalfitness.BuildConfig
import com.sonde.mentalfitness.MentalFitnessApplication
import java.security.Key

object EncryptionUtil {
    fun getSecretKey(): Key {
        val secretKey = SondeSafeStore(
            MentalFitnessApplication.applicationContext(),
            BuildConfig.ENCRYPTION_ALIAS_KEY
        ).secretKey
        return secretKey
    }
}