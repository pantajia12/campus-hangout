package com.campushangout.server.common.response

import com.campushangout.server.common.error.ErrorCode
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * 모든 API가 공통으로 사용하는 응답 봉투.
 *
 * 성공: { "success": true, "data": ... }
 * 실패: { "success": false, "error": { "code": ..., "message": ..., "details": [...] } }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorBody? = null,
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class ErrorBody(
        val code: String,
        val message: String,
        val details: List<FieldErrorDetail>? = null,
    )

    data class FieldErrorDetail(
        val field: String,
        val reason: String,
    )

    companion object {
        fun <T> ok(data: T): ApiResponse<T> = ApiResponse(success = true, data = data)

        fun ok(): ApiResponse<Unit> = ApiResponse(success = true)

        fun fail(
            errorCode: ErrorCode,
            message: String = errorCode.message,
            details: List<FieldErrorDetail>? = null,
        ): ApiResponse<Nothing> = ApiResponse(
            success = false,
            error = ErrorBody(code = errorCode.name, message = message, details = details),
        )
    }
}
