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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.launchInComposition
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.markstash.android.R
import com.markstash.android.Session
import com.markstash.api.models.Resource
import com.markstash.client.api.ResourcesApi
import com.markstash.client.api.SessionsApi

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
        IndexScreen()
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

@Composable
fun IndexScreen() {
    val session = Session.ambient.current
    val resourcesApi = remember { ResourcesApi(session.apiClient) }

    var resources: List<Resource> by remember { mutableStateOf(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    launchInComposition {
        isLoading = true
        try {
            resources = resourcesApi.index()
            isLoading = false
        } catch (e: Throwable) {

        }
    }

    if (isLoading) {
        Text(
            text = stringResource(R.string.resource_label_loading),
            modifier = Modifier.padding(16.dp)
        )
    } else {
        ResourceList(resources)
    }
}
