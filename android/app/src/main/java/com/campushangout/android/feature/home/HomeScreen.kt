package com.campushangout.android.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campushangout.android.R
import com.campushangout.android.designsystem.component.ChTopAppBar
import com.campushangout.android.designsystem.component.EmptyState
import com.campushangout.android.designsystem.theme.CampusHangoutTheme
import com.campushangout.android.designsystem.theme.Spacing

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
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val events by viewModel.events.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadEvents()
    }

    Column(modifier = modifier.fillMaxSize()) {
        ChTopAppBar(title = stringResource(R.string.home_title))

        // Category filter chips
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

        if (events.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.home_empty_title),
                body = stringResource(R.string.home_empty_body),
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_nav_chat,
            )
        } else {
            // Horizontal Carousel Row for Events
            LazyRow(
                contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(events, key = { it.id }) { event ->
                    EventCard(
                        event = event,
                        onClick = {
                            // TODO: Handle click action (e.g., navigate to event details or chat room)
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick, // Makes the card clickable with a ripple effect
        modifier = modifier.width(280.dp), // Set a fixed width for carousel items
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = event.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "📍 ${event.location} | 🕒 ${event.date}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    CampusHangoutTheme {
        HomeScreen()
    }
}
