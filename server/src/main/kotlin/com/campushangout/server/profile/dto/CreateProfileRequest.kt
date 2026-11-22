package com.campushangout.server.profile.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class CreateProfileRequest(
    @field:NotBlank
    @field:Size(max = 100)
    val name: String,

    @field:NotBlank
    @field:Size(max = 100)
    val nationality: String,

    @field:NotBlank
    @field:Size(max = 100)
    val major: String,

    @field:NotEmpty
    val languageIds: List<Long>
)
