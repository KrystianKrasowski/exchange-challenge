package org.kkrasowski.exchange.domain

interface TransactionIdGenerator {

    fun generate(): TransactionId
}
