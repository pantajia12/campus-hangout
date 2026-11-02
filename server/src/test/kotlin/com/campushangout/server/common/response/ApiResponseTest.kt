package com.campushangout.server.common.response

import com.campushangout.server.common.error.ErrorCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ApiResponseTest {

    @Test
    fun `ok는 data만 담고 error는 비운다`() {
        val response = ApiResponse.ok("hello")

        assertTrue(response.success)
        assertEquals("hello", response.data)
        assertNull(response.error)
    }

    @Test
    fun `fail은 에러 코드 이름과 기본 메시지를 담는다`() {
        val response = ApiResponse.fail(ErrorCode.NOT_FOUND)

        assertFalse(response.success)
        assertNull(response.data)
        assertEquals("NOT_FOUND", response.error?.code)
        assertEquals(ErrorCode.NOT_FOUND.message, response.error?.message)
    }

    @Test
    fun `fromHttpStatus는 모르는 4xx를 INVALID_INPUT으로, 5xx를 INTERNAL_ERROR로 바꾼다`() {
        assertEquals(ErrorCode.METHOD_NOT_ALLOWED, ErrorCode.fromHttpStatus(405))
        assertEquals(ErrorCode.INVALID_INPUT, ErrorCode.fromHttpStatus(415))
        assertEquals(ErrorCode.INTERNAL_ERROR, ErrorCode.fromHttpStatus(503))
    }
}
