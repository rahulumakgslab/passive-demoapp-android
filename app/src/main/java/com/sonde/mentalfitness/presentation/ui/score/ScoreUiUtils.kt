package com.sonde.mentalfitness.presentation.ui.score

import android.text.TextUtils
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.domain.model.checkin.InferenceScoreModel
import com.sonde.mentalfitness.domain.model.checkin.OptionModel
import com.sonde.mentalfitness.domain.model.checkin.score.ScoreFactorAdapterModel
import com.sonde.mentalfitness.domain.model.checkin.score.TitleScoreRepresentationModel
import java.util.*

const val FEELING_TEXT = "Feeling: "
const val NOT_PROVIDED = "Not Provided"

enum class ScoreTitleEnum(val value: Int) {
    PEAK(0), STANDARD(1), REDUCED(2)
}

enum class ScoreTitleRespEnum(val value: Int) {
    LOW(0), HIGH(1)
}

private val scoreTitleArray = arrayOf(
    "Peak",
    "Standard",
    "Reduced"
)

private val scoreRangeRespiratory = arrayOf(
    "Low",
    "High"
)

private val scoreRangeArray = arrayOf(
    "High",
    "Medium",
    "Low"
)

private val scoreSummaryArray = arrayOf(
    "Your voice is most consistent with someone who is positive and isn't feeling down.",
    "Your voice is most consistent with someone who has typical levels of positivity and feeling down.",
    "Your voice is most consistent with someone who may be feeling more down than usual."
)

private val scoreSummaryArrayForRespiratory = arrayOf(
    "Higher risk scores reflect increasing similarity to vocal characteristics observed in people with asthma, COPD, and similar chronic airway conditions.",
    "Higher risk scores reflect increasing similarity to vocal characteristics observed in people with asthma, COPD, and similar chronic airway conditions."
)

private val mainBackgroundArray = arrayOf(
    R.color.score_peak,
    R.color.score_standard,
    R.color.score_reduced,
)

private val scoreValueBackgroundArray = arrayOf(
    R.color.score_peak_dark,
    R.color.score_standard_dark,
    R.color.score_reduced_dark,
)

private val mainBackgroundArrayForRespiratory = arrayOf(
    R.color.score_peak,
    R.color.score_reduced,
)

private val scoreValueBgArrayForRespiratory = arrayOf(
    R.color.score_peak_dark,
    R.color.score_reduced_dark,
)


const val MENTAL_FITNESS = "Mental fitness"
const val CONCENTRATION = "Concentration"
const val ENGAGEMENT = "Engagement"
const val OPTIMISM = "Optimism"

const val SHORTNESS_OF_BREATH = "Shortness of breath"
const val COUGH = "Cough"
const val CHEST_TIGHTNESS = "Chest tightness or pain"


private val healthChecks = arrayOf(
    ENGAGEMENT,
    OPTIMISM,
    CONCENTRATION
)

private val healthChecksOther = arrayOf(
    SHORTNESS_OF_BREATH,
    COUGH,
    CHEST_TIGHTNESS
)

fun buildTitleScoreRepresentationModel(score: Int): TitleScoreRepresentationModel {

    return when (getScoreTitle(score)) {
        ScoreTitleEnum.REDUCED -> {
            getTitleScoreRepresentation(score, ScoreTitleEnum.REDUCED.value)
        }
        ScoreTitleEnum.STANDARD -> {
            getTitleScoreRepresentation(score, ScoreTitleEnum.STANDARD.value)
        }
        ScoreTitleEnum.PEAK -> {
            getTitleScoreRepresentation(score, ScoreTitleEnum.PEAK.value)
        }
    }

}

fun buildTitleScoreForRespiratoryModel(score: Int): TitleScoreRepresentationModel {

    return when (getScoreTileForRespiratory(score)) {
        ScoreTitleRespEnum.LOW -> {
            getTitleScoreForRespiratory(score, ScoreTitleRespEnum.LOW.value)
        }
        ScoreTitleRespEnum.HIGH -> {
            getTitleScoreForRespiratory(score, ScoreTitleRespEnum.HIGH.value)
        }
    }

}

private fun getTitleScoreRepresentation(
    score: Int,
    scoreTypeIndex: Int
): TitleScoreRepresentationModel {
    return TitleScoreRepresentationModel(
        scoreValue = score,
        scoreTitle = scoreTitleArray[scoreTypeIndex],
        scoreSummary = scoreSummaryArray[scoreTypeIndex],
        mainBackgroundResId = mainBackgroundArray[scoreTypeIndex],
        scoreValueBackgroundResId = scoreValueBackgroundArray[scoreTypeIndex],
    )
}


private fun getTitleScoreForRespiratory(
    score: Int,
    scoreTypeIndex: Int
): TitleScoreRepresentationModel {
    return TitleScoreRepresentationModel(
        scoreValue = score,
        scoreTitle = scoreRangeRespiratory[scoreTypeIndex],
        scoreSummary = scoreSummaryArrayForRespiratory[scoreTypeIndex],
        mainBackgroundResId = mainBackgroundArrayForRespiratory[scoreTypeIndex],
        scoreValueBackgroundResId = scoreValueBgArrayForRespiratory[scoreTypeIndex],
    )
}


private fun getScoreTitle(score: Int): ScoreTitleEnum {
    return when (score) {
        in 0..25 -> {
            ScoreTitleEnum.REDUCED
        }
        in 26..35 -> {
            ScoreTitleEnum.STANDARD
        }
        in 36..100 -> {
            ScoreTitleEnum.PEAK
        }
        else ->
            ScoreTitleEnum.STANDARD
    }
}

private fun getScoreTileForRespiratory(score: Int): ScoreTitleRespEnum {
    return when (score) {
        in 0..64 -> {
            ScoreTitleRespEnum.LOW
        }
        in 65..100 -> {
            ScoreTitleRespEnum.HIGH
        }
        else -> {
            ScoreTitleRespEnum.LOW
        }
    }
}

fun buildScoreFactorAdapterModel(inferenceScoreList: List<InferenceScoreModel>): ArrayList<ScoreFactorAdapterModel> {
    val scoreFactorAdapterModelList: ArrayList<ScoreFactorAdapterModel> = ArrayList()

//    healthChecks.forEachIndexed { index, healthCheck ->
//        val specificInferenceModel = getSpecificInferenceModel(healthCheck, inferenceScoreList)
//
//        val scoreFactorAdapterModel = buildScoreFactorAdapterModel(
//            healthCheck,
//            specificInferenceModel?.let { specificInferenceModel.score })
//
//        scoreFactorAdapterModel?.let {
//            scoreFactorAdapterModelList.add(scoreFactorAdapterModel)
//        }
//
//    }
    healthChecksOther.forEachIndexed { index, healthCheck ->
        scoreFactorAdapterModelList.add(
            ScoreFactorAdapterModel(
                R.drawable.ic_factor_concentraion,
                healthCheck,
                "",
                R.color.black,
                R.dimen.score_type_value,
                null
            )
        )
    }

    return scoreFactorAdapterModelList
}

fun getSpecificInferenceModel(
    healthCheck: String,
    inferenceScoreList: List<InferenceScoreModel>
): InferenceScoreModel? {
    inferenceScoreList.forEach {
        if (it.name == healthCheck) {
            return it
        }
    }
    return null
}


fun buildScoreFactorAdapterModel(name: String, score: Int?): ScoreFactorAdapterModel? {

    var scoreTitle: ScoreTitleEnum? = null
    score?.let {
        scoreTitle = getScoreTitle(score)
    }

    val defaultRange = "-"
    val defaultColor = R.color.text_color_black

    return when (name) {
        CONCENTRATION ->
            ScoreFactorAdapterModel(
                R.drawable.ic_factor_concentraion,
                CONCENTRATION,
                scoreTitle?.let { scoreRangeArray[scoreTitle!!.value] } ?: defaultRange,
                scoreTitle?.let { mainBackgroundArray[scoreTitle!!.value] } ?: defaultColor,
                R.dimen.score_type_value,
                null
            )

        ENGAGEMENT ->
            ScoreFactorAdapterModel(
                R.drawable.ic_factor_energy,
                ENGAGEMENT,
                scoreTitle?.let { scoreRangeArray[scoreTitle!!.value] } ?: defaultRange,
                scoreTitle?.let { mainBackgroundArray[scoreTitle!!.value] } ?: defaultColor,
                R.dimen.score_type_value,
                null
            )

        OPTIMISM ->
            ScoreFactorAdapterModel(
                R.drawable.ic_factor_optimism,
                OPTIMISM,
                scoreTitle?.let { scoreRangeArray[scoreTitle!!.value] } ?: defaultRange,
                scoreTitle?.let { mainBackgroundArray[scoreTitle!!.value] } ?: defaultColor,
                R.dimen.score_type_value,
                null
            )

        else -> null

    }
}

fun buildScoreFactorAdapterModel(inferenceScoreModel: InferenceScoreModel): ScoreFactorAdapterModel? {
    val scoreTitle = getScoreTitle(inferenceScoreModel.score)

    return when (inferenceScoreModel.name) {
        CONCENTRATION ->
            ScoreFactorAdapterModel(
                R.drawable.ic_factor_concentraion,
                CONCENTRATION,
                scoreRangeArray[scoreTitle.value],
                mainBackgroundArray[scoreTitle.value],
                R.dimen.score_type_value,
                null
            )

        ENGAGEMENT ->
            ScoreFactorAdapterModel(
                R.drawable.ic_factor_energy,
                ENGAGEMENT,
                scoreRangeArray[scoreTitle.value],
                mainBackgroundArray[scoreTitle.value],
                R.dimen.score_type_value,
                null
            )

        OPTIMISM ->
            ScoreFactorAdapterModel(
                R.drawable.ic_factor_optimism,
                OPTIMISM,
                scoreRangeArray[scoreTitle.value],
                mainBackgroundArray[scoreTitle.value],
                R.dimen.score_type_value,
                null
            )

        else -> null

    }
}

fun buildFeelingFactorAdapterModel(
    selectedFeelingAnswerOption: OptionModel?,
    selectedFeelingReasonList: ArrayList<OptionModel>?
): ScoreFactorAdapterModel {

    val title = if (selectedFeelingAnswerOption == null) {
        FEELING_TEXT + NOT_PROVIDED
    } else {
        FEELING_TEXT + selectedFeelingAnswerOption.text
    }

    val subTitle = StringBuilder()
    selectedFeelingReasonList?.forEachIndexed { index, optionModel ->
        if (index != 0) {
            subTitle.append(", ")
        }
        subTitle.append(optionModel.text)
    }

    return ScoreFactorAdapterModel(
        R.drawable.ic_factor_feeling,
        title,
        selectedFeelingAnswerOption?.emojiText ?: "",
        R.color.black,
        R.dimen.score_type_emoji,
        if (TextUtils.isEmpty(subTitle.toString())) {
            null
        } else {
            subTitle.toString()
        }
    )
}