package org.kkrasowski.exchange.application.resources

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers.matchesRegex
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kkrasowski.exchange.infrastructure.db.UserAccountsJpaRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
class NewUserAccountResourceTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var accountsJpaRepository: UserAccountsJpaRepository

    @Test
    fun `should return 201 on successful account creation`() {
        val request = """{ "firstName": "John", "lastName": "Doe", "pesel": "86020313188", "startingBalanceInPLN": "150.99" }"""

        mvc.perform(post("/accounts")
            .contentType("application/vnd.new-account.v1+json")
            .content(request))
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", `is`("/accounts/86020313188")))
    }

    @Test
    fun `should create user account in storage`() {
        val request = """{ "firstName": "John", "lastName": "Doe", "pesel": "98113058179", "startingBalanceInPLN": "150.99" }"""

        mvc.perform(post("/accounts")
            .contentType("application/vnd.new-account.v1+json")
            .content(request))
            .andExpect(status().isCreated)

        val entity = accountsJpaRepository.getByPesel("98113058179")
        assertThat(entity).isNotNull
    }

    @Test
    fun `should make transaction with starting balance`() {
        val request = """{ "firstName": "John", "lastName": "Smith", "pesel": "82020347442", "startingBalanceInPLN": "150.99" }"""

        mvc.perform(post("/accounts")
            .contentType("application/vnd.new-account.v1+json")
            .content(request))
            .andExpect(status().isCreated)

        val balance = accountsJpaRepository.fetchBalanceByPeselAndCurrency("82020347442", "PLN")
        assertThat(balance).isEqualTo(BigDecimal.valueOf(150.99))
    }

    @Test
    fun `should create user account with empty balance`() {
        val request = """{ "firstName": "Nina", "lastName": "Smith", "pesel": "65020315383" }"""

        mvc.perform(post("/accounts")
            .contentType("application/vnd.new-account.v1+json")
            .content(request))
            .andExpect(status().isCreated)

        val balance = accountsJpaRepository.fetchBalanceByPeselAndCurrency("65020315383", "PLN")
        assertThat(balance).isNull()
    }

    @Test
    fun `should not create user account due to empty first name`() {
        val request = """{ "firstName": "", "lastName": "Doe", "pesel": "98113058179", "startingBalanceInPLN": "150.99" }"""

        mvc.perform(post("/accounts")
            .contentType("application/vnd.new-account.v1+json")
            .content(request))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(header().string("Content-Type", "application/vnd.constraint-violation.v1+json"))
            .andExpect(jsonPath("$.subject", `is`("firstName")))
            .andExpect(jsonPath("$.violation", `is`("IS_BLANK")))
    }

    @Test
    fun `should not create user account due to empty last name`() {
        val request = """{ "firstName": "John", "lastName": "", "pesel": "98113058179", "startingBalanceInPLN": "150.99" }"""

        mvc.perform(post("/accounts")
            .contentType("application/vnd.new-account.v1+json")
            .content(request))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(header().string("Content-Type", "application/vnd.constraint-violation.v1+json"))
            .andExpect(jsonPath("$.subject", `is`("lastName")))
            .andExpect(jsonPath("$.violation", `is`("IS_BLANK")))
    }

    @Test
    fun `should not create user account due to empty pesel`() {
        val request = """{ "firstName": "John", "lastName": "Dow", "startingBalanceInPLN": "150.99" }"""

        mvc.perform(post("/accounts")
            .contentType("application/vnd.new-account.v1+json")
            .content(request))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(header().string("Content-Type", "application/vnd.constraint-violation.v1+json"))
            .andExpect(jsonPath("$.subject", `is`("pesel")))
            .andExpect(jsonPath("$.violation", `is`("IS_BLANK")))
    }

    @Test
    fun `should not create user account due to already registered PESEL`() {
        val request = """{ "firstName": "John", "lastName": "Doe", "pesel": "93020362784", "startingBalanceInPLN": "150.99" }"""

        mvc.perform(post("/accounts")
            .contentType("application/vnd.new-account.v1+json")
            .content(request))
            .andExpect(status().isCreated)

        mvc.perform(post("/accounts")
            .contentType("application/vnd.new-account.v1+json")
            .content(request))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(header().string("Content-Type", "application/vnd.constraint-violation.v1+json"))
            .andExpect(jsonPath("$.subject", `is`("pesel")))
            .andExpect(jsonPath("$.violation", `is`("NOT_UNIQUE")))
    }

    @Test
    fun `should not create user account due to invalid pesel`() {
        val request = """{ "firstName": "John", "lastName": "Doe", "pesel": "00000000001", "startingBalanceInPLN": "150.99" }"""

        mvc.perform(post("/accounts")
            .contentType("application/vnd.new-account.v1+json")
            .content(request))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(header().string("Content-Type", "application/vnd.constraint-violation.v1+json"))
            .andExpect(jsonPath("$.subject", `is`("pesel")))
            .andExpect(jsonPath("$.violation", `is`("INVALID_VALUE")))
    }

    @Test
    fun `should not create user account due to user's age`() {
        val request = """{ "firstName": "John", "lastName": "Doe", "pesel": "17220345929", "startingBalanceInPLN": "150.99" }"""

        mvc.perform(post("/accounts")
            .contentType("application/vnd.new-account.v1+json")
            .content(request))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(header().string("Content-Type", "application/vnd.constraint-violation.v1+json"))
            .andExpect(jsonPath("$.subject", `is`("pesel")))
            .andExpect(jsonPath("$.violation", `is`("TOO_YOUNG")))
    }
}

