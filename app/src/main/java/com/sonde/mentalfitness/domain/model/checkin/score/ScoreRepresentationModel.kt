package com.sonde.mentalfitness.domain.model.checkin.score

data class ScoreRepresentationModel(
    val date: String,
    val titleScoreRepresentation: TitleScoreRepresentationModel,
    val scoreFactorAdapterList: List<ScoreFactorAdapterModel>
)
