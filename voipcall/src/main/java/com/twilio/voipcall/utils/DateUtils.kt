package com.twilio.voipcall.utils

import java.text.SimpleDateFormat
import java.util.*

const val TYPE_LOCAL = 1
const val TYPE_UTC = 2

const val MMMM_DD_DATE_PATTERN = "MMMM d"
const val SERVER_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'"
const val MMMM_D_YYYY_DATE_PATTERN = "MMMM d, yyyy"

const val ONE_SECOND_IN_MILLIS = 1000


fun getCurrentDateWithMonthAndTh(): String {

    val currentDateCal = Calendar.getInstance()
    val currentDate = Date(currentDateCal.timeInMillis)
    val format = SimpleDateFormat(MMMM_DD_DATE_PATTERN, Locale.getDefault())
    val format1 = format.format(currentDate)
    return format1 + getDayOfMonthSuffix(currentDateCal.get(Calendar.DATE))
}

fun getCurrentDateStr(datePattern: String, type: Int = TYPE_LOCAL): String {
    val currentDateCal = Calendar.getInstance()
    val currentDate = Date(currentDateCal.timeInMillis)
    val simpleDateFormat = getSimpleDateFormat(datePattern, type)
    return simpleDateFormat.format(currentDate)
}

private fun getSimpleDateFormat(format: String, type: Int = TYPE_LOCAL): SimpleDateFormat {
    return if (type == TYPE_LOCAL) {
        SimpleDateFormat(format, Locale.ENGLISH)
    } else {
        val utcDateFormat = SimpleDateFormat(format, Locale.ENGLISH)
        utcDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        utcDateFormat
    }

}

private fun getDayOfMonthSuffix(n: Int): String {
    return if (n in 1..31) {
        if (n in 11..13) {
            "th"
        } else when (n % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    } else
        ""
}

