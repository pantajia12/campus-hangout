package com.campushangout.server.account.controller

import com.campushangout.server.account.dto.StudentVerificationResponse
import com.campushangout.server.account.service.StudentVerificationService
import com.campushangout.server.common.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/student-verifications")
class StudentVerificationController(
    private val studentVerificationService: StudentVerificationService
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun submit(
        @RequestPart("file") file: MultipartFile,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<StudentVerificationResponse>> {

        val userId = authentication.principal.toString().toLong()

        val result = studentVerificationService.submit(userId, file)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.ok(result))
    }

    @GetMapping("/me")
    fun getMyStatus(
        authentication: Authentication
    ): ResponseEntity<ApiResponse<StudentVerificationResponse>> {

        val userId = authentication.principal.toString().toLong()

        return ResponseEntity.ok(
            ApiResponse.ok(
                studentVerificationService.getMyStatus(userId)
            )
        )
    }
}
