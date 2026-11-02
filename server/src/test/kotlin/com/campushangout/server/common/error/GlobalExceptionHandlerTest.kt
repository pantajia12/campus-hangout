package com.campushangout.server.common.error

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

class GlobalExceptionHandlerTest {

    /**
     * 예외를 일부러 던지는 테스트 전용 컨트롤러.
     *
     * Spring 6부터는 @Controller(또는 @RestController)가 붙은 클래스만 컨트롤러로 인식한다.
     * 클래스에 @RequestMapping만 있으면 요청 경로가 등록되지 않아 404가 난다.
     */
    @RestController
    @RequestMapping("/test")
    class ThrowingController {

        @GetMapping("/business")
        fun business(): String = throw BusinessException(ErrorCode.CONFLICT, "이미 참여한 채팅방입니다.")

        @GetMapping("/unexpected")
        fun unexpected(): String = throw IllegalStateException("boom")
    }

    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(ThrowingController())
        .setControllerAdvice(GlobalExceptionHandler())
        .build()

    @Test
    fun `BusinessException은 에러 코드의 HTTP 상태와 직접 지정한 메시지로 응답한다`() {
        mockMvc.get("/test/business").andExpect {
            status { isConflict() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("CONFLICT") }
            jsonPath("$.error.message") { value("이미 참여한 채팅방입니다.") }
            jsonPath("$.data") { doesNotExist() }
        }
    }

    @Test
    fun `예상하지 못한 예외는 내부 정보를 숨기고 500으로 응답한다`() {
        mockMvc.get("/test/unexpected").andExpect {
            status { isInternalServerError() }
            jsonPath("$.error.code") { value("INTERNAL_ERROR") }
            jsonPath("$.error.message") { value(ErrorCode.INTERNAL_ERROR.message) }
        }
    }
}
