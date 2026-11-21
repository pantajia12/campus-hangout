
package com.campushangout.server.account

import com.campushangout.server.account.config.JwtTokenProvider
import com.campushangout.server.account.entity.User
import com.campushangout.server.account.repository.UserRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StudentVerificationControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var jwtTokenProvider: JwtTokenProvider

    private fun createToken(): String {
        val user = userRepository.save(
            User(
                email = "controller-${UUID.randomUUID()}@test.com",
                password = "encoded-password"
            )
        )

        return jwtTokenProvider.generateToken(user.userId!!)
    }

    // PNG 파일 시그니처 검사에 통과하는 테스트 데이터
    private fun createImage() = MockMultipartFile(
        "file",
        "student-id.png",
        MediaType.IMAGE_PNG_VALUE,
        byteArrayOf(
            0x89.toByte(), 0x50, 0x4E, 0x47,
            0x0D, 0x0A, 0x1A, 0x0A
        )
    )

    @Test
    fun `student ID submission returns 201 and APPROVED`() {
        val token = createToken()

        mockMvc.multipart("/api/student-verifications") {
            file(createImage())
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isCreated() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.status") { value("APPROVED") }
        }
    }

    @Test
    fun `status lookup without submission returns NOT_SUBMITTED`() {
        val token = createToken()

        mockMvc.get("/api/student-verifications/me") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.status") { value("NOT_SUBMITTED") }
        }
    }

    @Test
    fun `student ID submission without JWT returns 401`() {
        mockMvc.multipart("/api/student-verifications") {
            file(createImage())
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `status response does not contain image URL`() {
        val token = createToken()

        mockMvc.multipart("/api/student-verifications") {
            file(createImage())
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isCreated() }
        }

        mockMvc.get("/api/student-verifications/me") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.imageUrl") { doesNotExist() }
        }
    }

    @Test
    fun `duplicate submission returns 409`() {
        val token = createToken()

        mockMvc.multipart("/api/student-verifications") {
            file(createImage())
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isCreated() }
        }

        mockMvc.multipart("/api/student-verifications") {
            file(createImage())
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isConflict() }
            jsonPath("$.error.code") { value("CONFLICT") }
        }
    }

    @Test
    fun `unsupported image type returns 400`() {
        val token = createToken()

        val invalidFile = MockMultipartFile(
            "file",
            "student-id.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "invalid".toByteArray()
        )

        mockMvc.multipart("/api/student-verifications") {
            file(invalidFile)
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `rejects fake PNG file`() {
        val token = createToken()

        val fakeFile = MockMultipartFile(
            "file",
            "fake.png",
            MediaType.IMAGE_PNG_VALUE,
            "This is not a real PNG".toByteArray()
        )

        mockMvc.multipart("/api/student-verifications") {
            file(fakeFile)
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isBadRequest() }
        }
    }
}
