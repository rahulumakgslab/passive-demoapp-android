package com.sonde.mentalfitness.presentation.ui

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
import java.util.*

class MainViewModel : ViewModel() {

    val event = MutableLiveData<MainViewEvent>()
    private val userRepository = UserRepositoryImpl(
        UserRemoteDataSourceImpl(RetrofitBuilder.apiService),
        UserLocalDataSourceImpl(
            AppDatabase.db.userDao(),
            SharedPreferenceServiceImpl(MentalFitnessApplication.applicationContext())
        )
    )


    fun onPassiveCallButtonClick() {
        viewModelScope.launch {
            userRepository.getUser().collect {
                when (it) {
                    is Result.Success -> {
                        event.value =
                            MainViewEvent.PassCallEvent(it.data.firstName.toLowerCase(Locale.US))
                    }
                }
            }
        }

    }
}