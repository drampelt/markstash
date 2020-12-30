package com.markstash.mobile.ui.login

import com.markstash.api.sessions.LoginRequest
import com.markstash.client.api.SessionsApi
import com.markstash.mobile.Session
import com.markstash.mobile.StateViewModel
import kotlinx.coroutines.launch

data class LoginViewState(
    val isLoggingIn: Boolean = false,
    val didLogIn: Boolean = false,
    val error: Throwable? = null,
)

class LoginViewModel(
    private val session: Session,
    private val sessionsApi: SessionsApi,
) : StateViewModel<LoginViewState>(LoginViewState()) {
    fun login(email: String, password: String) {
        scope.launch {
            setState(currentState.copy(isLoggingIn = true))

            val loginResponse = runCatching { sessionsApi.login(LoginRequest(email, password)) }
                .getOrElse {
                    setState(currentState.copy(isLoggingIn = false, error = it))
                    return@launch
                }

            session.login(loginResponse)
            setState(currentState.copy(isLoggingIn = false, didLogIn = true))
        }
    }

    fun dismissError() = setState(currentState.copy(error = null))
}
