package com.gymhelper.app.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.gymhelper.app.ui.theme.SessionStopRed
import kotlinx.coroutines.withTimeoutOrNull

private const val HOLD_MS = 3000L
private const val POLL_MS = 32L

@Composable
fun HoldToStopButton(
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val onStopUpdated by rememberUpdatedState(onStop)
    var holdProgress by remember { mutableFloatStateOf(0f) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (holdProgress > 0f) {
            LinearProgressIndicator(
                progress = { holdProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                color = SessionStopRed,
            )
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val pointerId = down.id
                        val startedAt = System.currentTimeMillis()
                        try {
                            while (true) {
                                val elapsed = System.currentTimeMillis() - startedAt
                                holdProgress = (elapsed / HOLD_MS.toFloat()).coerceIn(0f, 1f)

                                if (elapsed >= HOLD_MS) {
                                    holdProgress = 0f
                                    onStopUpdated()
                                    return@awaitEachGesture
                                }

                                val releasedEarly = withTimeoutOrNull(POLL_MS) {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val stillPressed = event.changes.any {
                                            it.id == pointerId && it.pressed
                                        }
                                        if (!stillPressed) {
                                            return@withTimeoutOrNull true
                                        }
                                    }
                                }
                                if (releasedEarly == true) {
                                    return@awaitEachGesture
                                }
                            }
                        } finally {
                            holdProgress = 0f
                        }
                    }
                },
            shape = RoundedCornerShape(12.dp),
            color = SessionStopRed,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("Hold 3s to stop", color = Color.White)
            }
        }
    }
}
