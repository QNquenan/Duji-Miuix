package com.quenan.duji

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.CompositionLocalProvider
import com.quenan.duji.ui.component.LocalSystemNotice
import com.quenan.duji.ui.component.SystemNoticeHost
import com.quenan.duji.ui.component.rememberSystemNoticeHostState
import com.quenan.duji.ui.screen.MyItemsScreen
import com.quenan.duji.ui.screen.SettingsScreen
import com.quenan.duji.ui.screen.ThoseDaysScreen
import com.quenan.duji.ui.theme.DuJiTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.All
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Years
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DuJiTheme {
                val pagerState = rememberPagerState(pageCount = { bottomNavItems.size })
                val duJiPagerState = rememberDuJiPagerState(pagerState)
                val noticeHostState = rememberSystemNoticeHostState()

                val settledPage = pagerState.settledPage
                LaunchedEffect(settledPage) { duJiPagerState.syncPage() }
                val currentPage = pagerState.currentPage
                LaunchedEffect(currentPage) { duJiPagerState.syncPage() }

                CompositionLocalProvider(LocalSystemNotice provides noticeHostState) {
                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                bottomNavItems.forEachIndexed { index, item ->
                                    NavigationBarItem(
                                        modifier = Modifier.weight(1f),
                                        icon = item.icon,
                                        label = item.label,
                                        selected = duJiPagerState.selectedPage == index,
                                        onClick = {
                                            duJiPagerState.animateToPage(index)
                                        }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        val bottomPadding = innerPadding.calculateBottomPadding()
                        HorizontalPager(
                            state = pagerState,
                        ) { page ->
                            when (page) {
                                0 -> MyItemsScreen(contentPadding = PaddingValues(bottom = bottomPadding))
                                1 -> ThoseDaysScreen()
                                2 -> SettingsScreen()
                            }
                        }
                        SystemNoticeHost(hostState = noticeHostState)
                    }
                }
            }
        }
    }
}

private data class BottomNavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem("我的物品", MiuixIcons.All),
    BottomNavItem("那些日子", MiuixIcons.Years),
    BottomNavItem("设置", MiuixIcons.Settings),
)

// ═══════════════════════════════════════════
//  页面状态管理（来自 KernelSU MainPagerState）
// ═══════════════════════════════════════════
class DuJiPagerState(
    val pagerState: PagerState,
    private val coroutineScope: CoroutineScope
) {
    var selectedPage by mutableIntStateOf(pagerState.currentPage)
        private set
    var isNavigating by mutableStateOf(false)
        private set
    private var navJob: Job? = null

    fun animateToPage(targetIndex: Int) {
        if (targetIndex == selectedPage) return
        navJob?.cancel()
        selectedPage = targetIndex
        isNavigating = true

        val distance = abs(targetIndex - pagerState.currentPage).coerceAtLeast(2)
        val duration = 100 * distance + 100
        val layoutInfo = pagerState.layoutInfo
        val pageSize = layoutInfo.pageSize + layoutInfo.pageSpacing
        val currentDistanceInPages =
            targetIndex - pagerState.currentPage - pagerState.currentPageOffsetFraction
        val scrollPixels = currentDistanceInPages * pageSize

        navJob = coroutineScope.launch {
            val myJob = coroutineContext.job
            try {
                pagerState.animateScrollBy(
                    value = scrollPixels,
                    animationSpec = tween(easing = EaseInOut, durationMillis = duration)
                )
            } finally {
                if (navJob == myJob) {
                    isNavigating = false
                    if (pagerState.currentPage != targetIndex) {
                        selectedPage = pagerState.currentPage
                    }
                }
            }
        }
    }

    fun syncPage() {
        if (!isNavigating && selectedPage != pagerState.currentPage) {
            selectedPage = pagerState.currentPage
        }
    }
}

@Composable
private fun rememberDuJiPagerState(
    pagerState: PagerState,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): DuJiPagerState = remember(pagerState, coroutineScope) { DuJiPagerState(pagerState, coroutineScope) }
