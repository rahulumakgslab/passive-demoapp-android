package com.sonde.mentalfitness.presentation.ui.checkIn.feelingreason

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.domain.CheckInSessionManager
import com.sonde.mentalfitness.domain.model.checkin.CheckInConfigModel
import com.sonde.mentalfitness.domain.model.checkin.OptionModel
import com.sonde.mentalfitness.domain.model.checkin.PassageModel
import com.sonde.mentalfitness.presentation.ui.checkIn.buildQuestionnaireAnswer
import com.sonde.mentalfitness.presentation.utils.livedata.SingleLiveData
import com.sonde.mentalfitness.presentation.utils.util.getCurrentDateWithMonthAndTh
import java.util.*
import kotlin.random.Random

class FeelingReasonsViewModel : ViewModel() {

    val event = SingleLiveData<FeelingReasonViewEvent>()

    private val _state = MutableLiveData<FeelingReasonsViewState>()
    val state: LiveData<FeelingReasonsViewState>
        get() = _state

    private val _feelingReasonOptions = MutableLiveData<List<OptionModel>>()
    val feelingReasonOptions: LiveData<List<OptionModel>> get() = _feelingReasonOptions

    private val _selectedFeelingOption = MutableLiveData<OptionModel>()
    val selectedFeelingOption: LiveData<OptionModel> get() = _selectedFeelingOption

    private val _isAnyReasonSelected = MutableLiveData<Boolean>()
    val isAnyReasonSelected: LiveData<Boolean> get() = _isAnyReasonSelected

    private val _todayDate = MutableLiveData<String>()
    val todayDate: LiveData<String> get() = _todayDate

    private val _question = MutableLiveData<String>()
    val question: LiveData<String> get() = _question

    private val _secondaryQuestion = MutableLiveData<String>()
    val secondaryQuestion: LiveData<String> get() = _secondaryQuestion

    private var mCheckInConfigModel: CheckInConfigModel? = null

    fun setData(selectedFeelingOption: OptionModel?, checkInConfigModel: CheckInConfigModel?) {
        checkInConfigModel.let {
            this.mCheckInConfigModel = it
            _feelingReasonOptions.value = it?.questionnaire?.questions?.get(1)?.options
            _question.value = checkInConfigModel?.questionnaire?.questions?.get(0)?.text
            _secondaryQuestion.value = checkInConfigModel?.questionnaire?.questions?.get(1)?.text
        }

        selectedFeelingOption?.let {
            _selectedFeelingOption.value = it
        }

        _todayDate.value = getCurrentDateWithMonthAndTh()
    }

    fun onSkipButtonClicked() {
        if (mCheckInConfigModel != null) {
            CheckInSessionManager.selectedFeelingReasonList = null

            val questionnaireAnswer = buildQuestionnaireAnswer(
                checkInConfigModel = mCheckInConfigModel!!,
                feelingOptionAnswer = selectedFeelingOption.value,
                feelingReasonAnswerList = null
            )
            event.postValue(
                FeelingReasonViewEvent.NavigateToReadyToRecord(
                    questionnaireAnswer,
                    getRandomPassage(mCheckInConfigModel!!)
                )
            )

        } else {
            _state.postValue(FeelingReasonsViewState.ShowError(R.string.failed_to_load_check_config))
        }
    }

    private fun getRandomPassage(checkInConfigModel: CheckInConfigModel): PassageModel {
        return checkInConfigModel.passages[Random.nextInt(0, (checkInConfigModel.passages.size))]
    }

    fun onNextStepButtonClicked() {

        storeFeelingReasonInCheckInSession(feelingReasonOptions.value)

        if (mCheckInConfigModel != null) {
            val questionnaireAnswer = buildQuestionnaireAnswer(
                checkInConfigModel = mCheckInConfigModel!!,
                feelingOptionAnswer = selectedFeelingOption.value,
                feelingReasonAnswerList = feelingReasonOptions.value
            )

            event.postValue(
                FeelingReasonViewEvent.NavigateToReadyToRecord(
                    questionnaireAnswer,
                    getRandomPassage(mCheckInConfigModel!!)
                )
            )

        } else {
            _state.postValue(FeelingReasonsViewState.ShowError(R.string.failed_to_load_check_config))
        }
    }

    private fun storeFeelingReasonInCheckInSession(feelingReasonOptions: List<OptionModel>?) {
        val selectedOnlyOptions = getFilteredSelectedReasonOptionsList(feelingReasonOptions)

        if (selectedOnlyOptions.isNotEmpty()) {
            CheckInSessionManager.selectedFeelingReasonList = selectedOnlyOptions
        }
    }

    private fun getFilteredSelectedReasonOptionsList(feelingReasonOptions: List<OptionModel>?): ArrayList<OptionModel> {
        val selectedOnlyOptions = ArrayList<OptionModel>()
        feelingReasonOptions?.let {
            feelingReasonOptions.forEach {
                if (it.isSelected) {
                    selectedOnlyOptions.add(it)
                }
            }
        }
        return selectedOnlyOptions
    }

    fun onBackButtonClicked() {
        event.postValue(FeelingReasonViewEvent.HandleBackPress)
    }

    fun updateReasonOptionList(mOptionsList: List<OptionModel>) {
        _feelingReasonOptions.value = mOptionsList

        handleButtonEnable(mOptionsList)
    }

    private fun handleButtonEnable(mOptionsList: List<OptionModel>) {
        val selectedOnlyOptions = getFilteredSelectedReasonOptionsList(mOptionsList)
        _isAnyReasonSelected.value = selectedOnlyOptions.isNotEmpty()
    }
}