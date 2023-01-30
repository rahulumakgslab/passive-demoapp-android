package com.sonde.mentalfitness.presentation.ui.record.readytorecord

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonde.mentalfitness.MentalFitnessApplication
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.data.Result
import com.sonde.mentalfitness.data.local.checkin.CheckInLocalDataSourceImpl
import com.sonde.mentalfitness.data.local.db.AppDatabase
import com.sonde.mentalfitness.data.local.sharedpref.SharedPreferenceServiceImpl
import com.sonde.mentalfitness.data.remote.checkin.CheckInRemoteDataSourceImpl
import com.sonde.mentalfitness.data.remote.network.RetrofitBuilder
import com.sonde.mentalfitness.data.repository.CheckInRepositoryImpl
import com.sonde.mentalfitness.domain.CheckInSessionManager
import com.sonde.mentalfitness.domain.model.checkin.PassageModel
import com.sonde.mentalfitness.domain.model.checkin.queanswers.QuestionnaireAnswersModel
import com.sonde.mentalfitness.presentation.utils.livedata.SingleLiveData
import com.sonde.mentalfitness.presentation.utils.util.getCurrentDateWithMonthAndTh
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class ReadyToRecordViewModel() : ViewModel() {

    var isQuestionnaireAlreadySubmitted = false

    val event = SingleLiveData<ReadyToRecordViewEvent>()

    private val _state = MutableLiveData<ReadyToRecordViewState>()
    val state: LiveData<ReadyToRecordViewState>
        get() = _state

    private val _passageModel = MutableLiveData<PassageModel>()
    val passageModel: LiveData<PassageModel> get() = _passageModel

    private val _todayDate = MutableLiveData<String>()
    val todayDate: LiveData<String> get() = _todayDate

    private var mQuestionnaireAnswer: QuestionnaireAnswersModel? = null

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

    fun setAnswersData(questionnaireAnswer: QuestionnaireAnswersModel?) {
        this.mQuestionnaireAnswer = questionnaireAnswer
    }

    fun setPassage(passageModel: PassageModel) {
        _passageModel.value = passageModel
    }

    fun onBackButtonClicked() {
        event.postValue(ReadyToRecordViewEvent.HandleBackPress)
    }

    fun submitQuestionnaireAnswer() {

        if (isQuestionnaireAlreadySubmitted) {
            handleQuestionnaireAlreadySubmitted()
        } else {
            _state.postValue(ReadyToRecordViewState.Loading)

            viewModelScope.launch {
                CheckInSessionManager.checkInSessionId = UUID.randomUUID().toString()
                val checkInSessionId = CheckInSessionManager.checkInSessionId
                val questionnaireAnswer = mQuestionnaireAnswer

                if (checkInSessionId != null && questionnaireAnswer != null) {
                    checkInRepository.submitQuestionnaireAnswer(
                        checkInSessionId,
                        questionnaireAnswer
                    ).collect {
                        when (it) {
                            is Result.Success -> {
                                Log.d("!!", "Questionnaire submitted successfully: ")
                                isQuestionnaireAlreadySubmitted = true
                                _state.postValue(ReadyToRecordViewState.LoadingComplete)
                                event.postValue(ReadyToRecordViewEvent.OnQuestionnaireAnswerSubmitted)
                            }
                            is Result.DataError -> {
                                _state.postValue(ReadyToRecordViewState.LoadingComplete)
                                _state.postValue(ReadyToRecordViewState.ShowError(R.string.something_went_wrong))
                            }
                        }

                    }
                }
            }
        }
    }

    /**
     *  This will be the case, when user press back from countdown or recording screen.
     *  In that case we have lunch countdown screen without submitting questionnaire again
     */
    private fun handleQuestionnaireAlreadySubmitted() {
        event.postValue(ReadyToRecordViewEvent.OnQuestionnaireAnswerSubmitted)
    }

    fun onBeginRecordingClicked() {
        event.postValue(ReadyToRecordViewEvent.OnBeginRecordingClicked)
    }
}