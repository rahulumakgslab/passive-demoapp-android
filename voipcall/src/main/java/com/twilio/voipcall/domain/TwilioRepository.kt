package com.twilio.voipcall.domain

interface TwilioRepository {
    suspend fun getAccessToken(identity : String) : String
}