package com.twilio.voipcall.data

import retrofit2.http.GET
import retrofit2.http.Query

interface TwilioApiService {
    @GET("access-token")
    suspend fun getAccessToken(@Query("identity") identity: String) : String
}