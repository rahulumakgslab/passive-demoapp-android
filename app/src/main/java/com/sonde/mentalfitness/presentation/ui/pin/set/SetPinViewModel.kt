package com.sonde.mentalfitness.presentation.ui.pin.set

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonde.mentalfitness.MentalFitnessApplication
import com.sonde.mentalfitness.data.Result
import com.sonde.mentalfitness.data.local.db.AppDatabase
import com.sonde.mentalfitness.data.local.sharedpref.SharedPreferenceServiceImpl
import com.sonde.mentalfitness.data.local.user.UserLocalDataSourceImpl
import com.sonde.mentalfitness.data.remote.network.RetrofitBuilder
import com.sonde.mentalfitness.data.remote.user.UserRemoteDataSourceImpl
import com.sonde.mentalfitness.data.repository.UserRepositoryImpl
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SetPinViewModel : ViewModel() {
    val pinInput = ObservableField("")
    val confirmPinInput = ObservableField("")
    private val _isValidated = MutableLiveData(false)
    val isValidated: LiveData<Boolean> get() = _isValidated

    private val _state = MutableLiveData<SetPinViewState>()
    val state: LiveData<SetPinViewState>
        get() = _state
    private val _event = MutableLiveData<SetPinViewEvent>()
    val event: LiveData<SetPinViewEvent> get() = _event

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
                isValid(pinInput.get()!!, confirmPinInput.get()!!)
            )
        }
    }

    private fun isValid(pin: String, confirmPin: String): Boolean {
        return pin.length == 4 && pin.equals(confirmPin)
    }

    init {
        pinInput.addOnPropertyChangedCallback(textFieldCallback)
        confirmPinInput.addOnPropertyChangedCallback(textFieldCallback)
    }

    fun onSetButtonClick() {
        viewModelScope.launch {
            userRepository.setAccessPin(pinInput.get()!!)
                .collect {
                    when (it) {
                        is Result.Success -> {
                            _event.postValue(SetPinViewEvent.PinSetComplete)
                        }
                    }
                }
        }
    }

    fun onNoPinButtonClick() {
        viewModelScope.launch {
            userRepository.setNoPinOption()
        }
    }
}