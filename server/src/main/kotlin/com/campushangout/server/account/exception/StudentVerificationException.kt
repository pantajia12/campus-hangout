package com.campushangout.server.account.exception

import com.campushangout.server.common.error.BusinessException
import com.campushangout.server.common.error.ErrorCode

class InvalidStudentIdImageException :
    BusinessException(
        ErrorCode.INVALID_INPUT,
        "JPG 또는 PNG 학생증 이미지만 제출할 수 있습니다."
    )

class StudentVerificationAlreadySubmittedException :
    BusinessException(
        ErrorCode.CONFLICT,
        "이미 학생증 인증을 신청했습니다."
    )

class StudentVerificationUserNotFoundException :
    BusinessException(
        ErrorCode.NOT_FOUND,
        "사용자를 찾을 수 없습니다."
    )
