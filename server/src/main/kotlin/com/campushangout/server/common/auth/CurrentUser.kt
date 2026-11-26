package com.campushangout.server.common.auth

/** The logged-in user of the current request. */
data class AuthUser(val userId: Long)

/**
 * Put on a controller parameter of type [AuthUser] to receive the logged-in user:
 *
 * ```
 * @PostMapping("/api/v1/rooms")
 * fun create(@CurrentUser user: AuthUser, @Valid @RequestBody request: CreateRoomRequest) = ...
 * ```
 *
 * Responds with 401 UNAUTHORIZED when nobody is logged in.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class CurrentUser
