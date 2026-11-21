package com.campushangout.server.account.service

import com.campushangout.server.account.dto.SignupRequest
import com.campushangout.server.account.entity.User
import com.campushangout.server.account.exception.DuplicateEmailException
import com.campushangout.server.account.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SignupService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    fun signup(request: SignupRequest): Long {

        if (userRepository.existsByEmail(request.email)) {
            throw DuplicateEmailException()
        }

        val encodedPassword = passwordEncoder.encode(request.password)
            ?: throw IllegalStateException("비밀번호 암호화에 실패했습니다.")

        val user = User(
            email = request.email,
            password = encodedPassword,
            verified = false
        )

        val savedUser = userRepository.save(user)

        return savedUser.userId
            ?: throw IllegalStateException("회원 ID 생성에 실패했습니다.")
    }
}
