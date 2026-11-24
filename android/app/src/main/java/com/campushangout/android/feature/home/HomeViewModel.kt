package com.campushangout.android.feature.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    // Dynamic list of events (starts empty or fetched from a server/database)
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    fun loadEvents() {
        // TODO: Fetch from Room database or remote API repository
        _events.value = listOf(
            Event("1", "Campus Hangout Meetup", "Let's meet and chat!", "Social", "2023-11-20", "Student Center"),
            Event("2", "Kotlin Study Group", "Android & Compose study", "Study", "2023-11-22", "Library Room 3")
        )
    }
}
