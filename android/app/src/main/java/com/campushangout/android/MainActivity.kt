package com.campushangout.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.campushangout.android.designsystem.theme.CampusHangoutTheme
import com.campushangout.android.ui.CampusHangoutApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampusHangoutTheme {
                CampusHangoutApp()
            }
        }
    }
}
