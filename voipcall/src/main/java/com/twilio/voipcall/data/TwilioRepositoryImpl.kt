package com.twilio.voipcall.data

import com.twilio.voipcall.domain.TwilioRepository

class TwilioRepositoryImpl(val twilioApiService: TwilioApiService) : TwilioRepository {
    override suspend fun getAccessToken(identity: String): String {
        return twilioApiService.getAccessToken(identity)
    }
}