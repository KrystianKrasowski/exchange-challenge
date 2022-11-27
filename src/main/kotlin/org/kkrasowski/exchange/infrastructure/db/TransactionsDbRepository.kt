package org.kkrasowski.exchange.infrastructure.db

import arrow.core.Either
import arrow.core.None
import arrow.core.left
import arrow.core.right
import org.kkrasowski.exchange.domain.Transaction
import org.kkrasowski.exchange.domain.TransactionsRepository
import org.kkrasowski.exchange.domain.TransactionsRepositoryFailure
import org.springframework.dao.DataIntegrityViolationException

/*
 * This implementation could finally use some persistent queue and create transaction asynchronously
 * I suppose we do not need to have transaction stored immediately
 */
open class TransactionsDbRepository(private val jpaRepository: TransactionsJpaRepository) : TransactionsRepository {

    override fun create(transaction: Transaction): Either<TransactionsRepositoryFailure, None> {
        return jpaRepository
            .runCatching { save(TransactionEntity.of(transaction)) }
            .map { None.right() }
            .getOrElse { handleCreateError(it).left() }
    }

    override fun create(transactions: List<Transaction>): Either<TransactionsRepositoryFailure, None> {
        return jpaRepository
            .runCatching { saveAll(createEntities(transactions)) }
            .map { None.right() }
            .getOrElse { handleCreateError(it).left() }
    }

    private fun handleCreateError(throwable: Throwable) = when (throwable) {
        is DataIntegrityViolationException -> TransactionsRepositoryFailure.NOT_UNIQUE
        else -> TransactionsRepositoryFailure.FAILURE
    }

    private fun createEntities(transactions: List<Transaction>) = transactions.map { TransactionEntity.of(it) }
}
