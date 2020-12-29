package com.markstash.android

import com.markstash.client.api.ApiClient
import com.markstash.client.api.BookmarksApi
import com.markstash.client.api.NotesApi
import com.markstash.client.api.ResourcesApi
import com.markstash.client.api.SessionsApi
import com.markstash.client.api.TagsApi
import com.markstash.client.api.UsersApi
import com.markstash.mobile.ui.main.ResourceListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { Session(get()) }

    single<ApiClient> { get<Session>().apiClient }
    single { SessionsApi(get()) }
    single { ResourcesApi(get()) }
    single { BookmarksApi(get()) }
    single { NotesApi(get()) }
    single { TagsApi(get()) }
    single { UsersApi(get()) }

    viewModel { ResourceListViewModel(get()) }
}
