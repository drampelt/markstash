package com.markstash.android.ui.bookmark.receive

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Icon
import androidx.compose.foundation.ScrollableRow
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.getValue
import androidx.compose.runtime.launchInComposition
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.util.PatternsCompat
import com.markstash.android.KoinContext
import com.markstash.android.R
import com.markstash.android.inject
import com.markstash.android.ui.components.BottomSheet
import com.markstash.api.bookmarks.CreateRequest
import com.markstash.api.models.Bookmark
import com.markstash.client.api.BookmarksApi
import kotlinx.coroutines.delay
import org.koin.android.ext.android.getKoin

class ReceiveBookmarkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        super.onCreate(savedInstanceState)

        val url = intent.getStringExtra(Intent.EXTRA_TEXT)
        val title = intent.getStringExtra(Intent.EXTRA_SUBJECT)

        setContent {
            Providers(KoinContext provides getKoin()) {
                MaterialTheme {
                    ReceiveBookmarkScreen(
                        url = url,
                        title = title,
                        onFinish = {
                            finish()
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ReceiveBookmarkScreen(url: String?, title: String?, onFinish: () -> Unit) {
    val bookmarksApi: BookmarksApi by inject()

    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var bookmark by remember { mutableStateOf<Bookmark?>(null) }

    val notUrlErrorMessage = stringResource(R.string.bookmark_label_not_url)

    launchInComposition {
        if (url.isNullOrBlank() || !PatternsCompat.WEB_URL.matcher(url).matches()) {
            error = notUrlErrorMessage
            isLoading = false
            delay(2500)
            onFinish()
            return@launchInComposition
        }

        try {
            val saveStart = System.currentTimeMillis()
            val newBookmark = bookmarksApi.create(CreateRequest(title ?: "Untitled", url))
            val saveDuration = System.currentTimeMillis() - saveStart
            if (saveDuration < 500) delay(500 - saveDuration)
            bookmark = newBookmark
            isLoading = false
            error = null
        } catch (e: Throwable) {
            error = e.message
            delay(2500)
            onFinish()
        }
    }

    BottomSheet(
        onClose = onFinish
    ) {
        ReceiveBookmarkContent(
            isLoading = isLoading,
            title = title,
            error = error,
            bookmark = bookmark
        )
    }
}

@Composable
fun ReceiveBookmarkContent(isLoading: Boolean, title: String?, error: String?, bookmark: Bookmark?) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row {
            Icon(
                asset = if (isLoading) Icons.Default.Refresh else Icons.Default.Star,
                modifier = Modifier.preferredSize(48.dp)
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (error != null) stringResource(R.string.bookmark_label_not_saved) else title ?: stringResource(R.string.resource_label_untitled),
                    style = MaterialTheme.typography.h6,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = error ?: stringResource(if (isLoading) R.string.bookmark_label_saving else R.string.bookmark_label_saved)
                )

                if (bookmark != null) {
                    Spacer(Modifier.height(16.dp))

                    if (bookmark.tags.isEmpty()) {
                        Text(stringResource(R.string.resource_label_no_tags))
                    } else {
                        ScrollableRow {
                            bookmark.tags.forEach { tag ->
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.caption,
                                    modifier = Modifier
                                        .background(Color.LightGray, RoundedCornerShape(50))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    IconButton(onClick = {}, modifier = Modifier.gravity(Alignment.End)) {
                        Icon(Icons.Default.Delete)
                    }
                }
            }
        }
    }
}
