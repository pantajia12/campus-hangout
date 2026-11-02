package com.caubuddy.android.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.caubuddy.android.navigation.CauBuddyBottomBar
import com.caubuddy.android.navigation.CauBuddyNavHost
import com.caubuddy.android.navigation.navigateToTopLevel

/**
 * 앱 최상위 화면: 하단 탭 + 탭별 화면.
 * Phase 1에서 로그인 여부/계정 상태(승인 대기 등)에 따라 시작 화면을 나누는 분기가 여기에 추가된다.
 */
@Composable
fun CauBuddyApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()

    Scaffold(
        modifier = modifier,
        // 상단 인셋은 각 화면의 TopAppBar가, 하단 인셋은 NavigationBar가 처리하므로 여기서는 적용하지 않는다.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            CauBuddyBottomBar(
                currentDestination = backStackEntry?.destination,
                onSelect = { navController.navigateToTopLevel(it) },
            )
        },
    ) { innerPadding ->
        CauBuddyNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
