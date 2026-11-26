package com.campushangout.server.account.config

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}")
    secret: String
) {
    private val key = Keys.hmacShaKeyFor(
        secret.toByteArray(StandardCharsets.UTF_8)
    )

    // Access Token 유효기간: 24시간
    private val expirationMs = 24 * 60 * 60 * 1000L

    // JWT 생성
    fun generateToken(userId: Long): String {
        val now = Date()
        val expiration = Date(now.time + expirationMs)

        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(expiration)
            .signWith(key)
            .compact()
    }

    // JWT 검증 후 회원 ID 반환
    fun validateAndGetUserId(token: String): Long? {
        return try {
            val claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload

            claims.subject.toLongOrNull()
        } catch (e: io.jsonwebtoken.JwtException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
