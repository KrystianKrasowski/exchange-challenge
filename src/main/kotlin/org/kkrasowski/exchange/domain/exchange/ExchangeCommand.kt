package org.kkrasowski.exchange.domain.exchange

import org.kkrasowski.exchange.domain.TransactionId
import org.kkrasowski.exchange.domain.account.Transaction
import java.time.Clock
import javax.money.CurrencyUnit
import javax.money.Monetary
import javax.money.MonetaryAmount
import javax.money.convert.ExchangeRateProvider

class ExchangeCommand(private val accountId: Long,
                      private val transactionId: TransactionId,
                      private val money: MonetaryAmount,
                      private val targetCurrencyUnit: CurrencyUnit,
                      private val exchangeRateProvider: ExchangeRateProvider,
                      private val clock: Clock
) {

    fun exchange(): List<Transaction> {
        return exchangeRateProvider
            .getCurrencyConversion(targetCurrencyUnit)
            .let { money.with(it) }
            .with(Monetary.getDefaultRounding())
            .let { createTransactions(it) }
    }

    private fun createTransactions(exchangedMoney: MonetaryAmount) = listOf(
        Transaction(
            id = transactionId,
            accountId = accountId,
            value = money.negate(),
            createdAt = clock.instant()
        ),
        Transaction(
            id = transactionId,
            accountId = accountId,
            value = exchangedMoney,
            createdAt = clock.instant()
        )
    )
}
