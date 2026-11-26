package com.campushangout.server.account.exception

import com.campushangout.server.common.error.BusinessException
import com.campushangout.server.common.error.ErrorCode

class DuplicateEmailException : BusinessException(
    ErrorCode.CONFLICT,
    "이미 가입된 이메일입니다."
)
