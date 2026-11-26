package com.campushangout.server.account.controller

import com.campushangout.server.account.dto.LoginRequest
import com.campushangout.server.account.service.LoginService
import com.campushangout.server.common.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class LoginController(
    private val loginService: LoginService
) {

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): ResponseEntity<ApiResponse<Map<String, String>>> {

        val accessToken = loginService.login(request)

        return ResponseEntity.ok(
            ApiResponse.ok(
                mapOf("accessToken" to accessToken)
            )
        )
    }
}
