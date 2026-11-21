package com.campushangout.server.account.config

import com.campushangout.server.account.repository.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authorizationHeader = request.getHeader("Authorization")

        if (
            authorizationHeader != null &&
            authorizationHeader.startsWith("Bearer ")
        ) {
            val token = authorizationHeader.removePrefix("Bearer ").trim()
            val userId = jwtTokenProvider.validateAndGetUserId(token)

            if (userId != null && userRepository.existsById(userId)) {
                val authentication = UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    emptyList()
                )

                SecurityContextHolder.getContext().authentication =
                    authentication
            }
        }

        filterChain.doFilter(request, response)
    }
}
