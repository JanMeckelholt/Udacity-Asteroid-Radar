package com.udacity.asteroidradar.api

import com.udacity.asteroidradar.Constants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

fun formatDate(date : Date) : String {
    val dateFormat = SimpleDateFormat(Constants.ASTEROID_API_QUERY_DATE_FORMAT, Locale.getDefault())
    return dateFormat.format(date)
}

fun strToDate(dateStr :String) :Date{
    return SimpleDateFormat(Constants.ASTEROID_API_QUERY_DATE_FORMAT).parse(dateStr)
}

fun getFormatDateToday() : String {
    return formatDate(Calendar.getInstance().time)
}
fun getFormatDateInSevenDays() : String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, 7)
   return formatDate(calendar.time)
}

fun getNextSevenDaysFormattedDates(): ArrayList<String> {
    val formattedDateList = ArrayList<String>()

    val calendar = Calendar.getInstance()
    for (i in 0..Constants.ASTEROID_DEFAULT_END_DATE_DAYS) {
        val currentTime = calendar.time
        val dateFormat = SimpleDateFormat(Constants.ASTEROID_API_QUERY_DATE_FORMAT, Locale.getDefault())
        formattedDateList.add(dateFormat.format(currentTime))
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }

    return formattedDateList
}
