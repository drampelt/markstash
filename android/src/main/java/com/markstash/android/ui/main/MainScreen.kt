package com.markstash.android.ui.main

import androidx.compose.foundation.Box
import androidx.compose.foundation.Icon
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.markstash.android.R
import com.markstash.android.Session

@Composable
fun MainScreen(onLogOut: () -> Unit) {
    val session = Session.ambient.current
    val scaffoldState = rememberScaffoldState()

    fun handleLogOut() {
        session.logout()
        onLogOut()
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_label_name)) },
                navigationIcon = {
                    IconButton(onClick = { scaffoldState.drawerState.open() }) {
                        Icon(Icons.Default.Menu)
                    }
                },
            )
        },
        drawerContent = { DrawerContent(onLogOut = ::handleLogOut) },
    ) {
    }
}

@Composable
fun DrawerContent(onLogOut: () -> Unit) {
    val session = Session.ambient.current
    ScrollableColumn {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text(stringResource(R.string.main_label_logged_in, session.requireUser().email))
        }

        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            Button(onClick = onLogOut) {
                Text(stringResource(R.string.main_action_log_out))
            }
        }
    }
}
