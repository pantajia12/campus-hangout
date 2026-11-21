package com.campushangout.server.account

import com.campushangout.server.account.entity.StudentVerificationStatus
import com.campushangout.server.account.entity.User
import com.campushangout.server.account.repository.StudentVerificationRepository
import com.campushangout.server.account.repository.UserRepository
import com.campushangout.server.account.service.StudentVerificationService
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
@TestPropertySource(properties = ["student-verification.auto-approve=false"])
@Transactional
class StudentVerificationManualApprovalTest {

    @Autowired
    lateinit var service: StudentVerificationService

    @Autowired
    lateinit var userRepository: UserRepository

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

        val file = MockMultipartFile(
            "file",
            "student-id.png",
            "image/png",
            byteArrayOf(1, 2, 3, 4)
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
