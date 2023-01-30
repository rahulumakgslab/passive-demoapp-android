package com.sonde.mentalfitness.presentation.ui.checkIn.feeling

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonde.mentalfitness.MentalFitnessApplication
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.data.ERROR_NO_INTERNET_CONNECTION
import com.sonde.mentalfitness.data.Result
import com.sonde.mentalfitness.data.local.checkin.CheckInLocalDataSourceImpl
import com.sonde.mentalfitness.data.local.db.AppDatabase
import com.sonde.mentalfitness.data.local.sharedpref.SharedPreferenceServiceImpl
import com.sonde.mentalfitness.data.remote.checkin.CheckInRemoteDataSourceImpl
import com.sonde.mentalfitness.data.remote.network.RetrofitBuilder
import com.sonde.mentalfitness.data.repository.CheckInRepositoryImpl
import com.sonde.mentalfitness.domain.CheckInSessionManager
import com.sonde.mentalfitness.domain.model.checkin.CheckInConfigModel
import com.sonde.mentalfitness.domain.model.checkin.OptionModel
import com.sonde.mentalfitness.domain.model.checkin.PassageModel
import com.sonde.mentalfitness.presentation.ui.checkIn.buildQuestionnaireAnswer
import com.sonde.mentalfitness.presentation.utils.livedata.SingleLiveData
import com.sonde.mentalfitness.presentation.utils.util.getCurrentDateWithMonthAndTh
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

const val QUESTION_INDEX_HOW_ARE_U_FEELING = 0

class FeelingViewModel : ViewModel() {

    val event = SingleLiveData<FeelingViewEvent>()

    private val _state = MutableLiveData<FeelingViewState>()
    val state: LiveData<FeelingViewState>
        get() = _state

    private val _checkInConfigModel = MutableLiveData<CheckInConfigModel>()
    val checkInConfigModel: LiveData<CheckInConfigModel> get() = _checkInConfigModel

    val _question = MutableLiveData<String>()
    val question: LiveData<String> get() = _question

    private val _todayDate = MutableLiveData<String>()
    val todayDate: LiveData<String> get() = _todayDate

    private val checkInRepository = CheckInRepositoryImpl(
        CheckInRemoteDataSourceImpl(
            RetrofitBuilder.apiService,
            Dispatchers.IO
        ),
        CheckInLocalDataSourceImpl(
            AppDatabase.db.checkInDetailsDao(),
            SharedPreferenceServiceImpl(MentalFitnessApplication.applicationContext())
        )
    )

    init {
        _todayDate.value = getCurrentDateWithMonthAndTh()

    }

    fun getConfigData() {
        _state.postValue(FeelingViewState.Loading)
        viewModelScope.launch {
            checkInRepository.getCheckInConfiguration("Mental Fitness:v1").collect { it ->
                when (it) {
                    is Result.Success -> {
                        it.data.let {
                            setCheckInConfigValues(checkInConfigModel = it)
                        }
                    }
                    is Result.DataError -> {
                        when (it.code) {
                            ERROR_NO_INTERNET_CONNECTION ->
                                _state.postValue(FeelingViewState.ShowNoInternet)
                            else ->
                                _state.postValue(FeelingViewState.ShowError(R.string.something_went_wrong))
                        }
                    }
                }

            }
        }
    }


    private fun setCheckInConfigValues(checkInConfigModel: CheckInConfigModel) {
        _checkInConfigModel.value = checkInConfigModel

        if (checkInConfigModel.questionnaire.questions.size > 1) {
            _question.value =
                checkInConfigModel.questionnaire.questions[QUESTION_INDEX_HOW_ARE_U_FEELING].text
        }
    }


    fun selectFeelingOption(option: OptionModel) {
        CheckInSessionManager.selectedFeelingAnswerOption = option
        event.postValue(checkInConfigModel.value?.let {
            FeelingViewEvent.SelectFeelingOption(
                option,
                it
            )
        })
    }

    fun onContinueToDashboardClicked() {
        event.postValue(FeelingViewEvent.NavigateToDashboard)
    }

    fun onSkipButtonClicked() {

        if (checkInConfigModel.value != null) {
            CheckInSessionManager.selectedFeelingAnswerOption = null
            val questionnaireAnswer = buildQuestionnaireAnswer(
                checkInConfigModel = checkInConfigModel.value!!,
                feelingOptionAnswer = null,
                feelingReasonAnswerList = null
            )

            event.postValue(
                FeelingViewEvent.NavigateToReadyToRecord(
                    questionnaireAnswer,
                    getRandomPassage(checkInConfigModel.value!!)
                )
            )

        } else {
            _state.postValue(FeelingViewState.ShowError(R.string.failed_to_load_check_config))
        }
    }

    fun onBackButtonClicked() {
        event.postValue(FeelingViewEvent.HandleBackPress)
    }

    private fun getRandomPassage(checkInConfigModel: CheckInConfigModel): PassageModel {
        return checkInConfigModel.passages[0]
    }
}