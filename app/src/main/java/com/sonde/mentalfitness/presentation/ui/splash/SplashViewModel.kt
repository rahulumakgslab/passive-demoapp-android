package com.sonde.mentalfitness.presentation.ui.splash

import android.util.Log
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

class SplashViewModel : ViewModel() {

    private val _event = MutableLiveData<SplashViewEvent>()
    val event: LiveData<SplashViewEvent> get() = _event

    private val userRepository = UserRepositoryImpl(
        UserRemoteDataSourceImpl(RetrofitBuilder.apiService),
        UserLocalDataSourceImpl(
            AppDatabase.db.userDao(),
            SharedPreferenceServiceImpl(MentalFitnessApplication.applicationContext())
        )
    )

    init {
        initView()
    }

    private fun initView() {
        viewModelScope.launch {
            try {
                userRepository.getUser().collect {
                    when (it) {
                        is Result.Success -> {
                            userRepository.getUserPin().collect {
                                when (it) {
                                    is Result.Success -> {
                                        if (!it.data.isEmpty()) {
                                            _event.postValue(SplashViewEvent.ShowVerifyPin)
                                        } else {
                                            handleNoPinFlow()
                                        }
                                    }
                                    else -> {
                                        handleNoPinFlow()
                                    }
                                }
                            }
                        }
                        else -> {
                            _event.postValue(SplashViewEvent.UserNotLoggedIn)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("!!", "Error : " + e)
                _event.postValue(SplashViewEvent.UserNotLoggedIn)
            }
        }

    }

    private suspend fun handleNoPinFlow() {
        userRepository.getNoPinOption().collect {
            when (it) {
                is Result.Success -> {
                    if (it.data) {
                        _event.postValue(SplashViewEvent.NoVerifyPin)
                    } else {
                        _event.postValue(SplashViewEvent.ShowSetPin)
                    }
                }
            }
        }
    }
}