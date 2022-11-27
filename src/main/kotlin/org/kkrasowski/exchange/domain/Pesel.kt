package org.kkrasowski.exchange.domain

import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Pesel(val value: String) {

    fun isValid(validator: PeselValidator): Boolean {
        return validator.isValid(this)
    }

    fun getAge(clock: Clock): Int {
        val year = getYearOfBirth()
        val month = getMonthOfBirth()
        val day = value.substring(4, 6).toInt()

        val dateOfBirth = LocalDate.of(year, month, day)
        return ChronoUnit.YEARS.between(dateOfBirth, LocalDate.now(clock)).toInt()
    }

    private fun getYearOfBirth(): Int {
        val userYearOfBirth = value.substring(0, 2).toInt()
        val userMonthOfBirth = value.substring(2, 4).toInt()

        return if (userMonthOfBirth > 12) {
            userYearOfBirth + 2000
        } else {
            userYearOfBirth + 1900
        }
    }

    private fun getMonthOfBirth(): Int {
        val userMonthOfBirth = value.substring(2, 4).toInt()

        return if (userMonthOfBirth > 12) {
            userMonthOfBirth - 20
        } else {
            userMonthOfBirth
        }
    }
}
