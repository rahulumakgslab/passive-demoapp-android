package com.sonde.mentalfitness.presentation.ui.record.countdown

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sonde.mentalfitness.domain.model.checkin.PassageModel
import com.sonde.mentalfitness.presentation.utils.livedata.SingleLiveData

class CountDownViewModel : ViewModel()  {

    val event = SingleLiveData<CountDownViewEvent>()

    private val _passageModel = MutableLiveData<PassageModel>()
    val passageModel: LiveData<PassageModel> get() = _passageModel

    fun setPassage(passageModel: PassageModel) {
        _passageModel.postValue(passageModel)
    }

    fun onCancelClicked(){
        event.postValue(CountDownViewEvent.OnCancelClicked)
    }
}