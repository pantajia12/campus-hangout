package com.campushangout.server.common.error

import org.springframework.http.HttpStatus

/**
 * An error the API can return. Each area keeps its own enum that implements this
 * (common: [ErrorCode], account: AUTH_/VERIFY_/PROFILE_..., social: ROOM_/CHAT_/KNOCK_...).
 *
 * - [code] is what the app switches on. It must be unique across the server, so use the area prefix.
 * - [message] is for developers only. The app shows its own localized text for each code.
 */
interface ApiErrorCode {
    val code: String
    val status: HttpStatus
    val message: String
}
