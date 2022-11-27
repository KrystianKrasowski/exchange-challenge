package org.kkrasowski.exchange.domain.account

import org.javamoney.moneta.Money
import org.kkrasowski.exchange.domain.*
import java.math.BigDecimal
import java.time.Clock

data class CreateUserAccountRequest constructor(
    val firstName: String?,
    val lastName: String?,
    val pesel: String?,
    val startingBalanceInPLN: BigDecimal?
) {

    fun getConstraintViolations(peselValidator: PeselValidator, clock: Clock): List<ConstraintViolation> {
        return Validator(this, clock, peselValidator).getConstraintViolations()
    }

    fun toNewUserAccount() = NewUserAccount(
        firstName = firstName!!.trim(),
        lastName = lastName!!.trim(),
        pesel = Pesel(pesel!!.trim()),
        balance = startingBalanceInPLN?.let { Money.of(it, "PLN") }
    )
}

private class Validator(private val request: CreateUserAccountRequest,
                        private val clock: Clock,
                        private val peselValidator: PeselValidator
) {

    private val firstNameBlankViolation
        get() = ConstraintViolation("firstName", Violation.IS_BLANK)
            .takeIf { request.firstName.isNullOrBlank() }

    private val lastNameBlankViolation
        get() = ConstraintViolation("lastName", Violation.IS_BLANK)
            .takeIf { request.lastName.isNullOrBlank() }

    private val peselBlankViolation
        get() = ConstraintViolation("pesel", Violation.IS_BLANK)
            .takeIf { request.pesel.isNullOrBlank() }

    private val peselInvalidViolation
        get() = ConstraintViolation("pesel", Violation.INVALID_VALUE)
            .takeIf { request.pesel != null && !Pesel(request.pesel).isValid(peselValidator) }

    private val peselTooYoungViolation
        get() = ConstraintViolation("pesel", Violation.TOO_YOUNG)
            .takeIf { request.pesel != null && Pesel(request.pesel).isValid(peselValidator) && Pesel(request.pesel).getAge(clock) < 18 }

    fun getConstraintViolations(): List<ConstraintViolation> {
        return listOfNotNull(
            firstNameBlankViolation,
            lastNameBlankViolation,
            peselBlankViolation,
            peselInvalidViolation,
            peselTooYoungViolation
        )
    }
}
