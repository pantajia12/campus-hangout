package com.campushangout.server.account

import com.campushangout.server.account.dto.SignupRequest
import com.campushangout.server.account.exception.DuplicateEmailException
import com.campushangout.server.account.repository.UserRepository
import com.campushangout.server.account.service.SignupService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class SignupIntegrationTest {

    @Autowired
    lateinit var signupService: SignupService

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Test
    fun `회원가입하면 회원 정보가 DB에 저장된다`() {
        val request = SignupRequest(
            email = "student@cau.ac.kr",
            password = "example1234"
        )

        val userId = signupService.signup(request)
        val savedUser = userRepository.findById(userId).orElseThrow()

        assertEquals("student@cau.ac.kr", savedUser.email)
        assertFalse(savedUser.verified)
        assertNotEquals("example1234", savedUser.password)

        assertTrue(
            passwordEncoder.matches("example1234", savedUser.password)
        )
    }

    @Test
    fun `이미 가입된 이메일로 다시 가입할 수 없다`() {
        val request = SignupRequest(
            email = "duplicate@cau.ac.kr",
            password = "example1234"
        )

        signupService.signup(request)

        val exception = assertThrows(
            DuplicateEmailException::class.java
        ) {
            signupService.signup(request)
        }

        assertEquals(
            "이미 가입된 이메일입니다.",
            exception.message
        )
    }
}
