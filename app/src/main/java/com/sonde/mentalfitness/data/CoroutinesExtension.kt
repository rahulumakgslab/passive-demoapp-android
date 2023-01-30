package com.sonde.mentalfitness.data

import com.google.gson.Gson
import com.sonde.mentalfitness.data.model.ErrorResponse
import com.sonde.mentalfitness.presentation.utils.util.isNetworkAvailable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

const val ERROR_NO_INTERNET_CONNECTION = -101

object CoroutinesExtension {
    suspend fun <T> safeApiCall(
        dispatcher: CoroutineDispatcher,
        apiCall: suspend () -> T
    ): Result<T> {
        if (!isNetworkAvailable()) {
            return Result.DataError(ERROR_NO_INTERNET_CONNECTION, null)
        }

        return withContext(dispatcher) {
            try {
                Result.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                when (throwable) {
                    is IOException -> Result.DataError(0, null)
                    is HttpException -> {
                        val code = throwable.code()
                        val errorResponse = convertErrorBody(throwable)
                        Result.DataError(code, errorResponse)
                    }
                    else -> {
                        Result.DataError(0, ErrorResponse("", "", throwable.message ?: ""))
                    }
                }
            }
        }
    }

    suspend fun <T> runIO(
        dispatcher: CoroutineDispatcher,
        apiCall: suspend () -> T
    ): Flow<Result<T>> {
        return flow {
            withContext(dispatcher) {
                try {
                    Result.Success(apiCall())
                } catch (throwable: Throwable) {
                    Result.DataError(0, ErrorResponse("", "", throwable.message ?: "Unknown Error"))
                }
            }
        }
    }


    private fun convertErrorBody(throwable: HttpException): ErrorResponse? {
        return try {
            throwable.response()?.errorBody()?.charStream()?.let {
                Gson().fromJson(it, ErrorResponse::class.java)
            }
        } catch (exception: Exception) {
            null
        }
    }
}