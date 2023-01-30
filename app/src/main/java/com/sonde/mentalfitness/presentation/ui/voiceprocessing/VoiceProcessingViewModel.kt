package com.sonde.mentalfitness.presentation.ui.voiceprocessing

import android.os.CountDownTimer
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
import com.sonde.mentalfitness.data.local.user.UserLocalDataSourceImpl
import com.sonde.mentalfitness.data.remote.checkin.CheckInRemoteDataSourceImpl
import com.sonde.mentalfitness.data.remote.network.RetrofitBuilder
import com.sonde.mentalfitness.data.remote.user.UserRemoteDataSourceImpl
import com.sonde.mentalfitness.data.repository.CheckInRepositoryImpl
import com.sonde.mentalfitness.data.repository.UserRepositoryImpl
import com.sonde.mentalfitness.domain.CheckInSessionManager
import com.sonde.mentalfitness.domain.model.checkin.InferenceScoreModel
import com.sonde.mentalfitness.domain.model.checkin.PassageModel
import com.sonde.mentalfitness.domain.model.checkin.SubmitSessionModel
import com.sonde.mentalfitness.presentation.utils.util.ONE_SECOND_IN_MILLIS
import com.sondeservices.common.HealthCheckType
import com.sondeservices.edge.elck.ElckFailedException
import com.sondeservices.edge.inference.InferenceCallback
import com.sondeservices.edge.inference.InferenceEngine
import com.sondeservices.edge.ml.model.Gender
import com.sondeservices.edge.ml.model.MetaData
import com.sondeservices.edge.ml.model.Score
import com.sondeservices.edge.ml.model.VFFinalScore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File

class VoiceProcessingViewModel : ViewModel() {

    private lateinit var timer: CountDownTimer
    private val _state = MutableLiveData<VoiceProcessingViewState>()
    val state: LiveData<VoiceProcessingViewState>
        get() = _state

    private val _event = MutableLiveData<VoiceProcessingViewEvent>()
    val event: LiveData<VoiceProcessingViewEvent>
        get() = _event

    private val _voiceProcessingStatus = MutableLiveData<Int>()
    val voiceProcessingStatus: LiveData<Int> get() = _voiceProcessingStatus
    private lateinit var mPassageModel: PassageModel
    private val userRepository = UserRepositoryImpl(
        UserRemoteDataSourceImpl(RetrofitBuilder.apiService),
        UserLocalDataSourceImpl(
            AppDatabase.db.userDao(),
            SharedPreferenceServiceImpl(MentalFitnessApplication.applicationContext())
        )
    )
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
        // Simulation of animation


    }

    private fun animation(stringArray: Array<Int>) {
        var count = 0
        timer = object :
            CountDownTimer(12 * ONE_SECOND_IN_MILLIS.toLong(), ONE_SECOND_IN_MILLIS.toLong() * 3) {
            override fun onTick(millisUntilFinished: Long) {
                _voiceProcessingStatus.value = stringArray[count]
                if (count < stringArray.size - 1) {
                    count++
                }
            }

            override fun onFinish() {

            }
        }
        timer.start()
    }

    fun calculateScore(audioFilePath: String) {
        viewModelScope.launch {
            try {
                userRepository.getUser().collect {
                    when (it) {
                        is Result.Success -> {
                            val inference = InferenceEngine.createInference(
                                MentalFitnessApplication.applicationContext(),
                                MetaData(Gender.valueOf(it.data.sex), Integer.parseInt(it.data.birthYear))
                            )
                            if (mPassageModel.timerLength == 6) {
                                inference.inferScore(
                                    audioFilePath,
                                    HealthCheckType.RESPIRATORY_SYMPTOMS_RISK,
                                    object : InferenceCallback {
                                        override fun onError(throwable: Throwable) {
                                            deleteFile(audioFilePath)
                                            Log.d("getScore : ", "$throwable")
                                            timer.cancel()
                                            if (throwable is ElckFailedException) {
                                                _event.value = VoiceProcessingViewEvent.OnElckFailed
                                            } else {
                                                _event.value =
                                                    VoiceProcessingViewEvent.OnVoiceProcessError("")
                                            }
                                        }

                                        override fun onScore(score: Score) {
                                            deleteFile(audioFilePath)
                                            _event.value =
                                                VoiceProcessingViewEvent.OnVoiceProcessSuccess(
                                                    result(score.getValue()),
                                                    CheckInSessionManager.selectedFeelingAnswerOption,
                                                    CheckInSessionManager.selectedFeelingReasonList
                                                )
                                        }

                                    })
                            } else {
                                inference.inferScore(
                                    audioFilePath,
                                    HealthCheckType.MENTAL_FITNESS,
                                    object : InferenceCallback {
                                        override fun onError(throwable: Throwable) {
                                            deleteFile(audioFilePath)
                                            Log.d("getScore : ", "$throwable")
                                            timer.cancel()
                                            if (throwable is ElckFailedException) {
                                                _event.value = VoiceProcessingViewEvent.OnElckFailed
                                            } else {
                                                _event.value =
                                                    VoiceProcessingViewEvent.OnVoiceProcessError("")
                                            }
                                        }

                                        override fun onScore(score: Score) {
                                            deleteFile(audioFilePath)
                                            _event.postValue(
                                                VoiceProcessingViewEvent.OnMFScoreSuccess(
                                                    SubmitSessionModel("", ArrayList()),
                                                    score as VFFinalScore
                                                )
                                            )
                                        }

                                    })
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("calculateScore!!", "$e")
            }
        }
    }


    private fun result(score: Int): SubmitSessionModel {
        Log.d("result!!", "$score")
        val inferenceScoreList = ArrayList<InferenceScoreModel>()
        inferenceScoreList.add(InferenceScoreModel("", score, "Mental fitness"))
        return SubmitSessionModel(mPassageModel.prompt, inferenceScoreList)
    }

    fun setSelectedPassage(passageModel: PassageModel) {
        this.mPassageModel = passageModel

        if (mPassageModel.timerLength == 6) {
            val stringArray = arrayOf(
                R.string.analyzing_your_voice,
                R.string.understanding_your_mental_fitness,
                R.string.checking_additional_factors,
                R.string.finalizing_your_scores
            )
            animation(stringArray)
        } else {
            val stringArray = arrayOf(
                R.string.analyzing_your_voice,
                R.string.understanding_your_mental_risk,
                R.string.checking_additional_factors,
                R.string.finalizing_your_scores
            )
            animation(stringArray)
        }

    }

    private fun deleteFile(filePath: String) {
        try {
            File(filePath).delete()
        } catch (e: Exception) {
            Log.d("!!", "Error: " + e)
        }
    }

}