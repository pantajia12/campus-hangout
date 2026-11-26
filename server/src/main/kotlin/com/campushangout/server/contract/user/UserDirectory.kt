package com.campushangout.server.contract.user

/**
 * Read-only view of users for other areas (e.g. social shows names in chats and member lists).
 * Implemented by the profile area. Changing this interface needs approval from both backend owners.
 *
 * Only **approved users with a profile** are visible. Everyone else is treated as "not found".
 */
interface UserDirectory {

    /** `null` if the user does not exist, is not approved, or has no profile yet. */
    fun getSummary(userId: Long): UserSummary?

    /** Batch lookup for lists, without N+1 queries. Ids that are not visible are left out of the map. */
    fun getSummaries(userIds: Collection<Long>): Map<Long, UserSummary>
}

data class UserSummary(
    val userId: Long,
    /** Verified real name. */
    val displayName: String,
    /** ISO 3166-1 alpha-2, e.g. "KR". */
    val nationality: String?,
    val major: String?,
    /** 1..4, mapped to a theme color in the app. */
    val avatarColor: Int,
)
