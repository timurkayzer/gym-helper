package com.gymhelper.app.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.gymhelper.app.ui.theme.SessionStopRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val HOLD_MS = 3000L

@Composable
fun HoldToStopButton(
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
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
                .pointerInput(onStop) {
                    detectTapGestures(
                        onPress = {
                            var completed = false
                            val job = scope.launch {
                                val started = System.currentTimeMillis()
                                while (isActive) {
                                    delay(50)
                                    val elapsed = System.currentTimeMillis() - started
                                    holdProgress = (elapsed / HOLD_MS.toFloat()).coerceIn(0f, 1f)
                                    if (elapsed >= HOLD_MS) {
                                        completed = true
                                        holdProgress = 1f
                                        onStop()
                                        break
                                    }
                                }
                            }
                            tryAwaitRelease()
                            job.cancel()
                            if (!completed) {
                                holdProgress = 0f
                            }
                        },
                    )
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
