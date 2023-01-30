package com.sonde.mentalfitness.domain.model.checkin

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class HealthCheckModel(
    val name: String,
    val sondePlatformName: String
) : Parcelable