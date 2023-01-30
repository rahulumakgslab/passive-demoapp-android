package com.sonde.mentalfitness.presentation.ui.score

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
import com.sonde.mentalfitness.domain.model.checkin.HealthCheckModel
import com.sonde.mentalfitness.domain.model.checkin.InferenceScoreModel
import com.sonde.mentalfitness.domain.model.checkin.OptionModel
import com.sonde.mentalfitness.domain.model.checkin.SubmitSessionModel
import com.sonde.mentalfitness.domain.model.checkin.score.ScoreFactorAdapterModel
import com.sonde.mentalfitness.domain.model.checkin.score.ScoreRepresentationModel
import com.sonde.mentalfitness.domain.model.checkin.score.TipModel
import com.sonde.mentalfitness.presentation.utils.livedata.SingleLiveData
import com.sonde.mentalfitness.presentation.utils.util.MMMM_D_YYYY_DATE_PATTERN
import com.sonde.mentalfitness.presentation.utils.util.getCurrentDateStr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class ScoreViewModel : ViewModel() {

    val event = SingleLiveData<ScoreViewEvent>()

    private val _scoreRepresentationModel = MutableLiveData<ScoreRepresentationModel>()
    val scoreRepresentationModel: LiveData<ScoreRepresentationModel> get() = _scoreRepresentationModel

    private val _tipModelList = MutableLiveData<List<TipModel>>()
    val tipModelList: LiveData<List<TipModel>> get() = _tipModelList

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

    fun prepareScoreRepresentationModel(
        sessionData: SubmitSessionModel,
        selectedFeelingAnswerOption: OptionModel?,
        selectedFeelingReasonList: ArrayList<OptionModel>?
    ) {

        viewModelScope.launch {
            checkInRepository.getCheckInConfiguration("Mental Fitness:v1").collect { it ->
                when (it) {
                    is Result.Success -> {
                        val mainIndex = it.data.healthChecks[it.data.mainHealthCheckIndex]
                        val mainInferenceScoreIndex: Int? = getMainHcIndex(sessionData, mainIndex)
                        mainInferenceScoreIndex?.let {
                            val mainScore: InferenceScoreModel =
                                sessionData.inferenceScoreList[mainInferenceScoreIndex]

                            val data = ScoreRepresentationModel(
                                date = getCurrentDateStr(MMMM_D_YYYY_DATE_PATTERN),
                                titleScoreRepresentation = buildTitleScoreForRespiratoryModel(
                                    mainScore.score
                                ),
                                scoreFactorAdapterList = prepareScoreFactorAdapterModel(
                                    sessionData.inferenceScoreList,
                                    selectedFeelingAnswerOption,
                                    selectedFeelingReasonList
                                )
                            )
                            _scoreRepresentationModel.value = data

                            initTipsData()
                        }
                    }
                }

            }
        }
    }

    private fun getMainHcIndex(
        sessionData: SubmitSessionModel,
        mainHealthCheckIndex: HealthCheckModel,
    ): Int? {

        sessionData.inferenceScoreList.forEachIndexed { index, inferenceScoreModel ->
            if (inferenceScoreModel.name == mainHealthCheckIndex.name) {
                return index
            }
        }
        return null
    }

    private fun prepareScoreFactorAdapterModel(
        inferenceScoreList: List<InferenceScoreModel>,
        selectedFeelingAnswerOption: OptionModel?,
        selectedFeelingReasonList: ArrayList<OptionModel>?
    ): List<ScoreFactorAdapterModel> {
        val scoreFactorAdapterModelList: ArrayList<ScoreFactorAdapterModel> = ArrayList()

        val feelingFactorModel =
            buildFeelingFactorAdapterModel(selectedFeelingAnswerOption, selectedFeelingReasonList)
        scoreFactorAdapterModelList.add(feelingFactorModel)

        val buildScoreFactorAdapterList = buildScoreFactorAdapterModel(inferenceScoreList)
        scoreFactorAdapterModelList.addAll(buildScoreFactorAdapterList)

        return scoreFactorAdapterModelList
    }

    fun onDoneButtonClicked() {
        event.postValue(ScoreViewEvent.OnDoneClicked)
    }

    fun onScoreInfoClick() {
        event.postValue(ScoreViewEvent.OnScoreInfoClicked)
    }

    private fun initTipsData() {
        val tipsList: ArrayList<TipModel> = ArrayList()
        tipsList.add(
            TipModel(
                "Get regular exercise",
                R.drawable.ic_tip_exercise_icon,
                "The benefits of excercise are more than just physical. Getting your body moving on a regular basis can also enhance your mood, improve your sleep, decrease anxiety, and more."
            )
        )
        tipsList.add(
            TipModel(
                "Practice meditation",
                R.drawable.ic_tip_exercise_icon,
                "The benefits of excercise are more than just physical. Getting your body moving on a regular basis can also enhance your mood, improve your sleep, decrease anxiety, and more."
            )
        )
        tipsList.add(
            TipModel(
                "Take a break",
                R.drawable.ic_tip_exercise_icon,
                "The benefits of excercise are more than just physical. Getting your body moving on a regular basis can also enhance your mood, improve your sleep, decrease anxiety, and more."
            )
        )

        _tipModelList.value = tipsList
    }
}