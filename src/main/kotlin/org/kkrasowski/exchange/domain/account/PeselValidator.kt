package org.kkrasowski.exchange.domain.account

interface PeselValidator {

    fun isValid(pesel: Pesel): Boolean
}
