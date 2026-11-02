package com.campushangout.server.common.error

/**
 * 비즈니스 규칙 위반을 나타내는 예외.
 * 서비스 계층에서 던지면 GlobalExceptionHandler가 공통 응답 형식으로 바꿔 준다.
 *
 * 예: throw BusinessException(ErrorCode.NOT_FOUND, "채팅방을 찾을 수 없습니다.")
 */
open class BusinessException(
    val errorCode: ErrorCode,
    override val message: String = errorCode.message,
) : RuntimeException(message)
