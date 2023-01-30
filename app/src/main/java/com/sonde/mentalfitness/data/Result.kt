package com.sonde.mentalfitness.data

import com.sonde.mentalfitness.data.model.ErrorResponse

sealed class Result<out T>{
    data class Success<out T> (val data : T) : Result<T>()
    data class DataError(val code: Int, val errorResponse: ErrorResponse?) : Result<Nothing>()
}
