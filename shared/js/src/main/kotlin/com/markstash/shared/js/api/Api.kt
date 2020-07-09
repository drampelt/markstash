package com.markstash.shared.js.api

import com.markstash.client.api.ApiClient
import com.markstash.client.api.BookmarksApi
import com.markstash.client.api.SessionsApi
import com.markstash.client.api.UsersApi

val apiClient = ApiClient()
val sessionsApi = SessionsApi(apiClient)
val bookmarksApi = BookmarksApi(apiClient)
val usersApi = UsersApi(apiClient)
