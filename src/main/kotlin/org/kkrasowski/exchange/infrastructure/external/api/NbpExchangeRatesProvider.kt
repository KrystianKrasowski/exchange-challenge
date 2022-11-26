package org.kkrasowski.exchange.infrastructure.external.api

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.javamoney.moneta.convert.ExchangeRateBuilder
import org.javamoney.moneta.spi.AbstractRateProvider
import org.javamoney.moneta.spi.DefaultNumberValue
import java.math.BigDecimal
import javax.money.Monetary
import javax.money.convert.ConversionQuery
import javax.money.convert.ExchangeRate
import javax.money.convert.ProviderContextBuilder
import javax.money.convert.RateType

class NbpExchangeRatesProvider(private val nbpClient: NbpClient) : AbstractRateProvider(CONTEXT) {

    override fun getExchangeRate(conversionQuery: ConversionQuery): ExchangeRate {
        return ExchangeRateBuilder("NBP", RateType.DEFERRED)
            .setBase(conversionQuery.baseCurrency)
            .setTerm(conversionQuery.currency)
            .setFactor(DefaultNumberValue(getRate(conversionQuery)))
            .build()
    }

    private fun getRate(conversionQuery: ConversionQuery): BigDecimal {
        val rates = nbpClient.getExchangeRatesFor(conversionQuery.currency)

        return if (conversionQuery.baseCurrency == DOMESTIC_CURRENCY) {
            rates.foreignSell
        } else {
            rates.foreignPurchase
        }
    }
}

private val DOMESTIC_CURRENCY = Monetary.getCurrency("PLN")

data class RatesResponse @JsonCreator constructor(@JsonProperty("rates") val rates: List<Rate>)

data class Rate @JsonCreator constructor(
    @JsonProperty("bid") val bid: Double,
    @JsonProperty("ask") val ask: Double
)

private val CONTEXT = ProviderContextBuilder.of("NBP", RateType.DEFERRED)
    .set("providerDescription", "Narodowy Bank Polski")
    .set("days", 1)
    .build()
