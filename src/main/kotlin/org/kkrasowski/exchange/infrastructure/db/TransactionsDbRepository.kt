package org.kkrasowski.exchange.infrastructure.db

import org.kkrasowski.exchange.domain.account.Transaction
import org.kkrasowski.exchange.domain.account.TransactionsRepository
import org.springframework.data.jpa.repository.JpaRepository
import java.math.BigDecimal
import java.sql.Timestamp
import javax.persistence.*

/*
 * This implementation could finally use some persistent queue and create transaction asynchronously
 * I suppose we do not need to have transaction stored immediately
 */
class TransactionsDbRepository(private val jpaRepository: TransactionsJpaRepository) : TransactionsRepository {

    override fun create(transaction: Transaction) {
        jpaRepository.save(TransactionEntity.of(transaction))
    }
}

interface TransactionsJpaRepository : JpaRepository<TransactionEntity, Long>

@Entity
@Table(name = "transactions")
open class TransactionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

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
            accountId = transaction.accountId,
            amount = transaction.value.number.numberValue(BigDecimal::class.java),
            currency = transaction.value.currency.currencyCode,
            createdAt = transaction.createdAt.let { Timestamp.from(it) }
        )
    }
}
