package org.kkrasowski.exchange.domain.account

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import org.javamoney.moneta.Money
import org.kkrasowski.exchange.domain.ConstraintViolation
import org.kkrasowski.exchange.domain.TransactionIdGenerator
import org.kkrasowski.exchange.domain.Violation
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
        return request.validate()
            .map { it.toNewUserAccount() }
            .flatMap { createAccount(it) }
            .tap { createStartingBalance(it, request.startingBalanceInPLN) }
    }

    private fun CreateUserAccountRequest.validate(): Either<CreateAccountUseCaseFailure, CreateUserAccountRequest> {
        return getConstraintViolations(peselValidator, clock)
            .firstOrNull()
            ?.let { InvalidRequest(it) }
            ?.left()
            ?: right()
    }

    private fun createAccount(account: NewUserAccount): Either<CreateAccountUseCaseFailure, AccountId> {
        return accounts.create(account)
            .map { AccountId(it) }
            .mapLeft { handleAccountsRepositoryFailure(it) }
    }

    private fun createStartingBalance(accountId: AccountId, startingBalanceInPLN: BigDecimal?) {
        startingBalanceInPLN
            ?.let { Money.of(it, "PLN") }
            ?.let { Transaction(transactionIdGenerator.generate(), accountId.value, it, clock.instant()) }
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

data class AccountId(val value: Long)
