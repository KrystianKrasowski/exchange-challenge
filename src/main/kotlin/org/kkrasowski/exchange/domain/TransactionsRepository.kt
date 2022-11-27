package org.kkrasowski.exchange.domain

import arrow.core.Either
import arrow.core.None

interface TransactionsRepository {

    fun create(transaction: Transaction): Either<TransactionsRepositoryFailure, None>

    fun create(transactions: List<Transaction>): Either<TransactionsRepositoryFailure, None>
}

enum class TransactionsRepositoryFailure {
    NOT_UNIQUE,
    FAILURE
}
