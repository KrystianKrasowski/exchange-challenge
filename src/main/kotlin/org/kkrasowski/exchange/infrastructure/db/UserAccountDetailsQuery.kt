package org.kkrasowski.exchange.infrastructure.db

import org.kkrasowski.exchange.application.resources.dto.AccountDetailsDto
import org.kkrasowski.exchange.application.resources.dto.BalanceDto
import org.kkrasowski.exchange.application.resources.dto.MoneyDto
import java.math.BigDecimal

class UserAccountDetailsQuery(private val accountsJpaRepository: UserAccountsJpaRepository) {

    fun findAccountDetails(pesel: String): AccountDetailsDto? {
        val userAccount = accountsJpaRepository.findByPesel(pesel) ?: return null

        val plnBalance = accountsJpaRepository.fetchBalanceByPeselAndCurrency(pesel, "PLN")
        val usdBalance = accountsJpaRepository.fetchBalanceByPeselAndCurrency(pesel, "USD")

        return AccountDetailsDto(
            firstName = userAccount.firstName!!,
            lastName = userAccount.lastName!!,
            accounts = listOf(
                BalanceDto(balance = MoneyDto(plnBalance ?: BigDecimal.ZERO, "PLN")),
                BalanceDto(balance = MoneyDto(usdBalance ?: BigDecimal.ZERO, "USD"))
            )
        )
    }
}

