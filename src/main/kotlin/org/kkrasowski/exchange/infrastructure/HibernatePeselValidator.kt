package org.kkrasowski.exchange.infrastructure

import org.hibernate.validator.internal.constraintvalidators.hv.pl.PESELValidator
import org.kkrasowski.exchange.domain.account.Pesel
import org.kkrasowski.exchange.domain.account.PeselValidator

class HibernatePeselValidator : PeselValidator {

    override fun isValid(pesel: Pesel): Boolean {
        val validator = PESELValidator()
        validator.initialize(null)
        return validator.isValid(pesel.value, null)
    }
}
