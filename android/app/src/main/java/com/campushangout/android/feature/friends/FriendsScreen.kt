package com.campushangout.android.feature.friends

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.campushangout.android.R
import com.campushangout.android.designsystem.component.ChTopAppBar
import com.campushangout.android.designsystem.component.EmptyState
import com.campushangout.android.designsystem.theme.CampusHangoutTheme

/** 친구 탭: 관심사·언어 기반 추천 친구와 노크. (Phase 5에서 구현) */
@Composable
fun FriendsScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        ChTopAppBar(title = stringResource(R.string.friends_title))
        EmptyState(
            title = stringResource(R.string.friends_empty_title),
            body = stringResource(R.string.friends_empty_body),
            modifier = Modifier.weight(1f),
            iconRes = R.drawable.ic_nav_friends,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FriendsScreenPreview() {
    CampusHangoutTheme {
        FriendsScreen()
    }
}
