package com.campushangout.server.common.ping

import com.campushangout.server.common.error.GlobalExceptionHandler
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class PingControllerTest {

    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(PingController())
        .setControllerAdvice(GlobalExceptionHandler())
        .build()

    @Test
    fun `ping은 공통 응답 형식으로 ok를 돌려준다`() {
        mockMvc.get("/api/v1/ping").andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.status") { value("ok") }
            jsonPath("$.data.serverTime") { exists() }
            jsonPath("$.error") { doesNotExist() }
        }
    }

    @Test
    fun `지원하지 않는 메서드는 405와 METHOD_NOT_ALLOWED를 돌려준다`() {
        mockMvc.post("/api/v1/ping").andExpect {
            status { isMethodNotAllowed() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("METHOD_NOT_ALLOWED") }
        }
    }
}
