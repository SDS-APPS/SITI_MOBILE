package com.siti.mobile.mvvm.util.helpers

import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

class CustomStringHelper {
    companion object {
        fun getHour(date: String): String {
            val hourFormat = date.substring(0, 16)
            val localDateTime = LocalDateTime.parse(hourFormat)
            val epochSeconds = localDateTime.toEpochSecond(ZoneOffset.UTC)
//            val zone = ZoneOffset.ofHoursMinutes(5, 30)
            val zone = ZoneOffset.ofHoursMinutes(0, 0)
            val actual =
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), zone)
            val hour = actual.getHour()
            val minute = actual.getMinute()
            val stringHour: String?
            val stringMinute: String?
            if (hour < 9) {
                stringHour = "0" + hour
            } else {
                stringHour = "" + hour
            }
            if (minute < 9) {
                stringMinute = "0" + minute
            } else {
                stringMinute = "" + minute
            }
            return "$stringHour:$stringMinute"
        }
    }
}