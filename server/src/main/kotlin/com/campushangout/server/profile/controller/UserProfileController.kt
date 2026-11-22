package com.campushangout.server.profile.controller

import com.campushangout.server.common.response.ApiResponse
import com.campushangout.server.profile.dto.CreateProfileRequest
import com.campushangout.server.profile.dto.UserProfileResponse
import com.campushangout.server.profile.service.ProfileService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import tools.jackson.databind.JsonNode

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

    @PostMapping("/me/profile")
    fun createProfile(
        @Valid @RequestBody request: CreateProfileRequest,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<UserProfileResponse>> {
        val userId = authentication.principal.toString().toLong()

        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.ok(profileService.createProfile(userId, request))
        )
    }

    @PatchMapping("/me/profile")
    fun patchProfile(
        @RequestBody request: JsonNode,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<UserProfileResponse>> {
        val userId = authentication.principal.toString().toLong()

        return ResponseEntity.ok(
            ApiResponse.ok(profileService.patchProfile(userId, request))
        )
    }
}
