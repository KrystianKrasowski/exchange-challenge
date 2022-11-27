package org.kkrasowski.exchange.application.resources.dto

import javax.money.MonetaryAmount

data class ExchangedDto(val value: MoneyDto) {

    companion object {

        fun of(value: MonetaryAmount) = ExchangedDto(
            value = MoneyDto.of(value)
        )
    }
}
