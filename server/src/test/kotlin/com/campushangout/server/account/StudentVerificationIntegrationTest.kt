package com.campushangout.server.account

import com.campushangout.server.account.entity.StudentVerificationStatus
import com.campushangout.server.account.entity.User
import com.campushangout.server.account.exception.StudentVerificationAlreadySubmittedException
import com.campushangout.server.account.repository.StudentVerificationRepository
import com.campushangout.server.account.repository.UserRepository
import com.campushangout.server.account.service.StudentVerificationService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StudentVerificationIntegrationTest {

    @Autowired
    lateinit var service: StudentVerificationService

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var verificationRepository: StudentVerificationRepository

    private fun createUser(): User =
        userRepository.save(
            User(
                email = "student-${java.util.UUID.randomUUID()}@test.com",
                password = "encoded-password"
            )
        )

    private fun createImage() = MockMultipartFile(
        "file",
        "student-id.png",
        "image/png",
        byteArrayOf(1, 2, 3, 4)
    )

    @Test
    fun `student ID submission is automatically approved`() {
        val user = createUser()

        val result = service.submit(user.userId!!, createImage())

        assertEquals(StudentVerificationStatus.APPROVED, result.status)

        val saved = verificationRepository
            .findTopByUserUserIdOrderBySubmittedAtDescVerificationIdDesc(
                user.userId!!
            )

        assertNotNull(saved)
        assertEquals(StudentVerificationStatus.APPROVED, saved!!.status)
        assertNotNull(saved.reviewedAt)

        userRepository.flush()
        assertTrue(userRepository.findById(user.userId!!).get().verified)
    }

    @Test
    fun `user without submission has NOT_SUBMITTED status`() {
        val user = createUser()

        val result = service.getMyStatus(user.userId!!)

        assertEquals(StudentVerificationStatus.NOT_SUBMITTED, result.status)
        assertFalse(user.verified)
    }

    @Test
    fun `duplicate student ID submission is rejected`() {
        val user = createUser()

        service.submit(user.userId!!, createImage())

        assertThrows(StudentVerificationAlreadySubmittedException::class.java) {
            service.submit(user.userId!!, createImage())
        }
    }

    @Test
    fun `student verification status does not expose image path`() {
        val user = createUser()

        service.submit(user.userId!!, createImage())

        val result = service.getMyStatus(user.userId!!)

        assertEquals(StudentVerificationStatus.APPROVED, result.status)
        assertNull(result.rejectionReason)
    }
}
