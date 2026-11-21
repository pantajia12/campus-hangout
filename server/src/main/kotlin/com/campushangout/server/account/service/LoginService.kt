package com.campushangout.server.account.service

import com.campushangout.server.account.config.JwtTokenProvider
import com.campushangout.server.account.dto.LoginRequest
import com.campushangout.server.account.exception.InvalidCredentialsException
import com.campushangout.server.account.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LoginService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): String {

        val user = userRepository.findByEmail(request.email)
            ?: throw InvalidCredentialsException()

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw InvalidCredentialsException()
        }

        val userId = user.userId
            ?: throw IllegalStateException("회원 ID를 확인할 수 없습니다.")

        return jwtTokenProvider.generateToken(userId)
    }
}
