package com.markstash.android.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import androidx.ui.tooling.preview.Preview
import com.markstash.android.Session
import com.markstash.android.ui.login.LoginScreen
import com.markstash.android.ui.login.LoginSettingsScreen
import com.markstash.android.ui.main.MainScreen
import org.koin.androidx.compose.inject

@Composable
fun MarkstashApp() {
    MaterialTheme {
        AppContent()
    }
}

sealed class RootScreen(val name: String) {
    object Settings : RootScreen("settings")
    object Login : RootScreen("login")
}

@Composable
private fun AppContent() {
    val session: Session by inject()
    var isLoggedIn by remember { mutableStateOf(session.isLoggedIn) }
    val navController = rememberNavController()

    if (isLoggedIn) {
        MainScreen(
            onLogOut = { isLoggedIn = false }
        )
    } else {
        NavHost(navController, startDestination = RootScreen.Login.name) {
            composable(RootScreen.Login.name) {
                LoginScreen(
                    onShowSettings = { navController.navigate(RootScreen.Settings.name) },
                    onLogIn = { isLoggedIn = true }
                )
            }
            composable(RootScreen.Settings.name) {
                LoginSettingsScreen(
                    onBackPressed = { navController.popBackStack() }
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
