package com.campushangout.android.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.campushangout.android.R
import com.campushangout.android.designsystem.component.ChTopAppBar
import com.campushangout.android.designsystem.component.EmptyState
import com.campushangout.android.designsystem.theme.CampusHangoutTheme
import com.campushangout.android.designsystem.theme.Spacing

// TODO(Phase 3): 서버의 카테고리 API로 교체한다. 지금은 화면 구성을 보기 위한 임시 목록(문자열 리소스 ID).
private val PlaceholderCategories: List<Int> = listOf(
    R.string.category_all,
    R.string.category_hobby,
    R.string.category_study,
    R.string.category_language_exchange,
    R.string.category_food,
    R.string.category_travel,
    R.string.category_sports,
    R.string.category_k_culture,
    R.string.category_campus_life,
)

/** 홈 탭: 카테고리별 오픈채팅방 둘러보기. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        ChTopAppBar(title = stringResource(R.string.home_title))
        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            itemsIndexed(PlaceholderCategories) { index, labelRes ->
                FilterChip(
                    selected = index == selectedIndex,
                    onClick = { selectedIndex = index },
                    label = { Text(text = stringResource(labelRes)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                )
            }
        }
        EmptyState(
            title = stringResource(R.string.home_empty_title),
            body = stringResource(R.string.home_empty_body),
            modifier = Modifier.weight(1f),
            iconRes = R.drawable.ic_nav_chat,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    CampusHangoutTheme {
        HomeScreen()
    }
}
