package com.sonde.mentalfitness.presentation.ui.howitwork

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sonde.mentalfitness.presentation.utils.livedata.SingleLiveData
import com.sonde.mentalfitness.presentation.utils.util.getCurrentDateWithMonthAndTh

class HowItWorksViewModel : ViewModel() {

    val event = SingleLiveData<HowItWorksViewEvent>()

    private val _todayDate = MutableLiveData<String>()
    val todayDate: LiveData<String> get() = _todayDate

    init {
        _todayDate.value = getCurrentDateWithMonthAndTh()
    }

    fun onStartFirstCheckInClicked() {
        event.postValue(HowItWorksViewEvent.NavigateToCheckIn)
    }
}