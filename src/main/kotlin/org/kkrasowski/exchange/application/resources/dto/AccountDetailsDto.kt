package org.kkrasowski.exchange.application.resources.dto

data class AccountDetailsDto(val firstName: String, val lastName: String, val accounts: List<BalanceDto>)
