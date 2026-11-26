package com.campushangout.server.profile.service

import com.campushangout.server.account.repository.UserRepository
import com.campushangout.server.contract.user.UserDirectory
import com.campushangout.server.contract.user.UserSummary
import com.campushangout.server.profile.repository.UserProfileRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** Profile area's implementation of [UserDirectory]. Owner: profile area. */
@Service
class ProfileUserDirectory(
    private val userRepository: UserRepository,
    private val userProfileRepository: UserProfileRepository,
) : UserDirectory {

    @Transactional(readOnly = true)
    override fun getSummary(userId: Long): UserSummary? =
        getSummaries(listOf(userId))[userId]

    @Transactional(readOnly = true)
    override fun getSummaries(userIds: Collection<Long>): Map<Long, UserSummary> {
        val ids = userIds.toSet()
        if (ids.isEmpty()) return emptyMap()

        val approvedIds = userRepository.findAllById(ids)
            .filter { it.verified }
            .mapNotNull { it.userId }
            .toSet()
        if (approvedIds.isEmpty()) return emptyMap()

        return userProfileRepository.findAllById(approvedIds).associate { profile ->
            profile.userId to UserSummary(
                userId = profile.userId,
                displayName = profile.name,
                nationality = profile.nationality,
                major = profile.major,
                avatarColor = profile.color.toInt(),
            )
        }
    }
}
