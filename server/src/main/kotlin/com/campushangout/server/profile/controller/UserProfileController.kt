package com.campushangout.server.profile.controller

import com.campushangout.server.common.response.ApiResponse
import com.campushangout.server.profile.dto.UpdateProfileRequest
import com.campushangout.server.profile.dto.UserProfileResponse
import com.campushangout.server.profile.service.ProfileService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserProfileController(
    private val profileService: ProfileService
) {

    @GetMapping("/me")
    fun getMe(
        authentication: Authentication
    ): ResponseEntity<ApiResponse<UserProfileResponse>> {

        val userId = authentication.principal.toString().toLong()

        return ResponseEntity.ok(
            ApiResponse.ok(profileService.getMyProfile(userId))
        )
    }

    @PutMapping("/me/profile")
    fun updateProfile(
        @Valid @RequestBody request: UpdateProfileRequest,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<UserProfileResponse>> {

        val userId = authentication.principal.toString().toLong()

        return ResponseEntity.ok(
            ApiResponse.ok(profileService.updateProfile(userId, request))
        )
    }
}
