package com.twilio.voipcall

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonde.mentalfitness.presentation.utils.util.isNetworkAvailable
import com.twilio.voipcall.data.RetrofitBuilder
import com.twilio.voipcall.data.TwilioRepositoryImpl
import com.twilio.voipcall.domain.TwilioRepository
import kotlinx.coroutines.launch

class VoiceVideoModel : ViewModel() {
    val forceFetchSuccess = MutableLiveData<Boolean>()
    private var twilioRepository: TwilioRepository =
        TwilioRepositoryImpl(RetrofitBuilder.apiService)
    private lateinit var accessToken: String


    fun fetchAccessToken(userName: String, forceFetch: Boolean) {
        if (isNetworkAvailable()) {
            viewModelScope.launch {
                accessToken = twilioRepository.getAccessToken(userName)
                if (forceFetch) {
                    forceFetchSuccess.value = true
                }
            }
        }
    }

    fun isAccessTokenFetched(): Boolean {
        return this::accessToken.isInitialized
    }

    fun getAccessToken(): String {
        return accessToken
    }
}