package org.kkrasowski.exchange.infrastructure.external.api

import org.springframework.web.client.RestOperations
import java.math.BigDecimal
import java.math.RoundingMode
import javax.money.CurrencyUnit

class NbpClient(private val restOperations: RestOperations, private val apiAddress: String) {

    fun getExchangeRatesFor(currency: CurrencyUnit): ExchangeRates {
        return restOperations
            .getForEntity("$apiAddress/api/exchangerates/rates/c/${currency.currencyCode}", RatesResponse::class.java)
            .body!!
            .rates
            .first()
            .let { createExchangeRates(it) }
    }

    private fun createExchangeRates(it: Rate) = ExchangeRates(
        foreignPurchase = it.bid.toExchangeRate(),
        foreignSell = (1 / it.ask).toExchangeRate()
    )

    private fun Double.toExchangeRate() = toBigDecimal().setScale(4, RoundingMode.HALF_EVEN)
}

data class ExchangeRates(val foreignPurchase: BigDecimal, val foreignSell: BigDecimal)
