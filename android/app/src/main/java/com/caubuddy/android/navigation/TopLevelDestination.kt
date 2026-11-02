package com.caubuddy.android.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.caubuddy.android.R

/** 하단 탭에 나오는 최상위 화면 목록. 순서가 곧 탭 순서다. */
enum class TopLevelDestination(
    val route: Any,
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
) {
    HOME(HomeRoute, R.drawable.ic_nav_home, R.string.nav_home),
    CHATS(ChatsRoute, R.drawable.ic_nav_chat, R.string.nav_chats),
    FRIENDS(FriendsRoute, R.drawable.ic_nav_friends, R.string.nav_friends),
    MY(MyRoute, R.drawable.ic_nav_my, R.string.nav_my),
}
