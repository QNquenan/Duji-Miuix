package com.quenan.duji

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import com.quenan.duji.data.ReleaseNotesRepository
import com.quenan.duji.data.settings.SettingsRepository
import com.quenan.duji.ui.component.LocalSystemNotice
import com.quenan.duji.ui.component.SystemNoticeHost
import com.quenan.duji.ui.component.rememberSystemNoticeHostState
import com.quenan.duji.ui.screen.MyItemsScreen
import com.quenan.duji.ui.screen.SettingsScreen
import com.quenan.duji.ui.screen.ThoseDaysScreen
import com.quenan.duji.ui.theme.DuJiTheme
import com.quenan.duji.widget.WidgetIntentFactory
import com.quenan.duji.widget.WidgetTargetType
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
        val settingsRepository = SettingsRepository(applicationContext)
        setContent {
            val settings by settingsRepository.observeSettings().collectAsState(
                initial = com.quenan.duji.data.settings.SettingsData()
            )
            val latestVersion = remember { ReleaseNotesRepository.latestVersionName(applicationContext) }
            val startPage = intent?.getIntExtra(WidgetIntentFactory.EXTRA_START_PAGE, 0) ?: 0
            val startTargetType = intent?.getStringExtra(WidgetIntentFactory.EXTRA_TARGET_TYPE)
            val startTargetId = intent?.getLongExtra(WidgetIntentFactory.EXTRA_TARGET_ID, -1L) ?: -1L
            val pagerState = rememberPagerState(initialPage = startPage, pageCount = { bottomNavItems.size })
            val duJiPagerState = rememberDuJiPagerState(pagerState)
            val noticeHostState = rememberSystemNoticeHostState()
            val coroutineScope = rememberCoroutineScope()

            DuJiTheme(colorModeIndex = settings.colorModeIndex) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PredictiveBackHandler(enabled = settings.predictiveBackEnabled) {
                        if (duJiPagerState.handleBack()) {
                            return@PredictiveBackHandler
                        }
                        finish()
                    }
                }
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
                                        modifier = androidx.compose.ui.Modifier.weight(1f),
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
                                0 -> MyItemsScreen(
                                    contentPadding = PaddingValues(bottom = bottomPadding),
                                    openItemId = if (startTargetType == WidgetTargetType.ITEM.name && startTargetId >= 0) startTargetId else null,
                                )
                                1 -> ThoseDaysScreen(
                                    contentPadding = PaddingValues(bottom = bottomPadding),
                                    openDayId = if (startTargetType == WidgetTargetType.DAY.name && startTargetId >= 0) startTargetId else null,
                                )
                                2 -> SettingsScreen(
                                    versionName = latestVersion,
                                    selectedColorModeIndex = settings.colorModeIndex,
                                    predictiveBackEnabled = settings.predictiveBackEnabled,
                                    onSelectedColorModeChange = { newIndex ->
                                        coroutineScope.launch {
                                            settingsRepository.updateColorMode(newIndex)
                                        }
                                    },
                                    onPredictiveBackEnabledChange = { enabled ->
                                        coroutineScope.launch {
                                            settingsRepository.updatePredictiveBackEnabled(enabled)
                                        }
                                    },
                                )
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
    val icon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem("我的物品", MiuixIcons.All),
    BottomNavItem("那些日子", MiuixIcons.Years),
    BottomNavItem("设置", MiuixIcons.Settings),
)

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

    fun handleBack(): Boolean {
        if (selectedPage == 0) return false
        animateToPage(0)
        return true
    }
}

@Composable
private fun rememberDuJiPagerState(
    pagerState: PagerState,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): DuJiPagerState = remember(pagerState, coroutineScope) { DuJiPagerState(pagerState, coroutineScope) }
