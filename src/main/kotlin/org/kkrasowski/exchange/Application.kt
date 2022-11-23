package org.kkrasowski.exchange

import org.kkrasowski.exchange.domain.account.NewUserAccountUseCase
import org.kkrasowski.exchange.domain.account.PeselValidator
import org.kkrasowski.exchange.domain.account.TransactionsRepository
import org.kkrasowski.exchange.domain.account.UserAccountsRepository
import org.kkrasowski.exchange.infrastructure.HibernatePeselValidator
import org.kkrasowski.exchange.infrastructure.db.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@SpringBootApplication
open class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@Configuration
open class Configuration {

    @Bean
    open fun userAccounts(accounts: UserAccountsRepository,
                          transactions: TransactionsRepository,
                          peselValidator: PeselValidator,
                          clock: Clock): NewUserAccountUseCase {
        return NewUserAccountUseCase(accounts, transactions, peselValidator, clock)
    }

    @Bean
    open fun userAccountsRepository(jpaRepository: UserAccountsJpaRepository): UserAccountsRepository {
        return UserAccountsDbRepository(jpaRepository)
    }

    @Bean
    open fun transactionsRepository(jpaRepository: TransactionsJpaRepository): TransactionsRepository {
        return TransactionsDbRepository(jpaRepository)
    }

    @Bean
    open fun peselValidator(): PeselValidator {
        return HibernatePeselValidator()
    }

    @Bean
    open fun clock(): Clock {
        return Clock.systemUTC()
    }
}
