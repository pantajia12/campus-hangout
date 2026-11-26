package com.campushangout.server.common.error

import com.campushangout.server.common.response.ApiResponse
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.test.assertEquals

class ApiErrorCodeTest {

    /** An area-specific error code, the way social/account define theirs. */
    enum class SampleErrorCode(
        override val status: HttpStatus,
        override val message: String,
    ) : ApiErrorCode {
        ROOM_SAMPLE_FULL(HttpStatus.CONFLICT, "sample: room is full"),
        ;

        override val code: String get() = name
    }

    @RestController
    class SampleController {
        @GetMapping("/test/area-error")
        fun areaError(): String = throw BusinessException(SampleErrorCode.ROOM_SAMPLE_FULL)
    }

    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(SampleController())
        .setControllerAdvice(GlobalExceptionHandler())
        .build()

    @Test
    fun `an area error code is returned with its own code and HTTP status`() {
        mockMvc.get("/test/area-error").andExpect {
            status { isConflict() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("ROOM_SAMPLE_FULL") }
            jsonPath("$.error.message") { value("sample: room is full") }
        }
    }

    @Test
    fun `ApiResponse fail uses the code property`() {
        val response = ApiResponse.fail(SampleErrorCode.ROOM_SAMPLE_FULL)

        assertEquals("ROOM_SAMPLE_FULL", response.error?.code)
    }

    @Test
    fun `common error codes keep their enum name as code`() {
        assertEquals("NOT_FOUND", ErrorCode.NOT_FOUND.code)
        assertEquals(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND.status)
    }
}
