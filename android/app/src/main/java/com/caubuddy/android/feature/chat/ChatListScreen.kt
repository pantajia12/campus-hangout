package com.caubuddy.android.feature.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.caubuddy.android.R
import com.caubuddy.android.designsystem.component.CbTopAppBar
import com.caubuddy.android.designsystem.component.EmptyState
import com.caubuddy.android.designsystem.theme.CauBuddyTheme

/** 채팅 탭: 내가 참여 중인 채팅방 목록. (Phase 3~4에서 구현) */
@Composable
fun ChatListScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        CbTopAppBar(title = stringResource(R.string.chats_title))
        EmptyState(
            title = stringResource(R.string.chats_empty_title),
            body = stringResource(R.string.chats_empty_body),
            modifier = Modifier.weight(1f),
            iconRes = R.drawable.ic_nav_chat,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatListScreenPreview() {
    CauBuddyTheme {
        ChatListScreen()
    }
}
