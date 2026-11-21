package com.campushangout.server.account

import com.campushangout.server.account.config.JwtTokenProvider
import com.campushangout.server.account.dto.SignupRequest
import com.campushangout.server.account.service.SignupService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Bean
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class JwtAuthenticationIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var jwtTokenProvider: JwtTokenProvider

    @Autowired
    lateinit var signupService: SignupService

    @Test
    fun `유효한 JWT로 보호된 API에 접근할 수 있다`() {
        val userId = signupService.signup(
            SignupRequest(
                email = "jwt-test@cau.ac.kr",
                password = "example1234"
            )
        )

        val token = jwtTokenProvider.generateToken(userId)

        mockMvc.get("/api/test/auth") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            content { string(userId.toString()) }
        }
    }

    @Test
    fun `JWT 없이 보호된 API에 접근하면 401을 반환한다`() {
        mockMvc.get("/api/test/auth")
            .andExpect {
                status { isUnauthorized() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("UNAUTHORIZED") }
                jsonPath("$.error.message") {
                    value("로그인이 필요합니다.")
                }
            }
    }

    @Test
    fun `잘못된 JWT로 보호된 API에 접근하면 401을 반환한다`() {
        mockMvc.get("/api/test/auth") {
            header("Authorization", "Bearer invalid-token")
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("UNAUTHORIZED") }
        }
    }

    @Test
    fun `존재하지 않는 회원의 JWT는 인증되지 않는다`() {
        val token = jwtTokenProvider.generateToken(Long.MAX_VALUE)

        mockMvc.get("/api/test/auth") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("UNAUTHORIZED") }
        }
    }

    @TestConfiguration
    class TestControllerConfiguration {

        @Bean
        fun jwtTestController(): JwtTestController {
            return JwtTestController()
        }
    }

    @RestController
    class JwtTestController {

        @GetMapping("/api/test/auth")
        fun authenticatedUserId(): String {
            val authentication =
                SecurityContextHolder.getContext().authentication

            return authentication?.principal?.toString()
                ?: throw IllegalStateException("인증 정보가 없습니다.")
        }
    }
}
