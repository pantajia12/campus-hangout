package com.campushangout.server.profile

import com.campushangout.server.account.config.JwtTokenProvider
import com.campushangout.server.account.entity.User
import com.campushangout.server.account.repository.UserRepository
import com.campushangout.server.profile.entity.Interest
import com.campushangout.server.profile.entity.Language
import com.campushangout.server.profile.repository.InterestRepository
import com.campushangout.server.profile.repository.LanguageRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProfileControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var jwtTokenProvider: JwtTokenProvider

    @Autowired
    lateinit var languageRepository: LanguageRepository

    @Autowired
    lateinit var interestRepository: InterestRepository

    private fun createToken(): String {
        val user = userRepository.save(
            User(
                email = "profile-${UUID.randomUUID()}@test.com",
                password = "encoded-password"
            )
        )
        return jwtTokenProvider.generateToken(user.userId!!)
    }

    private fun createLanguage(): Long =
        languageRepository.save(
            Language(languageName = "Test-${UUID.randomUUID()}")
        ).languageId!!

    private fun createInterest(): Long =
        interestRepository.save(
            Interest(interestName = "Test-${UUID.randomUUID()}")
        ).interestId!!

    private fun profileJson(
        languageIds: List<Long> = emptyList(),
        interestIds: List<Long> = emptyList(),
        color: Int = 1
    ): String = """
        {
          "name": "Alex",
          "nationality": "Korea",
          "major": "Computer Science",
          "bio": "Hello!",
          "color": $color,
          "languageIds": $languageIds,
          "interestIds": $interestIds
        }
    """.trimIndent()

    @Test
    fun `profile is null before registration`() {
        val token = createToken()

        mockMvc.get("/api/users/me") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.profile") { doesNotExist() }
        }
    }

    @Test
    fun `profile registration succeeds`() {
        val token = createToken()
        val languageId = createLanguage()
        val interestId = createInterest()

        mockMvc.put("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = profileJson(
                languageIds = listOf(languageId),
                interestIds = listOf(interestId)
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.profile.name") { value("Alex") }
            jsonPath("$.data.profile.color") { value(1) }
            jsonPath("$.data.profile.languageIds[0]") { value(languageId) }
            jsonPath("$.data.profile.interestIds[0]") { value(interestId) }
        }
    }

    @Test
    fun `profile update replaces existing values`() {
        val token = createToken()
        val oldLanguageId = createLanguage()
        val newLanguageId = createLanguage()

        mockMvc.put("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = profileJson(languageIds = listOf(oldLanguageId))
        }.andExpect {
            status { isOk() }
        }

        mockMvc.put("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "name": "Jamie",
                  "nationality": "Japan",
                  "major": "Design",
                  "bio": "Updated",
                  "color": 3,
                  "languageIds": [$newLanguageId],
                  "interestIds": []
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.profile.name") { value("Jamie") }
            jsonPath("$.data.profile.color") { value(3) }
            jsonPath("$.data.profile.languageIds[0]") { value(newLanguageId) }
            jsonPath("$.data.profile.languageIds.length()") { value(1) }
        }

        mockMvc.get("/api/users/me") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.profile.name") { value("Jamie") }
        }
    }

    @Test
    fun `invalid color returns 400`() {
        val token = createToken()

        mockMvc.put("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = profileJson(color = 5)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error.code") { value("INVALID_INPUT") }
        }
    }

    @Test
    fun `unknown language returns 400`() {
        val token = createToken()

        mockMvc.put("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = profileJson(languageIds = listOf(Long.MAX_VALUE))
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error.code") { value("INVALID_INPUT") }
        }
    }

    @Test
    fun `unknown interest returns 400`() {
        val token = createToken()

        mockMvc.put("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = profileJson(interestIds = listOf(Long.MAX_VALUE))
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error.code") { value("INVALID_INPUT") }
        }
    }

    @Test
    fun `duplicate selections are stored once`() {
        val token = createToken()
        val languageId = createLanguage()
        val interestId = createInterest()

        mockMvc.put("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = profileJson(
                languageIds = listOf(languageId, languageId),
                interestIds = listOf(interestId, interestId)
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.profile.languageIds.length()") { value(1) }
            jsonPath("$.data.profile.interestIds.length()") { value(1) }
        }
    }

    @Test
    fun `profile lookup without JWT returns 401`() {
        mockMvc.get("/api/users/me")
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    fun `profile update without JWT returns 401`() {
        mockMvc.put("/api/users/me/profile") {
            contentType = MediaType.APPLICATION_JSON
            content = profileJson()
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `languages endpoint is public`() {
        val languageId = createLanguage()

        mockMvc.get("/api/languages")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data[?(@.id == $languageId)]") { exists() }
            }
    }

    @Test
    fun `interests endpoint is public`() {
        val interestId = createInterest()

        mockMvc.get("/api/interests")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data[?(@.id == $interestId)]") { exists() }
            }
    }
}
