package com.campushangout.server.profile.controller

import com.campushangout.server.common.response.ApiResponse
import com.campushangout.server.profile.dto.OptionResponse
import com.campushangout.server.profile.service.ProfileService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class ProfileOptionsController(
    private val profileService: ProfileService
) {

    @GetMapping("/api/languages")
    fun getLanguages(): ResponseEntity<ApiResponse<List<OptionResponse>>> =
        ResponseEntity.ok(
            ApiResponse.ok(profileService.getLanguages())
        )

    @GetMapping("/api/interests")
    fun getInterests(): ResponseEntity<ApiResponse<List<OptionResponse>>> =
        ResponseEntity.ok(
            ApiResponse.ok(profileService.getInterests())
        )
}
