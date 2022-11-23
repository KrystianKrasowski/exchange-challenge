package org.kkrasowski.exchange.domain.account

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

internal class PeselTest {

    private val clock = Clock.fixed(Instant.parse("2022-11-25T12:30:00.00Z"), ZoneId.of("UTC"))

    @ParameterizedTest
    @CsvSource(
        "00310314398, 22",
        "99110334163, 23"
    )
    fun `should return age`(peselValue: String, expectedAge: Int) {
        // given
        val pesel = Pesel(peselValue)

        // when
        val age = pesel.getAge(clock)

        // then
        Assertions.assertThat(age).isEqualTo(expectedAge)
    }
}
