package com.campushangout.server.profile.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class PatchProfileRequest(
    val bio: String? = null,

    @field:Min(1)
    @field:Max(4)
    val color: Int? = null,

    val interestIds: List<Long>? = null
)
