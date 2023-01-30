package com.sonde.mentalfitness.presentation.ui.pin.verify

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonde.mentalfitness.MentalFitnessApplication
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.data.Result
import com.sonde.mentalfitness.data.local.db.AppDatabase
import com.sonde.mentalfitness.data.local.sharedpref.SharedPreferenceServiceImpl
import com.sonde.mentalfitness.data.local.user.UserLocalDataSourceImpl
import com.sonde.mentalfitness.data.remote.network.RetrofitBuilder
import com.sonde.mentalfitness.data.remote.user.UserRemoteDataSourceImpl
import com.sonde.mentalfitness.data.repository.UserRepositoryImpl
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class VerifyPinViewModel : ViewModel() {
    val pinInput = ObservableField("")
    private val _isValidated = MutableLiveData(false)
    val isValidated: LiveData<Boolean> get() = _isValidated

    private val _state = MutableLiveData<VerifyPinViewState>()
    val state: LiveData<VerifyPinViewState>
        get() = _state
    private val _event = MutableLiveData<VerifyPinViewEvent>()
    val event: LiveData<VerifyPinViewEvent> get() = _event
    private val userRepository = UserRepositoryImpl(
        UserRemoteDataSourceImpl(RetrofitBuilder.apiService),
        UserLocalDataSourceImpl(
            AppDatabase.db.userDao(),
            SharedPreferenceServiceImpl(MentalFitnessApplication.applicationContext())
        )
    )

    val textFieldCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            _isValidated.postValue(
                isValid(pinInput.get()!!)
            )
        }
    }

    private fun isValid(pin: String): Boolean {
        return pin.length == 4
    }

    init {
        pinInput.addOnPropertyChangedCallback(textFieldCallback)
    }

    fun onVerifyButtonClick() {
        viewModelScope.launch {
            userRepository.getUserPin().collect {
                when (it) {
                    is Result.Success -> {
                        if (pinInput.get()!!.equals(it.data)) {
                            _event.postValue(VerifyPinViewEvent.Verified)
                        } else {
                            _state.postValue(VerifyPinViewState.ShowError(R.string.wrong_pin_msg))
                        }
                    }
                }
            }
        }

    }
}