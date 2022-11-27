package org.kkrasowski.exchange.application.resources

import org.kkrasowski.exchange.infrastructure.db.UserAccountDetailsQuery
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class UserAccountDetailsResource(private val detailsQuery: UserAccountDetailsQuery) {

    @RequestMapping(
        path = ["/accounts/{pesel}"],
        method = [RequestMethod.GET],
        produces = ["application/vnd.account-details.v1+json"]
    )
    fun getAccountDetails(@PathVariable("pesel") pesel: String): ResponseEntity<AccountDetailsDto> {
        return detailsQuery
            .runCatching { findAccountDetails(pesel) }
            .map { it.toResponseEntity() }
            .getOrElse { ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR) }
    }

    private fun AccountDetailsDto?.toResponseEntity() = this
        ?.let { ResponseEntity.ok(it) }
        ?: ResponseEntity(HttpStatus.NOT_FOUND)
}

data class AccountDetailsDto(val firstName: String, val lastName: String, val accounts: List<BalanceDto>)

data class BalanceDto(val balance: MoneyDto)
