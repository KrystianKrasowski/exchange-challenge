package org.kkrasowski.exchange

import org.kkrasowski.exchange.domain.TransactionIdGenerator
import org.kkrasowski.exchange.domain.account.NewUserAccountUseCase
import org.kkrasowski.exchange.domain.account.PeselValidator
import org.kkrasowski.exchange.domain.account.TransactionsRepository
import org.kkrasowski.exchange.domain.account.UserAccountsRepository
import org.kkrasowski.exchange.domain.exchange.ExchangeUseCase
import org.kkrasowski.exchange.infrastructure.HibernatePeselValidator
import org.kkrasowski.exchange.infrastructure.UUIDTransactionIdGenerator
import org.kkrasowski.exchange.infrastructure.db.*
import org.kkrasowski.exchange.infrastructure.external.api.NbpClient
import org.kkrasowski.exchange.infrastructure.external.api.NbpExchangeRatesProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import java.time.Clock
import javax.money.convert.ExchangeRateProvider

@SpringBootApplication
open class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@Configuration
open class Configuration {

    @Bean
    open fun userAccountsUseCase(accounts: UserAccountsRepository,
                                 transactions: TransactionsRepository,
                                 peselValidator: PeselValidator,
                                 transactionIdGenerator: TransactionIdGenerator,
                                 clock: Clock): NewUserAccountUseCase {
        return NewUserAccountUseCase(accounts, transactions, peselValidator, transactionIdGenerator, clock)
    }

    @Bean
    open fun exchangeUseCase(accountsRepository: UserAccountsRepository,
                             transactionsRepository: TransactionsRepository,
                             exchangeRateProvider: ExchangeRateProvider,
                             clock: Clock): ExchangeUseCase {
        return ExchangeUseCase(accountsRepository, transactionsRepository, exchangeRateProvider, clock)
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
    open fun transactionIdGenerator(): TransactionIdGenerator {
        return UUIDTransactionIdGenerator()
    }

    @Bean
    open fun exchangeRateProvider(nbpClient: NbpClient): ExchangeRateProvider {
        return NbpExchangeRatesProvider(nbpClient)
    }

    @Bean
    open fun nbpClient(@Value("\${nbp-api-address}") apiAddress: String): NbpClient {
        return NbpClient(RestTemplate(), apiAddress)
    }

    @Bean
    open fun userAccountDetailsQuery(jpaRepository: UserAccountsJpaRepository): UserAccountDetailsQuery {
        return UserAccountDetailsQuery(jpaRepository)
    }

    @Bean
    open fun clock(): Clock {
        return Clock.systemUTC()
    }
}
