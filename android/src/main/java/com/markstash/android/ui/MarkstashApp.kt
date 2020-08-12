package com.markstash.android.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.ui.tooling.preview.Preview
import com.markstash.android.Session
import com.markstash.android.ui.login.LoginScreen
import com.markstash.android.ui.login.LoginSettingsScreen
import com.markstash.android.ui.main.MainScreen

@Composable
fun MarkstashApp() {
    MaterialTheme {
        AppContent()
    }
}

sealed class RootScreen {
    object Settings : RootScreen()
    object Login : RootScreen()
    object Main : RootScreen()
}

@Composable
private fun AppContent() {
    val session = Session.ambient.current
    var screen by remember { mutableStateOf(if (session.isLoggedIn) RootScreen.Main else RootScreen.Login) }

    Crossfade(screen) { currentScreen ->
        when (currentScreen) {
            RootScreen.Login -> {
                LoginScreen(
                    onShowSettings = { screen = RootScreen.Settings },
                    onLogIn = { screen = RootScreen.Main }
                )
            }
            RootScreen.Settings -> {
                LoginSettingsScreen(
                    onBackPressed = { screen = RootScreen.Login }
                )
            }
            RootScreen.Main -> {
                MainScreen(
                    onLogOut = { screen = RootScreen.Login }
                )
            }
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    MarkstashApp()
}
