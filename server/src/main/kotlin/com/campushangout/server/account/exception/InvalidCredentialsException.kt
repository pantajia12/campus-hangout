package com.campushangout.server.account.exception

import com.campushangout.server.common.error.BusinessException
import com.campushangout.server.common.error.ErrorCode

class InvalidCredentialsException(
    message: String = "이메일 또는 비밀번호가 올바르지 않습니다."
) : BusinessException(
    ErrorCode.UNAUTHORIZED,
    message
)
