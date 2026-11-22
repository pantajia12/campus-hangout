package com.campushangout.server.common.error

class StudentIdFileTooLargeException : RuntimeException(
    "파일 크기는 10MB를 초과할 수 없습니다."
)
