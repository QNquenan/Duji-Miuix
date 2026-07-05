package com.quenan.duji.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

class SystemNoticeHostState {
    var currentMessage by mutableStateOf<String?>(null)
        private set

    private var hideJob: Job? = null

    fun show(scope: kotlinx.coroutines.CoroutineScope, message: String, durationMillis: Long = 2000L) {
        hideJob?.cancel()
        currentMessage = message
        hideJob = scope.launch {
            delay(durationMillis)
            currentMessage = null
        }
    }
}

val LocalSystemNotice = compositionLocalOf<SystemNoticeHostState> {
    error("SystemNoticeHostState not provided")
}

@Composable
fun rememberSystemNoticeHostState(): SystemNoticeHostState = remember { SystemNoticeHostState() }

@Composable
fun SystemNoticeHost(
    hostState: SystemNoticeHostState,
    modifier: Modifier = Modifier,
) {
    val message = hostState.currentMessage
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        AnimatedVisibility(
            visible = message != null,
            enter = fadeIn() + slideInVertically { -it / 2 },
            exit = fadeOut() + slideOutVertically { -it / 2 },
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = Color(0xF2121212),
                        shape = RoundedCornerShape(16.dp),
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(
                    text = message.orEmpty(),
                    color = Color.White,
                    style = MiuixTheme.textStyles.main,
                )
            }
        }
    }
}

@Composable
fun rememberNoticeAction(): (String) -> Unit {
    val hostState = LocalSystemNotice.current
    val scope = rememberCoroutineScope()
    return remember(hostState, scope) {
        { message -> hostState.show(scope, message) }
    }
}
