package com.markstash.android

import com.markstash.client.api.ApiClient
import com.markstash.client.api.BookmarksApi
import com.markstash.client.api.MutableApiClient
import com.markstash.client.api.NotesApi
import com.markstash.client.api.ResourcesApi
import com.markstash.client.api.SessionsApi
import com.markstash.client.api.TagsApi
import com.markstash.client.api.UsersApi
import com.markstash.mobile.Session
import com.markstash.mobile.ui.login.LoginViewModel
import com.markstash.mobile.ui.main.ResourceListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Session(listener = object : Session.Listener {
            override fun onAuthTokenChange(authToken: String?) {
                (get<ApiClient>() as MutableApiClient).authToken = authToken
            }

            override fun onBaseUrlChange(baseUrl: String) {
                (get<ApiClient>() as MutableApiClient).baseUrl = baseUrl
            }
        })
    }

    single<ApiClient> { MutableApiClient(baseUrl = Session.DEFAULT_BASE_URL) }

    single { SessionsApi(get()) }
    single { ResourcesApi(get()) }
    single { BookmarksApi(get()) }
    single { NotesApi(get()) }
    single { TagsApi(get()) }
    single { UsersApi(get()) }

    viewModel { LoginViewModel(get(), get()) }
    viewModel { ResourceListViewModel(get()) }
}
