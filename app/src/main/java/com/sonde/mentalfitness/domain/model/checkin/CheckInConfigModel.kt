package com.sonde.mentalfitness.domain.model.checkin

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class CheckInConfigModel(
    val requestId: String,
    val checkInInstanceId: String,
    val passages: List<PassageModel>,
    val healthChecks: List<HealthCheckModel>,
    val questionnaire: QuestionnaireModel,
    val mainHealthCheckIndex: Int
) : Parcelable