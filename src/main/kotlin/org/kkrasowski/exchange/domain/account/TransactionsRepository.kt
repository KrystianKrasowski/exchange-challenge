package org.kkrasowski.exchange.domain.account

import arrow.core.Either
import arrow.core.None
import org.kkrasowski.exchange.domain.TransactionId
import java.time.Instant
import javax.money.MonetaryAmount

// TODO: Move one package up
interface TransactionsRepository {

    fun create(transaction: Transaction): Either<TransactionsRepositoryFailure, None>

    fun create(transactions: List<Transaction>): Either<TransactionsRepositoryFailure, None>
}

enum class TransactionsRepositoryFailure {
    NOT_UNIQUE,
    FAILURE
}

data class Transaction(val id: TransactionId, val accountId: Long, val value: MonetaryAmount, val createdAt: Instant)
