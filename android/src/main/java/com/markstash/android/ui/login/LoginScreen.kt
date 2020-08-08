package com.markstash.android.ui.login

import android.widget.Toast
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.markstash.android.R
import com.markstash.android.Session
import com.markstash.api.sessions.LoginRequest
import com.markstash.client.api.SessionsApi
import kotlinx.coroutines.launch

@Composable
fun LoginScreen() {
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val session = Session.ambient.current
    val context = ContextAmbient.current
    val sessionsApi = remember { SessionsApi(session.apiClient) }

    fun handleLogIn(email: String, password: String) {
        isLoading = true
        scope.launch {
            try {
                val response = sessionsApi.login(LoginRequest(email, password))
                session.login(response)
                Toast.makeText(context, "Logged in as $response", Toast.LENGTH_LONG).show()
            } catch (e: Throwable) {
                isLoading = false
                Toast.makeText(context, e.message ?: "Error logging in", Toast.LENGTH_LONG).show()
            }
        }
    }

    ScrollableColumn {
        Spacer(Modifier.height(32.dp))

        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.app_label_name),
                style = MaterialTheme.typography.h4,
            )
        }

        Spacer(Modifier.height(32.dp))

        LoginForm(
            onLogIn = ::handleLogIn,
            isLoading = isLoading,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
fun LoginForm(
    onLogIn: ((email: String, password: String) -> Unit)? = null,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var email by savedInstanceState(saver = TextFieldValue.Saver) { TextFieldValue() }
    var password by savedInstanceState(saver = TextFieldValue.Saver) { TextFieldValue() }

    Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.login_label_email)) },
            imeAction = ImeAction.Next,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.login_label_password)) },
            imeAction = ImeAction.Go,
            keyboardType = KeyboardType.Password,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { onLogIn?.invoke(email.text, password.text) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(if (isLoading) R.string.login_label_logging_in else R.string.login_action_log_in))
        }
    }
}

@Preview
@Composable
fun LoginFormPreview() {
    MaterialTheme {
        LoginForm()
    }
}
