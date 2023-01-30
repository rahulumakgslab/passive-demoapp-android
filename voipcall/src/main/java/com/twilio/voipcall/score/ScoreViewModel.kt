package com.twilio.voipcall.score

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twilio.voipcall.domain.model.score.ScoreRepresentationModel
import com.twilio.voipcall.utils.MMMM_D_YYYY_DATE_PATTERN
import com.twilio.voipcall.utils.getCurrentDateStr
import kotlinx.coroutines.launch

class ScoreViewModel : ViewModel() {

    val event = MutableLiveData<ScoreViewEvent>()

    private val _scoreRepresentationModel = MutableLiveData<ScoreRepresentationModel>()
    val scoreRepresentationModel: LiveData<ScoreRepresentationModel> get() = _scoreRepresentationModel


    fun prepareScoreRepresentationModel(
        score: Int
    ) {

        viewModelScope.launch {

            val data = ScoreRepresentationModel(
                date = getCurrentDateStr(MMMM_D_YYYY_DATE_PATTERN),
                titleScoreRepresentation = buildTitleScoreForRespiratoryModel(
                    score
                )
            )
            _scoreRepresentationModel.value = data
        }
    }


    fun onDoneButtonClicked() {
        event.postValue(ScoreViewEvent.OnDoneClicked)
    }

    fun onScoreInfoClick() {
        event.postValue(ScoreViewEvent.OnScoreInfoClicked)
    }
}