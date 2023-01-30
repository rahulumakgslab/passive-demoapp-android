package com.sonde.mentalfitness.domain.model

data class User(
    val firstName: String,
    val lastName: String?,
    val sex: String,
    val birthYear: String,
    val email:String,
    val pin:Int
)