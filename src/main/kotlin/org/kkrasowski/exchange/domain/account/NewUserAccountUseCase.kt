package org.kkrasowski.exchange.domain.account

import arrow.core.Either
import arrow.core.flatMap
import org.javamoney.moneta.Money
import org.kkrasowski.exchange.domain.*
import org.kkrasowski.exchange.domain.account.CreateAccountUseCaseFailure.Failure
import org.kkrasowski.exchange.domain.account.CreateAccountUseCaseFailure.InvalidRequest
import java.math.BigDecimal
import java.time.Clock

class NewUserAccountUseCase(
    private val accounts: UserAccountsRepository,
    private val transactions: TransactionsRepository,
    private val peselValidator: PeselValidator,
    private val transactionIdGenerator: TransactionIdGenerator,
    private val clock: Clock
) {

    fun create(request: CreateUserAccountRequest): Either<CreateAccountUseCaseFailure, AccountId> {
        return validate(request)
            .map { it.toNewUserAccount() }
            .flatMap { createAccount(it) }
            .tap { createStartingBalance(it, request.startingBalanceInPLN) }
    }

    private fun validate(request: CreateUserAccountRequest): Either<CreateAccountUseCaseFailure, CreateUserAccountRequest> {
        return request.validate(peselValidator, clock)
            .mapLeft { InvalidRequest(it) }
            .toEither()
    }

    private fun createAccount(account: NewUserAccount): Either<CreateAccountUseCaseFailure, AccountId> {
        return accounts.create(account)
            .map { AccountId(it) }
            .mapLeft { handleAccountsRepositoryFailure(it) }
    }

    private fun createStartingBalance(accountId: AccountId, startingBalanceInPLN: BigDecimal?) {
        startingBalanceInPLN
            ?.let { Money.of(it, "PLN") }
            ?.let { Transaction(transactionIdGenerator.generate(), accountId, it, clock.instant()) }
            ?.apply { transactions.create(this) }
    }

    private fun handleAccountsRepositoryFailure(failure: CreateAccountRepositoryFailure) = when (failure) {
        CreateAccountRepositoryFailure.PESEL_ALREADY_REGISTERED -> InvalidRequest(ConstraintViolation("pesel", Violation.NOT_UNIQUE))
        CreateAccountRepositoryFailure.REPOSITORY_FAILURE -> Failure
    }
}

sealed class CreateAccountUseCaseFailure {

    data class InvalidRequest(val violation: ConstraintViolation) : CreateAccountUseCaseFailure()
    object Failure : CreateAccountUseCaseFailure()
}

