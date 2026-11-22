package com.campushangout.server.account.config

import com.campushangout.server.account.repository.UserRepository
import com.campushangout.server.common.error.ErrorCode
import com.campushangout.server.common.response.ApiResponse
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import tools.jackson.databind.ObjectMapper

@Component
class StudentVerificationAccessFilter(
    private val userRepository: UserRepository,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    private val allowedBeforeApproval = setOf(
        "POST" to "/api/auth/signup",
        "POST" to "/api/auth/login",
        "GET" to "/api/users/me",
        "POST" to "/api/users/me/profile",
        "POST" to "/api/student-verifications",
        "GET" to "/api/student-verifications/me",
        "GET" to "/api/languages",
        "GET" to "/api/interests"
    )

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val path = request.requestURI
            .removePrefix(request.contextPath)
            .trimEnd('/')
            .ifEmpty { "/" }

        val method = request.method.uppercase()

        if ((method to path) in allowedBeforeApproval) {
            filterChain.doFilter(request, response)
            return
        }

        val authentication =
            SecurityContextHolder.getContext().authentication

        val userId = authentication
            ?.principal
            ?.toString()
            ?.toLongOrNull()

        if (userId == null) {
            filterChain.doFilter(request, response)
            return
        }

        val user = userRepository.findById(userId).orElse(null)

        if (user != null && !user.verified) {
            response.status = HttpServletResponse.SC_FORBIDDEN
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.characterEncoding = "UTF-8"

            objectMapper.writeValue(
                response.outputStream,
                ApiResponse.fail(
                    ErrorCode.FORBIDDEN,
                    "학생증 승인 후 이용할 수 있습니다."
                )
            )
            return
        }

        filterChain.doFilter(request, response)
    }
}
