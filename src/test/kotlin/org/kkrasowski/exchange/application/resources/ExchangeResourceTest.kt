package org.kkrasowski.exchange.application.resources

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.kkrasowski.exchange.infrastructure.db.TransactionsJpaRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExchangeResourceTest {

    companion object {

        private lateinit var wireMockServer: WireMockServer

        @BeforeAll
        @JvmStatic
        fun init() {
            wireMockServer = WireMockServer(WireMockConfiguration().port(7070))
            wireMockServer.start()
            configureFor(7070)
        }
    }

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var transactionsJpaRepository: TransactionsJpaRepository

    @BeforeEach
    fun setUp() {
        mockUSDExchangeRate(4.4642, 4.5544)
    }

    @Test
    fun `should exchange value`() {
        createUserAccount("87010424178", "500")

        val request = """{ "transactionId": "1111", "pesel": "87010424178", "amount": 50.00, "currency": "PLN", "targetCurrency": "USD" }"""

        mvc.perform(put("/exchange")
            .contentType("application/vnd.exchange-command.v1+json")
            .content(request))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.value.amount", `is`(10.98)))
            .andExpect(jsonPath("$.value.currency", `is`("USD")))
    }

    @Test
    fun `should create exchange transactions`() {
        createUserAccount("87010433361", "500")

        val request = """{ "transactionId": "2222", "pesel": "87010433361", "amount": 50.49, "currency": "PLN", "targetCurrency": "USD" }"""

        mvc.perform(put("/exchange")
            .contentType("application/vnd.exchange-command.v1+json")
            .content(request))

        val charge = transactionsJpaRepository.getByTransactionIdAndCurrency("2222", "PLN")
        val credit = transactionsJpaRepository.getByTransactionIdAndCurrency("2222", "USD")
        assertThat(charge.amount).isEqualTo((-50.49).toBigDecimal())
        assertThat(credit.amount).isEqualTo(11.09.toBigDecimal())
    }

    @Test
    fun `should not exchange value for the second call with same transaction id`() {
        createUserAccount("87010432742", "500")

        val request = """{ "transactionId": "3333", "pesel": "87010432742", "amount": 50.00, "currency": "PLN", "targetCurrency": "USD" }"""

        mvc.perform(put("/exchange")
            .contentType("application/vnd.exchange-command.v1+json")
            .content(request))
            .andExpect(status().isCreated)

        mvc.perform(put("/exchange")
            .contentType("application/vnd.exchange-command.v1+json")
            .content(request))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(header().string("Content-Type", "application/vnd.constraint-violation.v1+json"))
            .andExpect(jsonPath("$.subject", `is`("transactionId")))
            .andExpect(jsonPath("$.violation", `is`("NOT_UNIQUE")))
    }

    @Test
    fun `should not exchange value due to empty transactionId`() {
        val request = """{ "pesel": "87010438175", "amount": 50.00, "currency": "PLN", "targetCurrency": "USD" }"""

        mvc.perform(put("/exchange")
            .contentType("application/vnd.exchange-command.v1+json")
            .content(request))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(header().string("Content-Type", "application/vnd.constraint-violation.v1+json"))
            .andExpect(jsonPath("$.subject", `is`("transactionId")))
            .andExpect(jsonPath("$.violation", `is`("IS_BLANK")))
    }

    @Test
    fun `should not exchange value due to empty pesel`() {
        val request = """{ "transactionId": "4444", "amount": 50.00, "currency": "PLN", "targetCurrency": "USD" }"""

        mvc.perform(put("/exchange")
            .contentType("application/vnd.exchange-command.v1+json")
            .content(request))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(header().string("Content-Type", "application/vnd.constraint-violation.v1+json"))
            .andExpect(jsonPath("$.subject", `is`("pesel")))
            .andExpect(jsonPath("$.violation", `is`("IS_BLANK")))
    }

    @Test
    fun `should not exchange value due to unregistered pesel`() {
        val request = """{ "transactionId": "5555", "pesel": "000", "amount": 50.00, "currency": "PLN", "targetCurrency": "USD" }"""

        mvc.perform(put("/exchange")
            .contentType("application/vnd.exchange-command.v1+json")
            .content(request))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(header().string("Content-Type", "application/vnd.constraint-violation.v1+json"))
            .andExpect(jsonPath("$.subject", `is`("pesel")))
            .andExpect(jsonPath("$.violation", `is`("IS_NOT_REGISTERED")))
    }

    @Test
    fun `should not exchange value due to empty amount`() {
        val request = """{ "transactionId": "6666", "pesel": "87010438175", "currency": "PLN", "targetCurrency": "USD" }"""

        mvc.perform(put("/exchange")
            .contentType("application/vnd.exchange-command.v1+json")
            .content(request))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(header().string("Content-Type", "application/vnd.constraint-violation.v1+json"))
            .andExpect(jsonPath("$.subject", `is`("amount")))
            .andExpect(jsonPath("$.violation", `is`("IS_BLANK")))
    }

    @Test
    fun `should not exchange value due to negative amount`() {
        val request = """{ "transactionId": "7777", "pesel": "87010438175", "amount": -50.00, "currency": "PLN", "targetCurrency": "USD" }"""

        mvc.perform(put("/exchange")
            .contentType("application/vnd.exchange-command.v1+json")
            .content(request))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(header().string("Content-Type", "application/vnd.constraint-violation.v1+json"))
            .andExpect(jsonPath("$.subject", `is`("amount")))
            .andExpect(jsonPath("$.violation", `is`("IS_NEGATIVE")))
    }

    @Test
    fun `should not exchange value due to empty currency`() {
        val request = """{ "transactionId": "8888", "pesel": "87010438175", "amount": 50.00, "targetCurrency": "USD" }"""

        mvc.perform(put("/exchange")
            .contentType("application/vnd.exchange-command.v1+json")
            .content(request))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(header().string("Content-Type", "application/vnd.constraint-violation.v1+json"))
            .andExpect(jsonPath("$.subject", `is`("currency")))
            .andExpect(jsonPath("$.violation", `is`("IS_BLANK")))
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "HUF",
        "CHF",
        "XYZ",
        "GBP",
        "AUD",
        "EUR"
    ])
    fun `should not exchange value due to unsupported currency`(currency: String) {
        val request = """{ "transactionId": "9999", "pesel": "87010438175", "amount": 50.00, "currency": "$currency" }"""

        mvc.perform(put("/exchange")
            .contentType("application/vnd.exchange-command.v1+json")
            .content(request))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(header().string("Content-Type", "application/vnd.constraint-violation.v1+json"))
            .andExpect(jsonPath("$.subject", `is`("currency")))
            .andExpect(jsonPath("$.violation", `is`("IS_UNSUPPORTED")))
    }

    @Test
    fun `should not exchange value due to empty target currency`() {
        val request = """{ "transactionId": "8888", "pesel": "87010438175", "amount": 50.00, "currency": "PLN" }"""

        mvc.perform(put("/exchange")
            .contentType("application/vnd.exchange-command.v1+json")
            .content(request))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(header().string("Content-Type", "application/vnd.constraint-violation.v1+json"))
            .andExpect(jsonPath("$.subject", `is`("targetCurrency")))
            .andExpect(jsonPath("$.violation", `is`("IS_BLANK")))
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "HUF",
        "CHF",
        "XYZ",
        "GBP",
        "AUD",
        "EUR"
    ])
    fun `should not exchange value due to unsupported target currency`(targetCurrency: String) {
        val request = """{ "transactionId": "8888", "pesel": "87010438175", "amount": 50.00, "currency": "PLN", "targetCurrency": "$targetCurrency" }"""

        mvc.perform(put("/exchange")
            .contentType("application/vnd.exchange-command.v1+json")
            .content(request))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(header().string("Content-Type", "application/vnd.constraint-violation.v1+json"))
            .andExpect(jsonPath("$.subject", `is`("targetCurrency")))
            .andExpect(jsonPath("$.violation", `is`("IS_UNSUPPORTED")))
    }

    fun `should not exchange value due to not enough money on account`() {
        // parameterized for PLN and USD
    }

    private fun mockUSDExchangeRate(purchase: Double, sell: Double) {
        stubFor(get(urlMatching("/api/exchangerates/rates/c/USD"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(
                    """
                        {
                          "rates": [
                            {
                              "bid": $purchase,
                              "ask": $sell
                            }
                          ]
                        }
                    """.trimIndent()
                )))
    }

    private fun createUserAccount(pesel: String, startingBalanceInPLN: String) {
        val request = """{ "firstName": "John", "lastName": "Doe", "pesel": "$pesel", "startingBalanceInPLN": "$startingBalanceInPLN" }"""

        mvc.perform(
            MockMvcRequestBuilders.post("/accounts")
            .contentType("application/vnd.new-account.v1+json")
            .content(request))
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", Matchers.matchesRegex("\\/accounts\\/[0-9]+")))
    }
}
