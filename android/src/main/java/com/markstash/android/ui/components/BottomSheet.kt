package com.markstash.android.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animatedFloat
import androidx.compose.foundation.Box
import androidx.compose.foundation.animation.FlingConfig
import androidx.compose.foundation.animation.fling
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Stack
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.launchInComposition
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.gesture.scrollorientationlocking.Orientation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.onPositioned
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.unit.dp

@Composable
fun BottomSheet(
    onClose: () -> Unit,
    children: @Composable () -> Unit,
) {
    val densityAmbient = DensityAmbient.current

    var height by remember { mutableStateOf(0) }
    val dragOffset = animatedFloat(320f)
    val dragPercent = (dragOffset.value * densityAmbient.density) / height.toFloat()

    launchInComposition {
        dragOffset.animateTo(0f)
    }

    Stack {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f * (1f - dragPercent.coerceIn(0f, 1f))))
                .clickable(indication = null) {
                    dragOffset.animateTo(height.toFloat() / densityAmbient.density) { _, _ -> onClose() }
                }
        )

        Surface(
            shape = RoundedCornerShape(topLeft = 8.dp, topRight = 8.dp),
            color = Color.White,
            elevation = 4.dp,
            modifier = Modifier
                .gravity(Alignment.BottomStart)
                .fillMaxWidth()
                .offset(y = dragOffset.value.dp)
                .draggable(
                    orientation = Orientation.Vertical,
                    onDrag = { dragOffset.snapTo((dragOffset.value + it / density).coerceAtLeast(0f)) },
                    onDragStopped = { velocity ->
                        val config = FlingConfig(listOf(0f, height.toFloat()))
                        if (velocity > 0.7f) {
                            dragOffset.fling(velocity, config) { _, endValue, _ ->
                                if (endValue > 0f) {
                                    onClose()
                                }
                            }
                        } else if (dragOffset.value * densityAmbient.density > height / 2) {
                            dragOffset.animateTo(height.toFloat()/ densityAmbient.density) { _, _ ->
                                onClose()
                            }
                        } else {
                            dragOffset.animateTo(0f)
                        }
                    }
                )
                .onPositioned { height = it.size.height }
                .animateContentSize()
        ) {
            children()
        }
    }
}
