package com.campushangout.server.profile

import com.campushangout.server.account.entity.User
import com.campushangout.server.account.repository.UserRepository
import com.campushangout.server.contract.user.UserDirectory
import com.campushangout.server.profile.entity.UserProfile
import com.campushangout.server.profile.repository.UserProfileRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProfileUserDirectoryTest {

    @Autowired
    lateinit var userDirectory: UserDirectory

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var userProfileRepository: UserProfileRepository

    private fun createUser(verified: Boolean, withProfile: Boolean, name: String = "Tester"): Long {
        val userId = userRepository.save(
            User(email = "dir-${UUID.randomUUID()}@test.com", password = "encoded", verified = verified)
        ).userId!!
        if (withProfile) {
            userProfileRepository.save(
                UserProfile(
                    userId = userId,
                    name = name,
                    nationality = "NO",
                    major = "Design",
                    bio = null,
                    color = 3.toShort(),
                )
            )
        }
        return userId
    }

    @Test
    fun `an approved user with a profile is visible`() {
        val userId = createUser(verified = true, withProfile = true, name = "Hakon")

        val summary = userDirectory.getSummary(userId)

        assertEquals("Hakon", summary?.displayName)
        assertEquals("NO", summary?.nationality)
        assertEquals("Design", summary?.major)
        assertEquals(3, summary?.avatarColor)
    }

    @Test
    fun `unapproved users, users without a profile and unknown ids are not visible`() {
        val notApproved = createUser(verified = false, withProfile = true)
        val noProfile = createUser(verified = true, withProfile = false)

        assertNull(userDirectory.getSummary(notApproved))
        assertNull(userDirectory.getSummary(noProfile))
        assertNull(userDirectory.getSummary(999_999L))
    }

    @Test
    fun `batch lookup returns only visible users`() {
        val visible = createUser(verified = true, withProfile = true)
        val hidden = createUser(verified = false, withProfile = true)

        val result = userDirectory.getSummaries(listOf(visible, hidden, 999_999L))

        assertEquals(setOf(visible), result.keys)
        assertTrue(userDirectory.getSummaries(emptyList()).isEmpty())
    }
}
