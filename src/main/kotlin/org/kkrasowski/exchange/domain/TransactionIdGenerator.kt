package org.kkrasowski.exchange.domain

interface TransactionIdGenerator {

    fun generate(): TransactionId
}

data class TransactionId(val value: String)
