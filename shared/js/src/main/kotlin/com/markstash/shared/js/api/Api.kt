package com.markstash.shared.js.api

import com.markstash.client.api.ApiClient
import com.markstash.client.api.BookmarksApi
import com.markstash.client.api.SessionsApi

val apiClient = ApiClient("http://localhost:8080/api")
val sessionsApi = SessionsApi(apiClient)
val bookmarksApi = BookmarksApi(apiClient)
