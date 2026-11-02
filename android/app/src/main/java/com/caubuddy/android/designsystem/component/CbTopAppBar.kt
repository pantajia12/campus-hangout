package com.caubuddy.android.designsystem.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** 앱 공통 상단 바. 상태 표시줄(status bar) 인셋은 TopAppBar가 직접 처리한다. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CbTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
        modifier = modifier,
    )
}
