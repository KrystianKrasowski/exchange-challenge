package org.kkrasowski.exchange.infrastructure.db

import arrow.core.Either
import arrow.core.None
import arrow.core.left
import arrow.core.right
import org.kkrasowski.exchange.domain.account.Transaction
import org.kkrasowski.exchange.domain.account.TransactionsRepository
import org.kkrasowski.exchange.domain.account.TransactionsRepositoryFailure
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.sql.Timestamp
import javax.persistence.*

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

interface TransactionsJpaRepository : JpaRepository<TransactionEntity, Long> {

    fun getByTransactionIdAndCurrency(transactionId: String, currency: String): TransactionEntity
}

@Entity
@Table(
    name = "transactions",
    uniqueConstraints = [
        UniqueConstraint(
            columnNames = ["transaction_id", "currency"]
        )
    ]
)
open class TransactionEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "transaction_id")
    var transactionId: String? = null,

    @Column(name = "account_id")
    var accountId: Long? = null,

    @Column(name = "amount")
    var amount: BigDecimal? = null,

    @Column(name = "currency")
    var currency: String? = null,

    @Column(name = "created_at")
    var createdAt: Timestamp? = null,
) {

    companion object {

        fun of(transaction: Transaction) = TransactionEntity(
            transactionId = transaction.id.value,
            accountId = transaction.accountId,
            amount = transaction.value.number.numberValue(BigDecimal::class.java),
            currency = transaction.value.currency.currencyCode,
            createdAt = transaction.createdAt.let { Timestamp.from(it) }
        )
    }
}
