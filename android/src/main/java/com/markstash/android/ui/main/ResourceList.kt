package com.markstash.android.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.request.ImageRequest
import com.markstash.android.R
import com.markstash.android.ui.components.Tag
import com.markstash.api.models.Resource
import com.markstash.client.util.formatRelativeDisplay
import com.markstash.client.util.parseDomainFromUrl
import com.markstash.mobile.Session
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.get
import kotlin.time.ExperimentalTime
import kotlin.time.days

@Composable
fun ResourceList(resources: List<Resource>) {
    LazyColumn {
        itemsIndexed(resources) { index, resource ->
            ResourceRow(resource)

            if (index < resources.size) {
                Divider()
            }
        }
    }
}

@Composable
fun ResourceRowIcon(resource: Resource, modifier: Modifier = Modifier) {
    val session = get<Session>()
    val context = AmbientContext.current

    val coilRequest: ImageRequest? = remember(resource) {
        if (resource.iconArchiveId == null) return@remember null
        ImageRequest.Builder(context)
            .data("${session.baseUrl}/bookmarks/${resource.id}/archives/${resource.iconArchiveId}")
            .addHeader("Authorization", "Bearer ${session.authToken}")
            .build()
    }

    if (coilRequest == null) {
        Icon(
            if (resource.type == Resource.Type.BOOKMARK) Icons.Default.Star else Icons.Default.Edit,
            modifier = modifier,
        )
    } else {
        CoilImage(request = coilRequest, modifier = modifier)
    }
}

@Composable
fun ResourceRow(resource: Resource) {
    val title = remember(resource) { resource.title.takeUnless { it.isNullOrBlank() } }
    val excerpt = remember(resource) { resource.excerpt.takeUnless { it.isNullOrBlank() } }
    val date = remember(resource) {
        (if (resource.type == Resource.Type.BOOKMARK) resource.createdAt else resource.updatedAt)
            .toLocalDateTime(TimeZone.currentSystemDefault())
    }
    val domain = remember(resource) { resource.url?.parseDomainFromUrl() }
    val tags = remember(resource) { resource.tags.toList() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            ResourceRowIcon(resource = resource, modifier = Modifier.size(16.dp))

            Spacer(modifier = Modifier.width(4.dp))

            if (resource.type == Resource.Type.BOOKMARK && domain != null) {
                Text(
                    text = domain,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.alpha(0.7f),
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "•",
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.alpha(0.7f),
                )

                Spacer(modifier = Modifier.width(4.dp))

            }

            Text(
                text = date.formatRelativeDisplay(),
                style = MaterialTheme.typography.caption,
                modifier = Modifier.alpha(0.7f),
            )
        }

        Text(
            text = title ?: stringResource(R.string.resource_label_untitled),
            style = MaterialTheme.typography.h6,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .alpha(if (title == null) 0.7f else 1.0f)
                .padding(horizontal = 16.dp),
        )

        Text(
            text = excerpt ?: stringResource(R.string.resource_label_no_description),
            style = MaterialTheme.typography.body2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .alpha(if (excerpt == null) 0.7f else 1.0f)
                .padding(horizontal = 16.dp),
        )

        if (tags.isEmpty()) {
            Text(
                text = stringResource(R.string.resource_label_no_tags),
                style = MaterialTheme.typography.body2,
                modifier = Modifier
                    .alpha(0.7f)
                    .padding(horizontal = 16.dp),
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(contentPadding = PaddingValues(start = 16.dp, end = 16.dp)) {
                items(items = tags, itemContent = { tag ->
                    Tag(tag = tag)
                    Spacer(modifier = Modifier.width(8.dp))
                })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview
@Composable
fun ResourceRowBookmarkPreview() {
    MaterialTheme {
        Surface(Modifier.background(MaterialTheme.colors.background)) {
            ResourceRow(resource = Resource(
                type = Resource.Type.BOOKMARK,
                id = 1L,
                title = "Introduction to Programming",
                excerpt = "This is probably the best introduction to programming you could ever read.",
                tags = setOf("blog", "programming", "introduction", "beginner", "python"),
                url = "https://example.com",
                iconArchiveId = null,
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now(),
            ))
        }
    }
}

@OptIn(ExperimentalTime::class)
@Preview
@Composable
fun ResourceRowNotePreview() {
    MaterialTheme {
        Surface(Modifier.background(MaterialTheme.colors.background)) {
            ResourceRow(resource = Resource(
                type = Resource.Type.NOTE,
                id = 2L,
                title = "Daily Journal",
                excerpt = "Today I made some awesome previews in Compose, it's really nice",
                tags = emptySet(),
                url = null,
                iconArchiveId = null,
                createdAt = Clock.System.now().minus(5.days),
                updatedAt = Clock.System.now().minus(3.days),
            ))
        }
    }
}
