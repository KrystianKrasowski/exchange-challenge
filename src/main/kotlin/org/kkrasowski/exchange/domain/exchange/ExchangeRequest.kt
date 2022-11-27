package org.kkrasowski.exchange.domain.exchange

import arrow.core.Validated
import arrow.core.invalid
import arrow.core.valid
import org.kkrasowski.exchange.domain.ConstraintViolation
import org.kkrasowski.exchange.domain.TransactionId
import org.kkrasowski.exchange.domain.Violation
import org.kkrasowski.exchange.domain.Pesel
import java.math.BigDecimal
import javax.money.CurrencyUnit
import javax.money.Monetary
import javax.money.MonetaryAmount

data class ExchangeRequest(val transactionId: String?,
                           val pesel: String?,
                           val amount: BigDecimal?,
                           val currency: String?,
                           val targetCurrency: String?) {

    val requiredTransactionId: TransactionId
        get() = transactionId
            ?.let { TransactionId(it) }
            ?: error("Transaction id is null")

    val requiredPesel: Pesel
        get() = pesel
            ?.let { Pesel(it) }
            ?: error("Pesel is null")

    val requiredAmount: MonetaryAmount
        get() = Monetary.getDefaultAmountFactory()
            .setNumber(amount)
            .setCurrency(currency)
            .create()

    val requiredTargetCurrency: CurrencyUnit
        get() = Monetary.getCurrency(targetCurrency)

    fun validate(): Validated<ConstraintViolation, ExchangeRequest> {
        return Validator(this).validate()
    }
}

private class Validator(private val request: ExchangeRequest) {

    private val supportedCurrencies = setOf("PLN", "USD")

    private val transactionIdIsBlank
        get() = ConstraintViolation("transactionId", Violation.IS_BLANK)
            .takeIf { request.transactionId.isNullOrBlank() }
            ?.invalid()

    private val peselIsBlank
        get() = ConstraintViolation("pesel", Violation.IS_BLANK)
            .takeIf { request.pesel.isNullOrBlank() }
            ?.invalid()

    private val amountIsBlank
        get() = ConstraintViolation("amount", Violation.IS_BLANK)
            .takeIf { request.amount == null }
            ?.invalid()

    private val amountIsNegative
        get() = ConstraintViolation("amount", Violation.IS_NEGATIVE)
            .takeIf { request.amount != null && request.amount < BigDecimal.ZERO }
            ?.invalid()

    private val currencyIsBlank
        get() = ConstraintViolation("currency", Violation.IS_BLANK)
            .takeIf { request.currency.isNullOrBlank() }
            ?.invalid()

    private val currencyIsUnsupported
        get() = ConstraintViolation("currency", Violation.IS_UNSUPPORTED)
            .takeIf { !supportedCurrencies.contains(request.currency) }
            ?.invalid()

    private val targetCurrencyIsBlank
        get() = ConstraintViolation("targetCurrency", Violation.IS_BLANK)
            .takeIf { request.targetCurrency.isNullOrBlank() }
            ?.invalid()

    private val targetCurrencyIsUnsupported
        get() = ConstraintViolation("targetCurrency", Violation.IS_UNSUPPORTED)
            .takeIf { !supportedCurrencies.contains(request.targetCurrency) }
            ?.invalid()

    fun validate(): Validated<ConstraintViolation, ExchangeRequest> {
        return transactionIdIsBlank
            ?: peselIsBlank
            ?: amountIsBlank
            ?: amountIsNegative
            ?: currencyIsBlank
            ?: currencyIsUnsupported
            ?: targetCurrencyIsBlank
            ?: targetCurrencyIsUnsupported
            ?: request.valid()
    }
}
