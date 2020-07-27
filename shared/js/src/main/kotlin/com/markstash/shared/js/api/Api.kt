package com.markstash.shared.js.api

import com.markstash.client.api.ApiClient
import com.markstash.client.api.BookmarksApi
import com.markstash.client.api.NotesApi
import com.markstash.client.api.ResourcesApi
import com.markstash.client.api.SessionsApi
import com.markstash.client.api.TagsApi
import com.markstash.client.api.UsersApi

val apiClient = ApiClient()
val sessionsApi = SessionsApi(apiClient)
val bookmarksApi = BookmarksApi(apiClient)
val notesApi = NotesApi(apiClient)
val resourcesApi = ResourcesApi(apiClient)
val tagsApi = TagsApi(apiClient)
val usersApi = UsersApi(apiClient)
