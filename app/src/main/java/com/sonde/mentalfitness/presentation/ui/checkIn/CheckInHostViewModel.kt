package com.sonde.mentalfitness.presentation.ui.checkIn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonde.mentalfitness.MentalFitnessApplication
import com.sonde.mentalfitness.data.Result
import com.sonde.mentalfitness.data.local.checkin.CheckInLocalDataSourceImpl
import com.sonde.mentalfitness.data.local.db.AppDatabase
import com.sonde.mentalfitness.data.local.sharedpref.SharedPreferenceServiceImpl
import com.sonde.mentalfitness.data.local.user.UserLocalDataSourceImpl
import com.sonde.mentalfitness.data.remote.checkin.CheckInRemoteDataSourceImpl
import com.sonde.mentalfitness.data.remote.network.RetrofitBuilder
import com.sonde.mentalfitness.data.remote.user.UserRemoteDataSourceImpl
import com.sonde.mentalfitness.data.repository.CheckInRepositoryImpl
import com.sonde.mentalfitness.data.repository.UserRepositoryImpl
import com.sonde.mentalfitness.domain.CheckInSessionManager
import com.sonde.mentalfitness.domain.model.checkin.CheckInConfigModel
import com.sonde.mentalfitness.domain.model.checkin.PassageModel
import com.sonde.mentalfitness.presentation.utils.livedata.SingleLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CheckInHostViewModel : ViewModel() {

    val event = SingleLiveData<CheckInHostViewEvent>()

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

    private val userRepository = UserRepositoryImpl(
        UserRemoteDataSourceImpl(RetrofitBuilder.apiService),
        UserLocalDataSourceImpl(
            AppDatabase.db.userDao(),
            SharedPreferenceServiceImpl(MentalFitnessApplication.applicationContext())
        )
    )

    init {
        viewModelScope.launch {
            checkInRepository.getCheckInConfiguration("Mental Fitness:v1")
                .collect { it ->
                    when (it) {
                        is Result.Success -> {
                            it.data.let {
                                if (userRepository.isQuestionnaireSkipped()) {
                                    CheckInSessionManager.selectedFeelingAnswerOption = null
                                    val questionnaireAnswer = buildQuestionnaireAnswer(
                                        checkInConfigModel = it,
                                        feelingOptionAnswer = null,
                                        feelingReasonAnswerList = null
                                    )
                                    event.value =
                                        CheckInHostViewEvent.ReadyToRecordEvent(
                                            questionnaireAnswer,
                                            getRandomPassage(it)
                                        )
                                } else {
                                    event.value =
                                        CheckInHostViewEvent.FeelingScreenEvent
                                }
                            }
                        }
                    }
                }
        }
    }


    private fun getRandomPassage(checkInConfigModel: CheckInConfigModel): PassageModel {
        return checkInConfigModel.passages[0]
    }
}