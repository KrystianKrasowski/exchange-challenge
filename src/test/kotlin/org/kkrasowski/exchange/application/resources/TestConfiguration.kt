package org.kkrasowski.exchange.application.resources

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@Configuration
@Profile("test")
open class TestConfiguration {

    @Bean
    open fun clock(): Clock {
        return Clock.fixed(Instant.parse("2022-11-27T15:00:00.00Z"), ZoneId.of("UTC"))
    }
}
