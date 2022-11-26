package org.kkrasowski.exchange.infrastructure

import org.kkrasowski.exchange.domain.TransactionId
import org.kkrasowski.exchange.domain.TransactionIdGenerator
import java.util.*

class UUIDTransactionIdGenerator : TransactionIdGenerator {

    override fun generate(): TransactionId = UUID.randomUUID()
        .toString()
        .let { TransactionId(it) }
}
