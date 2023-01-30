package com.sonde.mentalfitness.presentation.ui.signup

import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
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
import com.sonde.mentalfitness.domain.GenderType
import com.sonde.mentalfitness.domain.model.User
import com.sonde.mentalfitness.domain.usecase.SignUpUsecase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SignupStepViewModel : ViewModel() {

    var firstName = ObservableField("")
    var lastName = ObservableField("")
    var email = ObservableField("")
    var birthYear = ObservableField("")
    var genderType: GenderType = GenderType.NONE
    var isQuestionnaireChecked = ObservableBoolean(false)

    private val _state = MutableLiveData<SignupViewState>()
    val state: LiveData<SignupViewState>
        get() = _state

    private val _isValidated = MutableLiveData(false)
    val isValidated: LiveData<Boolean> get() = _isValidated

    var userRepository = UserRepositoryImpl(
        UserRemoteDataSourceImpl(RetrofitBuilder.apiService),
        UserLocalDataSourceImpl(
            AppDatabase.db.userDao(),
            SharedPreferenceServiceImpl(MentalFitnessApplication.applicationContext())
        )
    )
    val signUpUsecase: SignUpUsecase = SignUpUsecase(
        userRepository
    )

    val textFieldCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            _isValidated.postValue(
                isValid(
                    firstName.get(),
                    lastName.get(),
                    genderType,
                    birthYear.get()
                )
            )
        }
    }

    init {
        firstName.addOnPropertyChangedCallback(textFieldCallback)
        lastName.addOnPropertyChangedCallback(textFieldCallback)
        birthYear.addOnPropertyChangedCallback(textFieldCallback)
        email.addOnPropertyChangedCallback(textFieldCallback)
    }


    fun onRegisterButtonClicked() {
        _state.postValue(SignupViewState.Loading)
        viewModelScope.launch {
            signUpUsecase.doSignup(
                User(
                    firstName.get()!!,
                    lastName.get(),
                    genderType.name,
                    birthYear.get()!!,
                    email.get()!!,1234
                )
            ).collect {
                when (it) {
                    is Result.Success -> {
                        userRepository.setQuestionnaireSkipped(!isQuestionnaireChecked.get())
                        _state.postValue(SignupViewState.SignupComplete)
                    }
                }
            }
        }
    }


    fun setUserGender(genderType: GenderType) {
        this.genderType = genderType
        _isValidated.postValue(
            isValid(
                firstName.get(),
                lastName.get(),
                genderType,
                birthYear.get()
            )
        )
    }

    private fun isValid(
        firstName: String?,
        lastName: String?,
        genderType: GenderType?,
        birthYear: String?
    ): Boolean {
        if (firstName != null && firstName.length >= 2 && (lastName.isNullOrEmpty() || lastName.length >= 2) && genderType != null && genderType != GenderType.NONE && birthYear != null && birthYear.length >= 4) {
            return true
        }
        return false
    }

}