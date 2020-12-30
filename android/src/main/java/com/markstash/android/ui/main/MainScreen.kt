package com.markstash.android.ui.main

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.markstash.android.AmbientReceivedIntent
import com.markstash.android.R
import com.markstash.mobile.Session
import com.markstash.mobile.ui.main.ResourceListViewModel
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@Composable
fun MainScreen(onLogOut: () -> Unit) {
    val session = get<Session>()
    val scaffoldState = rememberScaffoldState()
    val receivedIntent = AmbientReceivedIntent.current

    val context = AmbientContext.current

    LaunchedEffect(Unit) {
        val intent = receivedIntent.intent
        if (intent != null && intent.action == Intent.ACTION_SEND && intent.type == "") {
            receivedIntent.handle()
            Toast.makeText(context, "Received: ${intent.getStringExtra(Intent.EXTRA_TEXT)}", Toast.LENGTH_LONG).show()
        }
    }

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
        IndexScreen()
    }
}

@Composable
fun DrawerContent(onLogOut: () -> Unit) {
    val session = get<Session>()

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

@Composable
fun IndexScreen() {
    val viewModel = getViewModel<ResourceListViewModel>()
    val state by viewModel.state.collectAsState()

    if (state.isLoading) {
        Text(
            text = stringResource(R.string.resource_label_loading),
            modifier = Modifier.padding(16.dp)
        )
    } else {
        ResourceList(state.resources)
    }
}
