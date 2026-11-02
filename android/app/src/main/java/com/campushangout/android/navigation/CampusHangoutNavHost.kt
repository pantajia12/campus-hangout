package com.campushangout.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.campushangout.android.feature.chat.ChatListScreen
import com.campushangout.android.feature.friends.FriendsScreen
import com.campushangout.android.feature.home.HomeScreen
import com.campushangout.android.feature.my.MyScreen

@Composable
fun CampusHangoutNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        composable<HomeRoute> { HomeScreen() }
        composable<ChatsRoute> { ChatListScreen() }
        composable<FriendsRoute> { FriendsScreen() }
        composable<MyRoute> { MyScreen() }
    }
}

/**
 * 탭 이동. 탭을 오갈 때 각 탭의 화면 상태(스크롤 위치 등)를 저장·복원하고,
 * 같은 탭을 여러 번 눌러도 화면이 쌓이지 않게 한다.
 */
fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
