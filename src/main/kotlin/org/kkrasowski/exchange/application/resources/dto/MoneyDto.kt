package org.kkrasowski.exchange.application.resources.dto

import java.math.BigDecimal
import javax.money.MonetaryAmount

data class MoneyDto(val amount: BigDecimal, val currency: String) {

    companion object {

        fun of(money: MonetaryAmount) = MoneyDto(
            amount = money.number.numberValue(BigDecimal::class.java),
            currency = money.currency.currencyCode
        )
    }
}
