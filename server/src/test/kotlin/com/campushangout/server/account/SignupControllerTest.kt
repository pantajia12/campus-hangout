package com.campushangout.server.account

import com.campushangout.server.account.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SignupControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userRepository: UserRepository

    @Test
    fun `회원가입 API 요청이 성공한다`() {
        val requestJson = """
            {
                "email": "api-test@cau.ac.kr",
                "password": "example1234"
            }
        """.trimIndent()

        mockMvc.post("/api/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
        }.andExpect {
            status { isCreated() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.userId") { exists() }
        }

        val savedUser = userRepository.findByEmail("api-test@cau.ac.kr")

        assertNotNull(savedUser)
        assertFalse(savedUser!!.verified)
    }

    @Test
    fun `잘못된 이메일로 회원가입하면 400을 반환한다`() {
        val requestJson = """
            {
                "email": "invalid-email",
                "password": "example1234"
            }
        """.trimIndent()

        mockMvc.post("/api/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("INVALID_INPUT") }
        }
    }

    @Test
    fun `비밀번호가 8자 미만이면 400을 반환한다`() {
        val requestJson = """
            {
                "email": "student@cau.ac.kr",
                "password": "1234"
            }
        """.trimIndent()

        mockMvc.post("/api/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("INVALID_INPUT") }
        }
    }

    @Test
    fun `중복 이메일로 회원가입하면 409를 반환한다`() {
        val requestJson = """
            {
                "email": "duplicate-api@cau.ac.kr",
                "password": "example1234"
            }
        """.trimIndent()

        mockMvc.post("/api/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
        }.andExpect {
            status { isCreated() }
        }

        mockMvc.post("/api/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
        }.andExpect {
            status { isConflict() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("CONFLICT") }
            jsonPath("$.error.message") {
                value("이미 가입된 이메일입니다.")
            }
        }
    }
}
