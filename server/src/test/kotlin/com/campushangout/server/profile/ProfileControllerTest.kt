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
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.patch
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

    private fun createToken(verified: Boolean = false): String {
        val user = userRepository.save(
            User(
                email = "profile-${UUID.randomUUID()}@test.com",
                password = "encoded-password",
                verified = verified
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

    private fun createProfileJson(
        languageIds: List<Long>
    ): String = """
        {
          "name": "Alex",
          "nationality": "Korea",
          "major": "Computer Science",
          "languageIds": $languageIds
        }
    """.trimIndent()

    private fun registerProfile(
        token: String,
        languageId: Long
    ) {
        mockMvc.post("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = createProfileJson(listOf(languageId))
        }.andExpect {
            status { isCreated() }
        }
    }

    @Test
    fun `profile is null before registration`() {
        val token = createToken()

        mockMvc.get("/api/users/me") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.verified") { value(false) }
            jsonPath("$.data.profile") { doesNotExist() }
        }
    }

    @Test
    fun `initial profile registration succeeds`() {
        val token = createToken()
        val languageId = createLanguage()

        mockMvc.post("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = createProfileJson(listOf(languageId))
        }.andExpect {
            status { isCreated() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.profile.name") { value("Alex") }
            jsonPath("$.data.profile.nationality") { value("Korea") }
            jsonPath("$.data.profile.major") { value("Computer Science") }
            jsonPath("$.data.profile.color") { value(1) }
            jsonPath("$.data.profile.languageIds[0]") { value(languageId) }
            jsonPath("$.data.profile.interestIds.length()") { value(0) }
            jsonPath("$.data.profile.bio") { doesNotExist() }
        }
    }

    @Test
    fun `duplicate profile registration returns 409`() {
        val token = createToken()
        val languageId = createLanguage()

        registerProfile(token, languageId)

        mockMvc.post("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = createProfileJson(listOf(languageId))
        }.andExpect {
            status { isConflict() }
            jsonPath("$.error.code") { value("CONFLICT") }
        }
    }

    @Test
    fun `missing name returns 400`() {
        val token = createToken()
        val languageId = createLanguage()

        mockMvc.post("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "nationality": "Korea",
                  "major": "Computer Science",
                  "languageIds": [$languageId]
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error.code") { value("INVALID_INPUT") }
        }
    }

    @Test
    fun `blank nationality returns 400`() {
        val token = createToken()
        val languageId = createLanguage()

        mockMvc.post("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "name": "Alex",
                  "nationality": "",
                  "major": "Computer Science",
                  "languageIds": [$languageId]
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error.code") { value("INVALID_INPUT") }
        }
    }

    @Test
    fun `blank major returns 400`() {
        val token = createToken()
        val languageId = createLanguage()

        mockMvc.post("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "name": "Alex",
                  "nationality": "Korea",
                  "major": "",
                  "languageIds": [$languageId]
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error.code") { value("INVALID_INPUT") }
        }
    }

    @Test
    fun `empty languages returns 400`() {
        val token = createToken()

        mockMvc.post("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = createProfileJson(emptyList())
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error.code") { value("INVALID_INPUT") }
        }
    }

    @Test
    fun `unknown language returns 400`() {
        val token = createToken()

        mockMvc.post("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = createProfileJson(listOf(Long.MAX_VALUE))
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error.code") { value("INVALID_INPUT") }
        }
    }

    @Test
    fun `duplicate languages are stored once`() {
        val token = createToken()
        val languageId = createLanguage()

        mockMvc.post("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = createProfileJson(
                listOf(languageId, languageId)
            )
        }.andExpect {
            status { isCreated() }
            jsonPath("$.data.profile.languageIds.length()") { value(1) }
        }
    }

    @Test
    fun `unverified user cannot patch profile`() {
        val token = createToken(verified = false)
        val languageId = createLanguage()

        registerProfile(token, languageId)

        mockMvc.patch("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"color": 3}"""
        }.andExpect {
            status { isForbidden() }
            jsonPath("$.error.code") { value("FORBIDDEN") }
        }
    }

    @Test
    fun `verified user can patch editable fields`() {
        val token = createToken(verified = true)
        val languageId = createLanguage()
        val interestId = createInterest()

        registerProfile(token, languageId)

        mockMvc.patch("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "bio": "Hello!",
                  "color": 3,
                  "interestIds": [$interestId]
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.profile.name") { value("Alex") }
            jsonPath("$.data.profile.bio") { value("Hello!") }
            jsonPath("$.data.profile.color") { value(3) }
            jsonPath("$.data.profile.languageIds[0]") { value(languageId) }
            jsonPath("$.data.profile.interestIds[0]") { value(interestId) }
        }
    }

    @Test
    fun `patch preserves omitted fields`() {
        val token = createToken(verified = true)
        val languageId = createLanguage()
        val interestId = createInterest()

        registerProfile(token, languageId)

        mockMvc.patch("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "bio": "Original bio",
                  "color": 2,
                  "interestIds": [$interestId]
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
        }

        mockMvc.patch("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"color": 4}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.profile.bio") { value("Original bio") }
            jsonPath("$.data.profile.color") { value(4) }
            jsonPath("$.data.profile.interestIds[0]") { value(interestId) }
        }
    }

    @Test
    fun `bio null clears biography`() {
        val token = createToken(verified = true)
        val languageId = createLanguage()

        registerProfile(token, languageId)

        mockMvc.patch("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"bio": "Hello!"}"""
        }.andExpect {
            status { isOk() }
        }

        mockMvc.patch("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"bio": null}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.profile.bio") { doesNotExist() }
        }
    }

    @Test
    fun `empty interests clears selections`() {
        val token = createToken(verified = true)
        val languageId = createLanguage()
        val interestId = createInterest()

        registerProfile(token, languageId)

        mockMvc.patch("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"interestIds": [$interestId]}"""
        }.andExpect {
            status { isOk() }
        }

        mockMvc.patch("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"interestIds": []}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.profile.interestIds.length()") { value(0) }
        }
    }

    @Test
    fun `immutable profile fields cannot be patched`() {
        val token = createToken(verified = true)
        val languageId = createLanguage()

        registerProfile(token, languageId)

        mockMvc.patch("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"name": "Jamie"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error.code") { value("INVALID_INPUT") }
        }
    }

    @Test
    fun `invalid color returns 400`() {
        val token = createToken(verified = true)
        val languageId = createLanguage()

        registerProfile(token, languageId)

        mockMvc.patch("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"color": 5}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error.code") { value("INVALID_INPUT") }
        }
    }

    @Test
    fun `unknown interest returns 400`() {
        val token = createToken(verified = true)
        val languageId = createLanguage()

        registerProfile(token, languageId)

        mockMvc.patch("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"interestIds": [${Long.MAX_VALUE}]}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error.code") { value("INVALID_INPUT") }
        }
    }

    @Test
    fun `duplicate interests are stored once`() {
        val token = createToken(verified = true)
        val languageId = createLanguage()
        val interestId = createInterest()

        registerProfile(token, languageId)

        mockMvc.patch("/api/users/me/profile") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = """
                {"interestIds": [$interestId, $interestId]}
            """.trimIndent()
        }.andExpect {
            status { isOk() }
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
    fun `profile creation without JWT returns 401`() {
        val languageId = createLanguage()

        mockMvc.post("/api/users/me/profile") {
            contentType = MediaType.APPLICATION_JSON
            content = createProfileJson(listOf(languageId))
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `profile patch without JWT returns 401`() {
        mockMvc.patch("/api/users/me/profile") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"color": 2}"""
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
