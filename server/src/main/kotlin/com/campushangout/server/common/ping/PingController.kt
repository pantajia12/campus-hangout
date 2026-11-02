package com.campushangout.server.common.ping

import com.campushangout.server.common.response.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

/**
 * 앱이 서버에 연결되는지 확인하는 용도의 가장 단순한 API.
 * 운영 모니터링용 헬스체크는 actuator(/actuator/health)를 사용한다.
 */
@RestController
@RequestMapping("/api/v1")
class PingController {

    @GetMapping("/ping")
    fun ping(): ApiResponse<PingResponse> =
        ApiResponse.ok(PingResponse(status = "ok", serverTime = Instant.now()))
}

data class PingResponse(
    val status: String,
    val serverTime: Instant,
)
