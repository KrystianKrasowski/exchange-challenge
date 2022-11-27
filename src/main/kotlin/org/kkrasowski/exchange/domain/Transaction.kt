package org.kkrasowski.exchange.domain

import java.time.Instant
import javax.money.MonetaryAmount

data class Transaction(val id: TransactionId, val accountId: AccountId, val value: MonetaryAmount, val createdAt: Instant)
data class TransactionId(val value: String)
