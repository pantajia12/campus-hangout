package com.campushangout.server.common.auth

import com.campushangout.server.common.error.BusinessException
import com.campushangout.server.common.error.ErrorCode
import org.springframework.core.MethodParameter
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * Turns the authentication set by the JWT filter (principal = user id) into an [AuthUser],
 * so controllers never touch the security context themselves.
 */
class CurrentUserArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(CurrentUser::class.java) &&
            parameter.parameterType == AuthUser::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            throw BusinessException(ErrorCode.UNAUTHORIZED)
        }

        val userId = when (val principal = authentication.principal) {
            is Long -> principal
            is Number -> principal.toLong()
            is String -> principal.toLongOrNull()
            else -> null
        } ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        return AuthUser(userId)
    }
}
