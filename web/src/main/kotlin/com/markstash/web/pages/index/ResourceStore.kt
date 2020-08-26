package com.markstash.web.pages.index

import com.markstash.api.models.Resource
import com.markstash.web.Store

data class ResourceStoreState(
    val resources: List<Resource> = emptyList()
)

object ResourceStore : Store<ResourceStoreState>(ResourceStoreState()) {
    fun clearResources() {
        state = state.copy(resources = emptyList())
    }

    fun setResources(resources: List<Resource>) {
        state = state.copy(resources = resources)
    }

    fun updateResource(newResource: Resource) {
        val newResources = state.resources.map { oldResource ->
            if (oldResource.type == newResource.type && oldResource.id == newResource.id) newResource else oldResource
        }
        state = state.copy(resources = newResources)
    }

    fun updateResourceDate(newResource: Resource) {
        val newResources = state.resources.map { oldResource ->
            if (oldResource.type == newResource.type && oldResource.id == newResource.id) {
                oldResource.copy(updatedAt = newResource.updatedAt)
            } else {
                oldResource
            }
        }
        state = state.copy(resources = newResources)
    }

    fun addResource(newResource: Resource) {
        state = state.copy(resources = listOf(newResource) + state.resources)
    }

    fun deleteResource(resource: Resource): Resource? {
        val index = state.resources.indexOfFirst { it.type == resource.type && it.id == resource.id }
        val newResources = state.resources.filterNot { it.type == resource.type && it.id == resource.id }
        state = state.copy(resources = newResources)
        return newResources.getOrNull(index) ?: newResources.getOrNull(index - 1)
    }
}
