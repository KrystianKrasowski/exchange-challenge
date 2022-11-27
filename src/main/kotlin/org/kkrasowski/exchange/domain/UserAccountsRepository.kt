package org.kkrasowski.exchange.domain

import arrow.core.Either

interface UserAccountsRepository {

    fun create(account: NewUserAccount): Either<CreateAccountRepositoryFailure, Long>
    fun getByPesel(pesel: Pesel): Either<GetAccountByPeselRepositoryFailure, UserAccount>
}

enum class CreateAccountRepositoryFailure {
    PESEL_ALREADY_REGISTERED,
    REPOSITORY_FAILURE
}

enum class GetAccountByPeselRepositoryFailure {
    PESEL_IS_NOT_REGISTERED,
    REPOSITORY_FAILURE
}
