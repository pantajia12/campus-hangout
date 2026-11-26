package com.campushangout.server.profile.repository

import com.campushangout.server.profile.entity.UserInterest
import com.campushangout.server.profile.entity.UserInterestId
import org.springframework.data.jpa.repository.JpaRepository

interface UserInterestRepository : JpaRepository<UserInterest, UserInterestId> {

    fun findAllByIdUserId(userId: Long): List<UserInterest>

    fun deleteAllByIdUserId(userId: Long)
}
