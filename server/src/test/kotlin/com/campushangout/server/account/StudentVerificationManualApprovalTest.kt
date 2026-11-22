package com.campushangout.server.account

import com.campushangout.server.account.entity.StudentVerificationStatus
import com.campushangout.server.account.entity.User
import com.campushangout.server.account.repository.StudentVerificationRepository
import com.campushangout.server.account.repository.UserRepository
import com.campushangout.server.account.service.StudentVerificationService
import com.campushangout.server.profile.entity.UserProfile
import com.campushangout.server.profile.repository.UserProfileRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(
    properties = ["student-verification.auto-approve=false"]
)
@Transactional
class StudentVerificationManualApprovalTest {

    @Autowired
    lateinit var service: StudentVerificationService

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var userProfileRepository: UserProfileRepository

    @Autowired
    lateinit var verificationRepository: StudentVerificationRepository

    @Test
    fun `submission stays pending when auto approval is disabled`() {
        val user = userRepository.save(
            User(
                email = "pending-${UUID.randomUUID()}@test.com",
                password = "encoded-password"
            )
        )

        userProfileRepository.save(
            UserProfile(
                userId = user.userId!!,
                name = "Alex",
                nationality = "Korea",
                major = "Computer Science",
                bio = null,
                color = 1.toShort()
            )
        )

        val file = MockMultipartFile(
            "file",
            "student-id.png",
            "image/png",
            byteArrayOf(
                0x89.toByte(), 0x50, 0x4E, 0x47,
                0x0D, 0x0A, 0x1A, 0x0A
            )
        )

        val result = service.submit(user.userId!!, file)

        assertEquals(StudentVerificationStatus.PENDING, result.status)

        val saved = verificationRepository
            .findTopByUserUserIdOrderBySubmittedAtDescVerificationIdDesc(
                user.userId!!
            )

        assertNotNull(saved)
        assertEquals(StudentVerificationStatus.PENDING, saved!!.status)
        assertNull(saved.reviewedAt)
        assertFalse(user.verified)
    }
}
