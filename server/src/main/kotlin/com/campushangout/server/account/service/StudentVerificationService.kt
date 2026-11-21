package com.campushangout.server.account.service

import com.campushangout.server.account.dto.StudentVerificationResponse
import com.campushangout.server.account.entity.StudentVerification
import com.campushangout.server.account.entity.StudentVerificationStatus
import com.campushangout.server.account.exception.InvalidStudentIdImageException
import com.campushangout.server.account.exception.StudentVerificationAlreadySubmittedException
import com.campushangout.server.account.exception.StudentVerificationUserNotFoundException
import com.campushangout.server.account.repository.StudentVerificationRepository
import com.campushangout.server.account.repository.UserRepository
import com.campushangout.server.storage.FileStorage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
class StudentVerificationService(
    private val userRepository: UserRepository,
    private val verificationRepository: StudentVerificationRepository,
    private val fileStorage: FileStorage,
    @Value("\${student-verification.auto-approve:false}")
    private val autoApprove: Boolean
) {

    @Transactional
    fun submit(
        userId: Long,
        file: MultipartFile
    ): StudentVerificationResponse {

        val user = userRepository.findById(userId)
            .orElseThrow {
                StudentVerificationUserNotFoundException()
            }

        if (verificationRepository
                .findTopByUserUserIdOrderBySubmittedAtDescVerificationIdDesc(
                    userId
                ) != null
        ) {
            throw StudentVerificationAlreadySubmittedException()
        }

        if (file.isEmpty) {
            throw InvalidStudentIdImageException()
        }

        val imageKey = fileStorage.save(file)

        val status = if (autoApprove) {
            StudentVerificationStatus.APPROVED
        } else {
            StudentVerificationStatus.PENDING
        }

        val verification = StudentVerification(
            user = user,
            imageUrl = imageKey,
            status = status,
            reviewedAt = if (autoApprove) LocalDateTime.now() else null
        )

        verificationRepository.save(verification)

        if (autoApprove) {
            user.verified = true
        }

        return StudentVerificationResponse(status)
    }

    @Transactional(readOnly = true)
    fun getMyStatus(userId: Long): StudentVerificationResponse {
        val verification = verificationRepository
            .findTopByUserUserIdOrderBySubmittedAtDescVerificationIdDesc(
                userId
            )

        return if (verification == null) {
            StudentVerificationResponse(
                StudentVerificationStatus.NOT_SUBMITTED
            )
        } else {
            StudentVerificationResponse(
                status = verification.status,
                rejectionReason = verification.rejectionReason
            )
        }
    }
}
