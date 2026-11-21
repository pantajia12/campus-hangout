package com.campushangout.server.account.dto

import com.campushangout.server.account.entity.StudentVerificationStatus

data class StudentVerificationResponse(
    val status: StudentVerificationStatus,
    val rejectionReason: String? = null
)
