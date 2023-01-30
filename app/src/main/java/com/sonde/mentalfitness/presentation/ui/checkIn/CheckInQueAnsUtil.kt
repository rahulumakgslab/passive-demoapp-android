package com.sonde.mentalfitness.presentation.ui.checkIn

import com.sonde.mentalfitness.domain.model.checkin.CheckInConfigModel
import com.sonde.mentalfitness.domain.model.checkin.OptionModel
import com.sonde.mentalfitness.domain.model.checkin.queanswers.AnswerModel
import com.sonde.mentalfitness.domain.model.checkin.queanswers.QuestionnaireAnswersModel
import com.sonde.mentalfitness.presentation.utils.util.SERVER_DATE_PATTERN
import com.sonde.mentalfitness.presentation.utils.util.TYPE_UTC
import com.sonde.mentalfitness.presentation.utils.util.getCurrentDateStr
import java.util.*

fun buildQuestionnaireAnswer(
    checkInConfigModel: CheckInConfigModel,
    feelingOptionAnswer: OptionModel?,
    feelingReasonAnswerList: List<OptionModel>?
): QuestionnaireAnswersModel {

    return QuestionnaireAnswersModel(
        id = checkInConfigModel.questionnaire.id,
        language = checkInConfigModel.questionnaire.language,
        respondedAt = getCurrentDateStr(SERVER_DATE_PATTERN, TYPE_UTC),
        questionResponses = buildAnswers(
            checkInConfigModel,
            feelingOptionAnswer,
            feelingReasonAnswerList
        )

    )
}


private fun buildAnswers(
    checkInConfigModel: CheckInConfigModel,
    feelingOptionAnswer: OptionModel?,
    feelingReasonAnswerList: List<OptionModel>?
): List<AnswerModel> {
    val answers: ArrayList<AnswerModel> = ArrayList()

    // Que 1 : Feeling
    if (feelingOptionAnswer != null) {
        val optionIndex = getOptionIndex(
            feelingOptionAnswer.text,
            checkInConfigModel.questionnaire.questions[0].options
        )

        if (optionIndex == null) {
            answers.add(buildSkipQuesOption())
        } else {
            val answerModel = AnswerModel()
            answerModel.optionIndex = optionIndex
            answers.add(answerModel)
        }
    } else {
        // add skip answer block
        answers.add(buildSkipQuesOption())
    }

    // Que 2 : Feeling reason
    if (feelingReasonAnswerList != null) {
        val multipleSelectOptionsIndex = multipleSelectOptionsIndex(feelingReasonAnswerList)

        if (multipleSelectOptionsIndex.isNotEmpty()) {
            val answerModel = AnswerModel()
            answerModel.optionIndexes = multipleSelectOptionsIndex
            answers.add(answerModel)
        } else {
            answers.add(buildSkipQuesOption())
        }
    } else {
        answers.add(buildSkipQuesOption())
    }

    return answers
}

private fun buildSkipQuesOption(): AnswerModel {
    val answerModel = AnswerModel()
    answerModel.isSkipped = true
    return answerModel
}

private fun multipleSelectOptionsIndex(feelingReasonOptions: List<OptionModel>?): ArrayList<Int> {
    val optionIndexes = ArrayList<Int>()

    feelingReasonOptions?.forEachIndexed { index, optionModel ->
        if (optionModel.isSelected) {
            optionIndexes.add(index)
        }
    }
    return optionIndexes
}

private fun getOptionIndex(selectedOption: String?, options: List<OptionModel>?): Int? {

    options?.forEachIndexed { index, optionModel ->
        if (optionModel.text == selectedOption) {
            return index
        }
    }
    return null
}