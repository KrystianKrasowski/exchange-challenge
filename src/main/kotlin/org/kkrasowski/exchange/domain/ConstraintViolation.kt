package org.kkrasowski.exchange.domain

data class ConstraintViolation(val subject: String, val violation: Violation)

enum class Violation {
    IS_BLANK,
    NOT_UNIQUE,
    INVALID_VALUE,
    TOO_YOUNG,
    IS_UNSUPPORTED,
    IS_NEGATIVE,
    IS_NOT_REGISTERED
}
