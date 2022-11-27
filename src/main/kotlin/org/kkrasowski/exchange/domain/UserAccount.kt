package org.kkrasowski.exchange.domain

import javax.money.MonetaryAmount

data class UserAccount(val id: AccountId, val firstName: String, val lastName: String, val pesel: Pesel)
data class NewUserAccount(val firstName: String, val lastName: String, val pesel: Pesel, val balance: MonetaryAmount?)
data class AccountId(val value: Long)
