package org.kkrasowski.exchange.application.resources

import arrow.core.getOrHandle
import org.kkrasowski.exchange.domain.account.CreateAccountUseCaseFailure
import org.kkrasowski.exchange.domain.exchange.ExchangeRequest
import org.kkrasowski.exchange.domain.exchange.ExchangeUseCase
import org.kkrasowski.exchange.domain.exchange.ExchangeUseCaseFailure
import org.kkrasowski.exchange.domain.exchange.ExchangeUseCaseFailure.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import javax.money.MonetaryAmount

@RestController
@RequestMapping("/exchange")
class ExchangeResource(private val exchangeUseCase: ExchangeUseCase) {

    @RequestMapping(
        method = [RequestMethod.PUT],
        consumes = ["application/vnd.exchange-command.v1+json"],
        produces = ["application/vnd.exchanged.v1+json"]
    )
    fun exchange(@RequestBody request: ExchangeRequest): ResponseEntity<*> {
        return exchangeUseCase.exchange(request)
            .map { it.toResponseEntity() }
            .getOrHandle { it.toResponseEntity() }
    }

    private fun MonetaryAmount.toResponseEntity() = ResponseEntity.status(201)
        .header("Content-Type", "application/vnd.exchanged.v1+json")
        .body(ExchangedDto.of(this))

    private fun ExchangeUseCaseFailure.toResponseEntity() = when (this) {
        is InvalidRequest -> toUnprocessableEntity()
        AccountsRepositoryUnavailable,
        TransactionsRepositoryUnavailable,
        ExchangeRatesUnavailable -> ResponseEntity.internalServerError().build()
    }

    private fun InvalidRequest.toUnprocessableEntity() = ResponseEntity.status(422)
        .header("Content-Type", "application/vnd.constraint-violation.v1+json")
        .body(violation)
}

data class ExchangedDto(val value: MoneyDto) {

    companion object {

        fun of(value: MonetaryAmount) = ExchangedDto(
            value = MoneyDto.of(value)
        )
    }
}

data class MoneyDto(val amount: BigDecimal, val currency: String) {

    companion object {

        fun of(money: MonetaryAmount) = MoneyDto(
            amount = money.number.numberValue(BigDecimal::class.java),
            currency = money.currency.currencyCode
        )
    }
}
