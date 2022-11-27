package org.kkrasowski.exchange.infrastructure.db

import org.kkrasowski.exchange.domain.account.Transaction
import org.springframework.data.jpa.repository.JpaRepository
import java.math.BigDecimal
import java.sql.Timestamp
import javax.persistence.*

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
    open var id: Long? = null,

    @Column(name = "transaction_id")
    open var transactionId: String? = null,

    @Column(name = "account_id")
    open var accountId: Long? = null,

    @Column(name = "amount")
    open var amount: BigDecimal? = null,

    @Column(name = "currency")
    open var currency: String? = null,

    @Column(name = "created_at")
    open var createdAt: Timestamp? = null,
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
