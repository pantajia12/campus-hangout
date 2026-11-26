
package com.campushangout.server.account

import com.campushangout.server.account.config.JwtTokenProvider
import com.campushangout.server.account.dto.SignupRequest
import com.campushangout.server.account.service.SignupService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoginControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var signupService: SignupService

    @Autowired
    lateinit var jwtTokenProvider: JwtTokenProvider

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `올바른 로그인 요청에 JWT를 반환한다`() {
        val userId = signupService.signup(
            SignupRequest(
                email = "login-api@cau.ac.kr",
                password = "example1234"
            )
        )

        val requestJson = """
            {
                "email": "login-api@cau.ac.kr",
                "password": "example1234"
            }
        """.trimIndent()

        val result = mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.accessToken") { exists() }
        }.andReturn()

        val responseJson = result.response.contentAsString

        val accessToken = objectMapper.readTree(responseJson)
            .get("data")
            .get("accessToken")
            .asText()

        val extractedUserId =
            jwtTokenProvider.validateAndGetUserId(accessToken)

        assertNotNull(extractedUserId)
        assertEquals(userId, extractedUserId)
    }

    @Test
    fun `잘못된 이메일 형식으로 로그인하면 400을 반환한다`() {
        val requestJson = """
            {
                "email": "invalid-email",
                "password": "example1234"
            }
        """.trimIndent()

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("INVALID_INPUT") }
        }
    }

    @Test
    fun `비밀번호가 틀리면 401을 반환한다`() {
        signupService.signup(
            SignupRequest(
                email = "wrong-password-api@cau.ac.kr",
                password = "example1234"
            )
        )

        val requestJson = """
            {
                "email": "wrong-password-api@cau.ac.kr",
                "password": "wrong1234"
            }
        """.trimIndent()

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("UNAUTHORIZED") }
            jsonPath("$.error.message") {
                value("이메일 또는 비밀번호가 올바르지 않습니다.")
            }
        }
    }

    @Test
    fun `존재하지 않는 이메일로 로그인하면 401을 반환한다`() {
        val requestJson = """
            {
                "email": "not-found-api@cau.ac.kr",
                "password": "example1234"
            }
        """.trimIndent()

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("UNAUTHORIZED") }
            jsonPath("$.error.message") {
                value("이메일 또는 비밀번호가 올바르지 않습니다.")
            }
        }
    }
}
