package com.sonde.mentalfitness.data.remote.network

import android.content.Context
import com.sonde.mentalfitness.data.local.sharedpref.SharedPreferenceServiceImpl
import com.sonde.mentalfitness.data.local.sharedpref.SharedPreferenceService

class SessionManager (context: Context) {

    private val sharedPreferences : SharedPreferenceService by lazy {
        SharedPreferenceServiceImpl(context)
    }


    fun saveAuthToken(token: String) {

    }


    fun getAuthToken(): String? {
        return sharedPreferences.getIdToken()
    }
}