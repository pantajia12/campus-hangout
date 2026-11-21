package com.campushangout.server.account

import com.campushangout.server.account.config.JwtTokenProvider
import com.campushangout.server.account.dto.LoginRequest
import com.campushangout.server.account.dto.SignupRequest
import com.campushangout.server.account.exception.InvalidCredentialsException
import com.campushangout.server.account.service.LoginService
import com.campushangout.server.account.service.SignupService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class LoginIntegrationTest {

    @Autowired
    lateinit var signupService: SignupService

    @Autowired
    lateinit var loginService: LoginService

    @Autowired
    lateinit var jwtTokenProvider: JwtTokenProvider

    @Test
    fun `올바른 이메일과 비밀번호로 로그인하면 JWT가 발급된다`() {
        val userId = signupService.signup(
            SignupRequest(
                email = "login-test@cau.ac.kr",
                password = "example1234"
            )
        )

        val token = loginService.login(
            LoginRequest(
                email = "login-test@cau.ac.kr",
                password = "example1234"
            )
        )

        val extractedUserId = jwtTokenProvider.validateAndGetUserId(token)

        assertEquals(userId, extractedUserId)
    }

    @Test
    fun `비밀번호가 틀리면 로그인할 수 없다`() {
        signupService.signup(
            SignupRequest(
                email = "wrong-password@cau.ac.kr",
                password = "example1234"
            )
        )

        val exception = assertThrows(
            InvalidCredentialsException::class.java
        ) {
            loginService.login(
                LoginRequest(
                    email = "wrong-password@cau.ac.kr",
                    password = "wrong1234"
                )
            )
        }

        assertEquals(
            "이메일 또는 비밀번호가 올바르지 않습니다.",
            exception.message
        )
    }

    @Test
    fun `존재하지 않는 이메일로 로그인할 수 없다`() {
        val exception = assertThrows(
            InvalidCredentialsException::class.java
        ) {
            loginService.login(
                LoginRequest(
                    email = "not-found@cau.ac.kr",
                    password = "example1234"
                )
            )
        }

        assertEquals(
            "이메일 또는 비밀번호가 올바르지 않습니다.",
            exception.message
        )
    }
}
