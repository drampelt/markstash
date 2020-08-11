package com.markstash.android.ui

import androidx.compose.animation.Crossfade
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.ui.tooling.preview.Preview
import com.markstash.android.ui.login.LoginScreen
import com.markstash.android.ui.login.LoginSettingsScreen

@Composable
fun MarkstashApp() {
    MaterialTheme {
        AppContent()
    }
}

@Composable
private fun AppContent() {
    var showSettings by remember { mutableStateOf(false) }

    Crossfade(showSettings) { shouldShowSettings ->
        if (shouldShowSettings) {
            LoginSettingsScreen(
                onBackPressed = { showSettings = false }
            )
        } else {
            LoginScreen(
                onShowSettings = { showSettings = true },
                onLogIn = { }
            )
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    MarkstashApp()
}
