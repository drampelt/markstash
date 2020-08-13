package com.markstash.android.ui.main

import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.markstash.android.R
import com.markstash.api.models.Resource

@Composable
fun ResourceList(resources: List<Resource>) {
    LazyColumnForIndexed(resources) { index, resource ->
        ResourceRow(resource)

        if (index < resources.size) {
            Divider()
        }
    }
}

@Composable
fun ResourceRow(resource: Resource) {
    val title = remember(resource) { resource.title.takeUnless { it.isNullOrBlank() } }
    val excerpt = remember(resource) { resource.excerpt.takeUnless { it.isNullOrBlank() } }

    Row(
        verticalGravity = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {  }
            .padding(16.dp)
    ) {
        Icon(
            if (resource.type == Resource.Type.BOOKMARK) Icons.Default.Star else Icons.Default.Edit,
            modifier = Modifier.padding(end = 16.dp),
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title ?: stringResource(R.string.resource_label_untitled),
                style = MaterialTheme.typography.h6,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.drawOpacity(if (title == null) 0.7f else 1.0f),
            )

            Text(
                text = excerpt ?: stringResource(R.string.resource_label_no_description),
                style = MaterialTheme.typography.body2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.drawOpacity(if (excerpt == null) 0.7f else 1.0f),
            )
        }
    }
}
