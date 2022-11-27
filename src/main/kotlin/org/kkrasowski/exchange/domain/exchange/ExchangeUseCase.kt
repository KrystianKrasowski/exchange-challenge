package org.kkrasowski.exchange.domain.exchange

import arrow.core.*
import org.kkrasowski.exchange.domain.*
import org.kkrasowski.exchange.domain.GetAccountByPeselRepositoryFailure.PESEL_IS_NOT_REGISTERED
import org.kkrasowski.exchange.domain.GetAccountByPeselRepositoryFailure.REPOSITORY_FAILURE
import org.kkrasowski.exchange.domain.TransactionsRepositoryFailure.FAILURE
import org.kkrasowski.exchange.domain.TransactionsRepositoryFailure.NOT_UNIQUE
import org.kkrasowski.exchange.domain.exchange.ExchangeUseCaseFailure.*
import java.time.Clock
import javax.money.Monetary
import javax.money.MonetaryAmount
import javax.money.convert.ExchangeRateProvider

class ExchangeUseCase(private val accountsRepository: UserAccountsRepository,
                      private val transactionsRepository: TransactionsRepository,
                      private val exchangeRateProvider: ExchangeRateProvider,
                      private val clock: Clock) {

    fun exchange(request: ExchangeRequest): Either<ExchangeUseCaseFailure, MonetaryAmount> {
        return validate(request)
            .flatMap { getAccountId(it.requiredPesel) }
            .flatMap { exchange(it, request) }
            .flatMap { saveTransactions(it) }
    }

    private fun validate(request: ExchangeRequest) = request.validate()
        .mapLeft { InvalidRequest(it) }
        .toEither()

    private fun getAccountId(pesel: Pesel): Either<ExchangeUseCaseFailure, AccountId> {
        return accountsRepository.getByPesel(pesel)
            .map { it.id.right() }
            .getOrHandle { it.handleRepositoryError().left() }
    }

    private fun exchange(accountId: AccountId, request: ExchangeRequest): Either<ExchangeUseCaseFailure, List<Transaction>> {
        return exchangeRateProvider
            .runCatching { getCurrencyConversion(request.requiredTargetCurrency) }
            .map { request.requiredAmount.with(it) }
            .map { it.with(Monetary.getDefaultRounding()) }
            .map { createTransactions(request, accountId, it) }
            .map { it.right() }
            .getOrElse { ExchangeRatesUnavailable.left() }
    }

    private fun saveTransactions(transactions: List<Transaction>): Either<ExchangeUseCaseFailure, MonetaryAmount> {
        return transactionsRepository.create(transactions)
            .map { transactions[1].value.right() }
            .getOrHandle { it.handleRepositoryError().left() }
    }

    private fun GetAccountByPeselRepositoryFailure.handleRepositoryError() = when (this) {
        PESEL_IS_NOT_REGISTERED -> InvalidRequest(ConstraintViolation("pesel", Violation.IS_NOT_REGISTERED))
        REPOSITORY_FAILURE -> AccountsRepositoryUnavailable
    }

    private fun TransactionsRepositoryFailure.handleRepositoryError() = when (this) {
        NOT_UNIQUE -> InvalidRequest(ConstraintViolation("transactionId", Violation.NOT_UNIQUE))
        FAILURE -> TransactionsRepositoryUnavailable
    }

    private fun createTransactions(request: ExchangeRequest, accountId: AccountId, exchanged: MonetaryAmount) = listOf(
        Transaction(
            id = request.requiredTransactionId,
            accountId = accountId.value,
            value = request.requiredAmount.negate(),
            createdAt = clock.instant()
        ),
        Transaction(
            id = request.requiredTransactionId,
            accountId = accountId.value,
            value = exchanged,
            createdAt = clock.instant()
        )
    )
}

sealed class ExchangeUseCaseFailure {

    data class InvalidRequest(val violation: ConstraintViolation) : ExchangeUseCaseFailure()
    object AccountsRepositoryUnavailable : ExchangeUseCaseFailure()
    object TransactionsRepositoryUnavailable : ExchangeUseCaseFailure()
    object ExchangeRatesUnavailable : ExchangeUseCaseFailure()
}
