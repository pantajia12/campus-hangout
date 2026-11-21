package com.campushangout.server.account

import com.campushangout.server.account.config.JwtTokenProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
class JwtTokenProviderTest {

    @Autowired
    lateinit var jwtTokenProvider: JwtTokenProvider

    @Test
    fun `JWT를 생성하고 회원 ID를 추출할 수 있다`() {
        val userId = 1L

        val token = jwtTokenProvider.generateToken(userId)
        val extractedUserId = jwtTokenProvider.validateAndGetUserId(token)

        assertEquals(userId, extractedUserId)
    }

    @Test
    fun `변조된 JWT는 검증에 실패한다`() {
        val token = jwtTokenProvider.generateToken(1L)

        val parts = token.split(".")
        val signature = parts[2]
        val changedFirstCharacter = if (signature[0] == 'A') 'B' else 'A'
        val tamperedSignature = changedFirstCharacter + signature.substring(1)
        val tamperedToken = "${parts[0]}.${parts[1]}.$tamperedSignature"

        assertNull(jwtTokenProvider.validateAndGetUserId(tamperedToken))
    }

    @Test
    fun `잘못된 JWT는 검증에 실패한다`() {
        assertNull(jwtTokenProvider.validateAndGetUserId("invalid.token.value"))
    }
}
