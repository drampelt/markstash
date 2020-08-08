package com.markstash.android.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.ui.tooling.preview.Preview
import com.markstash.android.ui.login.LoginScreen

@Composable
fun MarkstashApp() {
    MaterialTheme {
        LoginScreen()
    }
}

@Preview
@Composable
fun AppPreview() {
    MarkstashApp()
}
