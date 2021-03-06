package com.markstash.android.ui.login

import android.widget.Toast
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focusRequester
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.markstash.android.R
import com.markstash.mobile.ui.login.LoginViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun LoginScreen(onShowSettings: () -> Unit, onLogIn: () -> Unit) {
    val viewModel = getViewModel<LoginViewModel>()
    val state by viewModel.state.collectAsState()
    val context = AmbientContext.current

    LaunchedEffect(state) {
        if (state.didLogIn) onLogIn()
        if (state.error != null) {
            Toast.makeText(context, state.error?.message, Toast.LENGTH_LONG).show()
            viewModel.dismissError()
        }
    }

    ScrollableColumn {
        IconButton(onClick = onShowSettings) {
            Icon(Icons.Default.Settings)
        }

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
            onLogIn = viewModel::login,
            isLoading = state.isLoggingIn,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
fun LoginForm(
    modifier: Modifier = Modifier,
    onLogIn: ((email: String, password: String) -> Unit)? = null,
    isLoading: Boolean = false,
) {
    var email by savedInstanceState(saver = TextFieldValue.Saver) { TextFieldValue() }
    var password by savedInstanceState(saver = TextFieldValue.Saver) { TextFieldValue() }

    val passwordFocusRequester = remember { FocusRequester() }

    Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.login_label_email)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            onImeActionPerformed = { action, _ ->
                if (action == ImeAction.Next) passwordFocusRequester.requestFocus()
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.login_label_password)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Go),
            onImeActionPerformed = { action, controller ->
                if (action == ImeAction.Go) {
                    controller?.hideSoftwareKeyboard()
                    onLogIn?.invoke(email.text, password.text)
                }
            },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFocusRequester),
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
