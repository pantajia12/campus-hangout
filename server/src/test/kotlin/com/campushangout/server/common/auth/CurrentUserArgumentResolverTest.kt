package com.campushangout.server.common.auth

import com.campushangout.server.common.error.GlobalExceptionHandler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

class CurrentUserArgumentResolverTest {

    @RestController
    class MeController {
        @GetMapping("/test/me")
        fun me(@CurrentUser user: AuthUser): Map<String, Long> = mapOf("userId" to user.userId)
    }

    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(MeController())
        .setCustomArgumentResolvers(CurrentUserArgumentResolver())
        .setControllerAdvice(GlobalExceptionHandler())
        .build()

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `the logged-in user id from the JWT filter is injected`() {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(7L, null, emptyList())

        mockMvc.get("/test/me").andExpect {
            status { isOk() }
            jsonPath("$.userId") { value(7) }
        }
    }

    @Test
    fun `no authentication returns 401 UNAUTHORIZED`() {
        mockMvc.get("/test/me").andExpect {
            status { isUnauthorized() }
            jsonPath("$.error.code") { value("UNAUTHORIZED") }
        }
    }

    @Test
    fun `anonymous authentication returns 401 UNAUTHORIZED`() {
        SecurityContextHolder.getContext().authentication = AnonymousAuthenticationToken(
            "key",
            "anonymousUser",
            AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"),
        )

        mockMvc.get("/test/me").andExpect {
            status { isUnauthorized() }
            jsonPath("$.error.code") { value("UNAUTHORIZED") }
        }
    }
}
