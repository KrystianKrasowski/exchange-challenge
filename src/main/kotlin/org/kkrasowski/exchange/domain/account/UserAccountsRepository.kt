package org.kkrasowski.exchange.domain.account

import arrow.core.Either
import javax.money.MonetaryAmount

interface UserAccountsRepository {

    fun create(account: NewUserAccount): Either<CreateAccountRepositoryFailure, Long>
}

enum class CreateAccountRepositoryFailure {
    PESEL_ALREADY_REGISTERED,
    REPOSITORY_FAILURE
}

data class NewUserAccount(val firstName: String, val lastName: String, val pesel: Pesel, val balance: MonetaryAmount?)

