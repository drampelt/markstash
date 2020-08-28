package com.markstash.android.ui.components

import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview

@Composable
fun Tag(
    tag: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = tag,
        style = MaterialTheme.typography.body2,
        modifier = Modifier
            .background(Color.LightGray, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .then(modifier)
    )
}

@Preview
@Composable
fun TagPreview() {
    Tag("blog")
}
