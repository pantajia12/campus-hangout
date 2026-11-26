package com.campushangout.server.profile.repository

import com.campushangout.server.profile.entity.UserLanguage
import com.campushangout.server.profile.entity.UserLanguageId
import org.springframework.data.jpa.repository.JpaRepository

interface UserLanguageRepository : JpaRepository<UserLanguage, UserLanguageId> {

    fun findAllByIdUserId(userId: Long): List<UserLanguage>

    fun deleteAllByIdUserId(userId: Long)
}
