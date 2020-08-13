package com.markstash.android.ui.login

import androidx.compose.foundation.Icon
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.markstash.android.R
import com.markstash.android.Session
import com.markstash.android.inject

@Composable
fun LoginSettingsScreen(onBackPressed: () -> Unit) {
    val session: Session by inject()
    var baseUrl by savedInstanceState(saver = TextFieldValue.Saver) { TextFieldValue(text = session.baseUrl) }

    fun handleBackPress() {
        session.baseUrl = baseUrl.text
        onBackPressed()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_label_name)) },
                navigationIcon = {
                    IconButton(onClick = ::handleBackPress) {
                        Icon(Icons.Default.ArrowBack)
                    }
                },
            )
        }
    ) {
        LoginSettings(
            baseUrl = baseUrl,
            setBaseUrl = { baseUrl = it },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun LoginSettings(
    baseUrl: TextFieldValue,
    setBaseUrl: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {

    ScrollableColumn(modifier = modifier) {
        TextField(
            value = baseUrl,
            onValueChange = setBaseUrl,
            label = { Text(stringResource(R.string.settings_label_server_address)) },
            placeholder = { Text(stringResource(R.string.settings_default_server_address)) },
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Done,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { setBaseUrl(TextFieldValue("")) }) {
                    Icon(Icons.Default.Clear)
                }
            },
        )

        Text(
            text = stringResource(R.string.settings_label_server_address_description),
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
