package com.campushangout.server.profile.dto

data class UserProfileResponse(
    val userId: Long,
    val email: String,
    val verified: Boolean,
    val profile: ProfileDetail?
)

data class ProfileDetail(
    val name: String,
    val nationality: String?,
    val major: String?,
    val bio: String?,
    val color: Int,
    val languageIds: List<Long>,
    val interestIds: List<Long>
)
