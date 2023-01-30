package com.sonde.mentalfitness.data.remote.network

import android.content.Context
import com.sonde.mentalfitness.BuildConfig
import com.sonde.mentalfitness.MentalFitnessApplication
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuilder {

    private const val BASE_URL = "https://d2u1wndd7r3e2j.cloudfront.net/" // move to buildConfig

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(buildOkHttpClient(MentalFitnessApplication.applicationContext()))
            .build()
    }

    val apiService: ApiService = getRetrofit().create(ApiService::class.java)

    private fun buildOkHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .addInterceptor(buildLoggingInterceptor())
            .build()
    }

    private fun buildLoggingInterceptor(): Interceptor {
        return HttpLoggingInterceptor().apply {
            if (BuildConfig.DEBUG) {
                level = HttpLoggingInterceptor.Level.BODY
            }
        }
    }
}