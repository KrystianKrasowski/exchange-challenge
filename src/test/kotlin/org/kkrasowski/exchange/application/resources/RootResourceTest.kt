package org.kkrasowski.exchange.application.resources

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
class RootResourceTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    fun `should return hello message`() {
        mvc.perform(get("/"))
            .andExpect(status().isOk)
    }
}

