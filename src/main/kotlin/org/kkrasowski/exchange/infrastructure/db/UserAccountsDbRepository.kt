package org.kkrasowski.exchange.infrastructure.db

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.kkrasowski.exchange.domain.*
import org.kkrasowski.exchange.domain.account.*
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.EmptyResultDataAccessException

class UserAccountsDbRepository(private val jpaRepository: UserAccountsJpaRepository) : UserAccountsRepository {

    override fun create(account: NewUserAccount): Either<CreateAccountRepositoryFailure, Long> {
        return UserAccountEntity.of(account)
            .runCatching { jpaRepository.save(this) }
            .map { it.id!!.right() }
            .getOrElse { handleCreateError(it).left() }
    }

    override fun getByPesel(pesel: Pesel): Either<GetAccountByPeselRepositoryFailure, UserAccount> {
        return pesel
            .runCatching { jpaRepository.getByPesel(value) }
            .map { it.toUserAccount() }
            .map { it.right() }
            .getOrElse { handleGetByPeselError(it).left() }
    }

    private fun handleCreateError(throwable: Throwable) = when (throwable) {
        is DataIntegrityViolationException -> CreateAccountRepositoryFailure.PESEL_ALREADY_REGISTERED
        else -> CreateAccountRepositoryFailure.REPOSITORY_FAILURE
    }

    private fun handleGetByPeselError(throwable: Throwable) = when (throwable) {
        is EmptyResultDataAccessException -> GetAccountByPeselRepositoryFailure.PESEL_IS_NOT_REGISTERED
        else -> GetAccountByPeselRepositoryFailure.REPOSITORY_FAILURE
    }
}

private fun UserAccountEntity.toUserAccount() = UserAccount(
    id = AccountId(id!!),
    firstName = firstName!!,
    lastName = lastName!!,
    pesel = Pesel(pesel!!)
)
