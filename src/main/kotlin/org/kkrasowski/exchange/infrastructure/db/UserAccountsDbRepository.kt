package org.kkrasowski.exchange.infrastructure.db

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.kkrasowski.exchange.domain.account.*
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.math.BigDecimal
import javax.persistence.*

class UserAccountsDbRepository(private val jpaRepository: UserAccountsJpaRepository) : UserAccountsRepository {

    override fun create(account: NewUserAccount): Either<CreateAccountRepositoryFailure, Long> {
        return UserAccountEntity.of(account)
            .runCatching { jpaRepository.save(this) }
            .map { it.id!!.right() }
            .getOrElse { handleThrowable(it).left() }
    }

    private fun handleThrowable(throwable: Throwable): CreateAccountRepositoryFailure {
        return when (throwable) {
            is DataIntegrityViolationException -> CreateAccountRepositoryFailure.PESEL_ALREADY_REGISTERED
            else -> CreateAccountRepositoryFailure.REPOSITORY_FAILURE
        }
    }
}

interface UserAccountsJpaRepository : JpaRepository<UserAccountEntity, Long> {

    fun getByPesel(pesel: String): UserAccountEntity

    @Query("SELECT SUM(t.amount) FROM UserAccountEntity u JOIN u.transactions t WHERE u.pesel = :pesel AND t.currency = :currency")
    fun fetchBalanceByPeselAndCurrency(pesel: String, currency: String): BigDecimal?
}

@Entity
@Table(name = "user_account")
open class UserAccountEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToMany
    @JoinColumn(name = "account_id")
    var transactions: MutableList<TransactionEntity> = mutableListOf(),

    @Column(name = "first_name")
    var firstName: String? = null,

    @Column(name = "last_name")
    var lastName: String? = null,

    @Column(name = "pesel", unique = true)
    var pesel: String? = null
) {

    companion object {

        fun of(account: NewUserAccount) = UserAccountEntity(
            firstName = account.firstName,
            lastName = account.lastName,
            pesel = account.pesel.value
        )
    }
}
