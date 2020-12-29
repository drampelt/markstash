//
//  ApiHelper.swift
//  Markstash
//
//  Created by Daniel Rampelt on 2020-12-27.
//

import Foundation
import shared

class Apis {
    let client: ApiClient

    lazy var sessions: SessionsApi = { SessionsApi(apiClient: client) }()
    lazy var archives: ArchivesApi = { ArchivesApi(apiClient: client) }()
    lazy var bookmarks: BookmarksApi = { BookmarksApi(apiClient: client) }()
    lazy var notes: NotesApi = { NotesApi(apiClient: client) }()
    lazy var resources: ResourcesApi = { ResourcesApi(apiClient: client) }()
    lazy var tags: TagsApi = { TagsApi(apiClient: client) }()
    lazy var users: UsersApi = { UsersApi(apiClient: client) }()

    init(baseUrl: String, authToken: String?) {
        client = FrozenApiClient(baseUrl: baseUrl, authToken: authToken)
    }
}

extension Apis {
    static var instance = Apis(baseUrl: "https://app.markstash.com/api", authToken: nil)

    static var sessions: SessionsApi { return instance.sessions }
    static var archives: ArchivesApi { return instance.archives }
    static var bookmarks: BookmarksApi { return instance.bookmarks }
    static var notes: NotesApi { return instance.notes }
    static var resources: ResourcesApi { return instance.resources }
    static var tags: TagsApi { return instance.tags }
    static var users: UsersApi { return instance.users }
}
