package com.campushangout.android.navigation

import kotlinx.serialization.Serializable

// 타입 안전 네비게이션 라우트. 인자가 필요한 화면은 data class로 만든다.
// 예 (Phase 3): @Serializable data class ChatRoomRoute(val roomId: Long)

@Serializable
data object HomeRoute

@Serializable
data object ChatsRoute

@Serializable
data object FriendsRoute

@Serializable
data object MyRoute
