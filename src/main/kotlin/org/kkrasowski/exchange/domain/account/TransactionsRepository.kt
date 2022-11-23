package org.kkrasowski.exchange.domain.account

import java.time.Instant
import javax.money.MonetaryAmount

interface TransactionsRepository {

    fun create(transaction: Transaction)
}

data class Transaction(val accountId: Long, val value: MonetaryAmount, val createdAt: Instant)
