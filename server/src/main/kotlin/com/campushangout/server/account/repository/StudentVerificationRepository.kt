package com.campushangout.server.account.repository

import com.campushangout.server.account.entity.StudentVerification
import org.springframework.data.jpa.repository.JpaRepository

interface StudentVerificationRepository :
    JpaRepository<StudentVerification, Long> {

    fun findTopByUserUserIdOrderBySubmittedAtDescVerificationIdDesc(
        userId: Long
    ): StudentVerification?
}
