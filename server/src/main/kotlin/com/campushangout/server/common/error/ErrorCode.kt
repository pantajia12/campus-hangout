package com.campushangout.server.common.error

import org.springframework.http.HttpStatus

/**
 * 앱은 code(enum 이름)로 분기하고, 사용자에게 보여 줄 문구는 앱의 다국어 리소스에서 가져온다.
 * message는 개발·디버깅용이다.
 *
 * 도메인별 에러 코드는 각 Phase에서 추가한다. (예: Phase 1 - DUPLICATE_STUDENT_NUMBER)
 */
enum class ErrorCode(
    val status: HttpStatus,
    val message: String,
) {
    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    CONFLICT(HttpStatus.CONFLICT, "요청이 현재 상태와 충돌합니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    ;

    companion object {
        /** Spring이 던지는 표준 예외의 HTTP 상태를 가장 가까운 공통 코드로 바꾼다. */
        fun fromHttpStatus(status: Int): ErrorCode =
            entries.firstOrNull { it.status.value() == status }
                ?: if (status in 400..499) INVALID_INPUT else INTERNAL_ERROR
    }
}
