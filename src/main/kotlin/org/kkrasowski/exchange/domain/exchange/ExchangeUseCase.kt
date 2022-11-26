package org.kkrasowski.exchange.domain.exchange

import arrow.core.*
import org.javamoney.moneta.Money
import org.kkrasowski.exchange.domain.ConstraintViolation
import org.kkrasowski.exchange.domain.TransactionId
import org.kkrasowski.exchange.domain.Violation
import org.kkrasowski.exchange.domain.account.*
import org.kkrasowski.exchange.domain.account.GetAccountByPeselRepositoryFailure.PESEL_IS_NOT_REGISTERED
import org.kkrasowski.exchange.domain.account.GetAccountByPeselRepositoryFailure.REPOSITORY_FAILURE
import org.kkrasowski.exchange.domain.account.TransactionsRepositoryFailure.*
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
            .flatMap { getAccountId(Pesel(it.pesel!!)) }
            .flatMap { exchange(request.toCommand(it)) }
            .flatMap { saveTransactions(it) }
    }

    private fun validate(request: ExchangeRequest) = request.validate()
        .mapLeft { InvalidRequest(it) }
        .toEither()

    private fun getAccountId(pesel: Pesel): Either<ExchangeUseCaseFailure, Long> {
        return accountsRepository.getByPesel(pesel)
            .map { it.id.right() }
            .getOrHandle { it.handleRepositoryError().left() }
    }

    private fun ExchangeRequest.toCommand(accountId: Long) = ExchangeCommand(
        accountId = accountId,
        transactionId = TransactionId(transactionId!!),
        money = Money.of(amount!!, currency!!),
        targetCurrencyUnit = Monetary.getCurrency(targetCurrency!!),
        exchangeRateProvider = exchangeRateProvider,
        clock = clock
    )

    private fun exchange(command: ExchangeCommand): Either<ExchangeUseCaseFailure, List<Transaction>> {
        return command
            .runCatching { exchange() }
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
}

sealed class ExchangeUseCaseFailure {

    data class InvalidRequest(val violation: ConstraintViolation) : ExchangeUseCaseFailure()
    object AccountsRepositoryUnavailable : ExchangeUseCaseFailure()
    object TransactionsRepositoryUnavailable : ExchangeUseCaseFailure()
    object ExchangeRatesUnavailable : ExchangeUseCaseFailure()
}
