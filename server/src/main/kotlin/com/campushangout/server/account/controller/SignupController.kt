package com.campushangout.server.account.controller

import com.campushangout.server.account.dto.SignupRequest
import com.campushangout.server.account.service.SignupService
import com.campushangout.server.common.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class SignupController(
    private val signupService: SignupService
) {

    @PostMapping("/signup")
    fun signup(
        @Valid @RequestBody request: SignupRequest
    ): ResponseEntity<ApiResponse<Map<String, Long>>> {

        val userId = signupService.signup(request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                ApiResponse.ok(
                    mapOf("userId" to userId)
                )
            )
    }
}
