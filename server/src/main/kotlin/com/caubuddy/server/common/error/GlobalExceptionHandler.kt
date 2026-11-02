package com.caubuddy.server.common.error

import com.caubuddy.server.common.response.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 모든 예외를 ApiResponse 실패 형식으로 통일한다.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BusinessException::class)
    fun handleBusiness(e: BusinessException): ResponseEntity<ApiResponse<Nothing>> =
        respond(e.errorCode, e.message)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val details = e.bindingResult.fieldErrors.map {
            ApiResponse.FieldErrorDetail(field = it.field, reason = it.defaultMessage ?: "올바르지 않은 값입니다.")
        }
        return respond(ErrorCode.INVALID_INPUT, details = details)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadable(e: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Nothing>> =
        respond(ErrorCode.INVALID_INPUT, "요청 본문을 읽을 수 없습니다.")

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        // 404, 405, 415 같은 Spring MVC 표준 예외는 ErrorResponse를 구현하고 있어 상태 코드를 그대로 살린다.
        if (e is ErrorResponse) {
            val status = e.statusCode
            return ResponseEntity.status(status).body(ApiResponse.fail(ErrorCode.fromHttpStatus(status.value())))
        }
        log.error("Unexpected error", e)
        return respond(ErrorCode.INTERNAL_ERROR)
    }

    private fun respond(
        errorCode: ErrorCode,
        message: String = errorCode.message,
        details: List<ApiResponse.FieldErrorDetail>? = null,
    ): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(errorCode.status).body(ApiResponse.fail(errorCode, message, details))
}
