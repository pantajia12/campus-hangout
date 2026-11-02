package com.caubuddy.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.caubuddy.android.designsystem.theme.CauBuddyTheme
import com.caubuddy.android.ui.CauBuddyApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CauBuddyTheme {
                CauBuddyApp()
            }
        }
    }
}
