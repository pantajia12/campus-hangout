package com.caubuddy.android.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class TopLevelDestinationTest {

    @Test
    fun `하단 탭은 홈 채팅 친구 마이 순서로 4개다`() {
        assertEquals(
            listOf(
                TopLevelDestination.HOME,
                TopLevelDestination.CHATS,
                TopLevelDestination.FRIENDS,
                TopLevelDestination.MY,
            ),
            TopLevelDestination.entries,
        )
    }

    @Test
    fun `탭마다 서로 다른 라우트를 가진다`() {
        val routes = TopLevelDestination.entries.map { it.route }
        assertEquals(routes.size, routes.toSet().size)
    }
}
