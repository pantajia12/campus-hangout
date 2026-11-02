package com.campushangout.android.feature.my

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

/** 마이 탭: 내 프로필(실명/익명), 관심사 수정, 설정. (Phase 2에서 구현) */
@Composable
fun MyScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        ChTopAppBar(title = stringResource(R.string.my_title))
        EmptyState(
            title = stringResource(R.string.my_empty_title),
            body = stringResource(R.string.my_empty_body),
            modifier = Modifier.weight(1f),
            iconRes = R.drawable.ic_nav_my,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MyScreenPreview() {
    CampusHangoutTheme {
        MyScreen()
    }
}
