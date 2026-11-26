package com.campushangout.server.account.repository

import com.campushangout.server.account.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {

    fun findByEmail(email: String): User?

    fun existsByEmail(email: String): Boolean
}
