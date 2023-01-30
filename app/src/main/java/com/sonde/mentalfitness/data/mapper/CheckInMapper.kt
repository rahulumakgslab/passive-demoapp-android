package com.sonde.mentalfitness.data.mapper

import com.sonde.mentalfitness.data.model.checkin.*
import com.sonde.mentalfitness.data.model.checkin.queanswers.AnswerData
import com.sonde.mentalfitness.data.model.checkin.queanswers.QuestionnaireAnswersData
import com.sonde.mentalfitness.data.model.checkin.queanswers.QuestionnaireAnswersRequest
import com.sonde.mentalfitness.data.model.checkin.session.CheckInCreateSessionRequestData
import com.sonde.mentalfitness.data.model.checkin.session.CreateCheckInSessionResponseData
import com.sonde.mentalfitness.data.model.checkin.session.SondePlatformDetailData
import com.sonde.mentalfitness.domain.model.checkin.*
import com.sonde.mentalfitness.domain.model.checkin.queanswers.AnswerModel
import com.sonde.mentalfitness.domain.model.checkin.queanswers.QuestionnaireAnswersModel
import com.sonde.mentalfitness.domain.model.checkin.session.CheckInCreateSessionRequestModel
import com.sonde.mentalfitness.domain.model.checkin.session.CheckInSessionModel
import com.sonde.mentalfitness.domain.model.checkin.session.SondePlatformDetailModel
import java.util.*

private const val KEY_SAD = "sad"
private const val KEY_STRESSED = "stressed"
private const val KEY_UNSURE = "unsure"
private const val KEY_RELAXED = "relaxed"
private const val KEY_HAPPY = "happy"
private const val KEY_EXCELLENT = "excellent"
private const val KEY_FINE = "good enough"
private const val KEY_SLIGHTLY_DIFFICULT = "some difficulty"
private const val KEY_CHALLENGING = "challenging"

private const val EMOJI_SAD = "\uD83D\uDE2D"
private const val EMOJI_STRESSED = "\uD83D\uDE30"
private const val EMOJI_UNSURE = "\uD83D\uDE10"
private const val EMOJI_RELAXED = "\uD83D\uDE0E"
private const val EMOJI_HAPPY = "\uD83D\uDE03"
private const val EMOJI_EXCELLENT = "\uD83E\uDD29"
private const val EMOJI_FINE = "\uD83D\uDE10"
private const val EMOJI_SLIGHTLY_DIFFICULT = "\uD83E\uDD27"
private const val EMOJI_CHALLENGING = "\uD83D\uDE2D"

class CheckInMapper {

    fun toModel(checkInConfigResponse: CheckInConfigResponse): CheckInConfigModel =
        CheckInConfigModel(
            requestId = checkInConfigResponse.requestId,
            checkInInstanceId = checkInConfigResponse.checkInInstanceId,
            passages = mapPassage(checkInConfigResponse.passages),
            healthChecks = mapHealthChecks(checkInConfigResponse.healthChecks),
            questionnaire = toModel(checkInConfigResponse.questionnaire),
            mainHealthCheckIndex = checkInConfigResponse.mainHealthCheckIndex

        )

    private fun mapOptions(optionsData: List<OptionData>): List<OptionModel> =
        optionsData.map { toModel(it) }

    private fun mapHealthChecks(healthChecksData: List<HealthCheckData>): List<HealthCheckModel> =
        healthChecksData.map { toModel(it) }

    private fun mapQuestions(questionsData: List<QuestionData>): List<QuestionModel> =
        questionsData.map { toModel(it) }

    private fun mapPassage(passageData: List<PassageData>): List<PassageModel> =
        passageData.map { toModel(it) }

    private fun toModel(questionnaireData: QuestionnaireData): QuestionnaireModel =
        QuestionnaireModel(
            id = questionnaireData.id,
            title = questionnaireData.title,
            language = questionnaireData.language,
            questions = mapQuestions(questionnaireData.questions),
            requestId = questionnaireData.requestId
        )

    private fun toModel(questionData: QuestionData): QuestionModel = QuestionModel(
        type = questionData.type,
        text = questionData.text,
        isSkippable = questionData.isSkippable,
        options = mapOptions(questionData.options)
    )

    private fun toModel(optionData: OptionData): OptionModel {

        val textAndEmojiMap = getTextAndEmojiMap()
        val emojiText: String = textAndEmojiMap[optionData.text.toLowerCase()] ?: "?"

        return OptionModel(
            text = optionData.text,
            score = optionData.score,
            emojiText = emojiText,
            isSelected = false

        )
    }

    private fun getTextAndEmojiMap(): HashMap<String, String> {
        val textEmojiMap: HashMap<String, String> = HashMap<String, String>()
        textEmojiMap[KEY_SAD] = EMOJI_SAD
        textEmojiMap[KEY_STRESSED] = EMOJI_STRESSED
        textEmojiMap[KEY_UNSURE] = EMOJI_UNSURE
        textEmojiMap[KEY_RELAXED] = EMOJI_RELAXED
        textEmojiMap[KEY_HAPPY] = EMOJI_HAPPY
        textEmojiMap[KEY_EXCELLENT] = EMOJI_EXCELLENT
        textEmojiMap[KEY_FINE] = EMOJI_FINE
        textEmojiMap[KEY_SLIGHTLY_DIFFICULT] = EMOJI_SLIGHTLY_DIFFICULT
        textEmojiMap[KEY_CHALLENGING] = EMOJI_CHALLENGING

        return textEmojiMap;
    }

    private fun toModel(healthCheckData: HealthCheckData): HealthCheckModel = HealthCheckModel(
        name = healthCheckData.name,
        sondePlatformName = healthCheckData.sondePlatformName
    )

    private fun toModel(passageData: PassageData): PassageModel = PassageModel(
        prompt = passageData.prompt,
        timerLength = passageData.timerLength
    )

    // check-in create session

    fun toModel(checkInSessionResponseData: CreateCheckInSessionResponseData): CheckInSessionModel =
        CheckInSessionModel(
            id = checkInSessionResponseData.id,
            sondePlatformDetail = toModel(checkInSessionResponseData.sondePlatformDetail)
        )

    private fun toModel(sondePlatformDetailData: SondePlatformDetailData): SondePlatformDetailModel =
        SondePlatformDetailModel(
            platformUrl = sondePlatformDetailData.platformUrl,
            accessToken = sondePlatformDetailData.accessToken
        )

    fun toData(requestModel: CheckInCreateSessionRequestModel): CheckInCreateSessionRequestData =
        CheckInCreateSessionRequestData(
            deviceIdSha256 = requestModel.deviceIdSha256,
            timezone = requestModel.timezone
        )

    // submit Questionnaire

    fun toDataRequest(
        questionnaireAnswersModel: QuestionnaireAnswersModel,
        userIdentifier: String
    ): QuestionnaireAnswersRequest =
        QuestionnaireAnswersRequest(
            questionnaireAnswersData = toData(questionnaireAnswersModel, userIdentifier)
        )

    private fun toData(
        questionnaireAnswersModel: QuestionnaireAnswersModel,
        userIdentifier: String
    ): QuestionnaireAnswersData =
        QuestionnaireAnswersData(
            id = questionnaireAnswersModel.id,
            language = questionnaireAnswersModel.language,
            respondedAt = questionnaireAnswersModel.respondedAt,
            userIdentifier = userIdentifier,
            questionResponses = toData(questionnaireAnswersModel.questionResponses)
        )

    private fun toData(questionResponsesModel: List<AnswerModel>): List<AnswerData> =
        questionResponsesModel.map { toData(it) }

    private fun toData(answerModel: AnswerModel): AnswerData {
        return AnswerData().apply {
            optionIndex = answerModel.optionIndex
            optionIndexes = answerModel.optionIndexes
            isSkipped = answerModel.isSkipped
            response = answerModel.textResponse
        }
    }

}