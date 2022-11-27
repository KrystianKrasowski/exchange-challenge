package org.kkrasowski.exchange.infrastructure.db

import org.kkrasowski.exchange.domain.NewUserAccount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.math.BigDecimal
import javax.persistence.*

interface UserAccountsJpaRepository : JpaRepository<UserAccountEntity, Long> {

    fun getByPesel(pesel: String): UserAccountEntity

    fun findByPesel(pesel: String): UserAccountEntity?

    @Query("SELECT SUM(t.amount) FROM UserAccountEntity u JOIN u.transactions t WHERE u.pesel = :pesel AND t.currency = :currency")
    fun fetchBalanceByPeselAndCurrency(pesel: String, currency: String): BigDecimal?
}

@Entity
@Table(name = "user_account")
open class UserAccountEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @OneToMany
    @JoinColumn(name = "account_id")
    open var transactions: MutableList<TransactionEntity> = mutableListOf(),

    @Column(name = "first_name")
    open var firstName: String? = null,

    @Column(name = "last_name")
    open var lastName: String? = null,

    @Column(name = "pesel", unique = true)
    open var pesel: String? = null
) {

    companion object {

        fun of(account: NewUserAccount) = UserAccountEntity(
            firstName = account.firstName,
            lastName = account.lastName,
            pesel = account.pesel.value
        )
    }
}
