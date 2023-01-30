package com.sonde.mentalfitness.presentation.ui.voiceprocessing.calculationerror

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sonde.mentalfitness.presentation.utils.livedata.SingleLiveData

class ScoreCalculationErrorViewModel : ViewModel() {

    val event = SingleLiveData<ScoreCalculationErrorViewEvent>()

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun setErrorMessage(errorMessage: String) {
        _errorMessage.value = errorMessage
    }

    fun onGoBackAndTryAgainClicked() {
        event.postValue(ScoreCalculationErrorViewEvent.OnGoBackAndTryAgain)
    }

    fun onCancelClicked() {
        event.postValue(ScoreCalculationErrorViewEvent.OnCancel)
    }
}