package com.campushangout.server.profile.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateProfileRequest(

    @field:NotBlank
    @field:Size(max = 100)
    val name: String,

    @field:Size(max = 100)
    val nationality: String? = null,

    @field:Size(max = 100)
    val major: String? = null,

    val bio: String? = null,

    @field:Min(1)
    @field:Max(4)
    val color: Int = 1,

    val languageIds: List<Long> = emptyList(),

    val interestIds: List<Long> = emptyList()
)
