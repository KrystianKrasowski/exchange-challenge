package org.kkrasowski.exchange.domain

interface PeselValidator {

    fun isValid(pesel: Pesel): Boolean
}
