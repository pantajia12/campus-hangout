package com.campushangout.server.common.error

import com.campushangout.server.common.response.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException
/**
 * 모든 예외를 ApiResponse 실패 형식으로 통일한다.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BusinessException::class)
    fun handleBusiness(
        e: BusinessException
    ): ResponseEntity<ApiResponse<Nothing>> =
        respond(e.errorCode, e.message)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(
        e: MethodArgumentNotValidException
    ): ResponseEntity<ApiResponse<Nothing>> {
        val details = e.bindingResult.fieldErrors.map {
            ApiResponse.FieldErrorDetail(
                field = it.field,
                reason = it.defaultMessage ?: "올바르지 않은 값입니다."
            )
        }

        return respond(ErrorCode.INVALID_INPUT, details = details)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadable(
        e: HttpMessageNotReadableException
    ): ResponseEntity<ApiResponse<Nothing>> =
        respond(
            ErrorCode.INVALID_INPUT,
            "요청 본문을 읽을 수 없습니다."
        )

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSize(
        e: MaxUploadSizeExceededException
    ): ResponseEntity<ApiResponse<Nothing>> =
        fileTooLargeResponse()

    @ExceptionHandler(StudentIdFileTooLargeException::class)
    fun handleStudentIdFileTooLarge(
        e: StudentIdFileTooLargeException
    ): ResponseEntity<ApiResponse<Nothing>> =
        fileTooLargeResponse()

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(
        e: Exception
    ): ResponseEntity<ApiResponse<Nothing>> {
        if (e is ErrorResponse) {
            val status = e.statusCode

            return ResponseEntity.status(status)
                .body(
                    ApiResponse.fail(
                        ErrorCode.fromHttpStatus(status.value())
                    )
                )
        }

        log.error("Unexpected error", e)
        return respond(ErrorCode.INTERNAL_ERROR)
    }

    private fun fileTooLargeResponse(): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(
                ApiResponse.fail(
                    ErrorCode.INVALID_INPUT,
                    "파일 크기는 10MB를 초과할 수 없습니다."
                )
            )

    private fun respond(
        errorCode: ErrorCode,
        message: String = errorCode.message,
        details: List<ApiResponse.FieldErrorDetail>? = null
    ): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(errorCode.status)
            .body(ApiResponse.fail(errorCode, message, details))
}
