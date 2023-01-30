package com.sonde.mentalfitness.data.remote.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

private const val HEADER_KEY_AUTHORIZATION = "Authorization"
private const val HEADER_KEY_CONTENT_TYPE = "application/json"
private const val HEADER_KEY_ACCEPT = "Content-Type"

class AuthInterceptor(context: Context) : Interceptor {
    private val sessionManager = SessionManager(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        sessionManager.getAuthToken()?.let {
            requestBuilder.addHeader(HEADER_KEY_AUTHORIZATION, it)
        }
        requestBuilder.addHeader(HEADER_KEY_ACCEPT, HEADER_KEY_CONTENT_TYPE)

        return chain.proceed(requestBuilder.build())
    }
}