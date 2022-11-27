package org.kkrasowski.exchange.application.resources

import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
class UserAccountDetailsResourceTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    fun `should return the user account details`() {
        createAccount("""{ "firstName": "John", "lastName": "Doe", "pesel": "91040188315", "startingBalanceInPLN": "200" }""")
        exchange("""{ "transactionId": "9898", "pesel": "91040188315", "amount": "45.59", "currency": "PLN", "targetCurrency": "USD" }""")

        mvc.perform(get("/accounts/91040188315")
            .accept("application/vnd.account-details.v1+json"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.firstName", `is`("John")))
            .andExpect(jsonPath("$.lastName", `is`("Doe")))
            .andExpect(jsonPath("$.accounts[0].balance.amount", `is`(154.41)))
            .andExpect(jsonPath("$.accounts[0].balance.currency", `is`("PLN")))
            .andExpect(jsonPath("$.accounts[1].balance.amount", `is`(10.01)))
            .andExpect(jsonPath("$.accounts[1].balance.currency", `is`("USD")))
    }

    @Test
    fun `should return 404`() {
        mvc.perform(get("/accounts/000")
            .accept("application/vnd.account-details.v1+json"))
            .andExpect(status().isNotFound)
    }

    private fun createAccount(request: String) {
        mvc.perform(post("/accounts")
            .contentType("application/vnd.new-account.v1+json")
            .content(request))
            .andExpect(status().isCreated)
    }

    private fun exchange(request: String) {
        mvc.perform(put("/exchange")
            .contentType("application/vnd.exchange-command.v1+json")
            .content(request))
            .andExpect(status().isCreated)
    }
}
