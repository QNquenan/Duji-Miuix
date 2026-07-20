package com.quenan.duji

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quenan.duji.data.ReleaseNoteEntry
import com.quenan.duji.data.ReleaseNotesRepository
import com.quenan.duji.data.AppUpdateManager
import com.quenan.duji.data.settings.SettingsRepository
import com.quenan.duji.data.reminder.DayReminderScheduler
import com.quenan.duji.ui.component.LocalSystemNotice
import com.quenan.duji.ui.component.SystemNoticeHost
import com.quenan.duji.ui.component.UpdateAvailableDialog
import com.quenan.duji.ui.component.fbutton.FloatingBottomBar
import com.quenan.duji.ui.component.fbutton.FloatingBottomBarItem
import com.quenan.duji.ui.component.rememberSystemNoticeHostState
import com.quenan.duji.ui.screen.CheckInScreen
import com.quenan.duji.ui.screen.MyItemsScreen
import com.quenan.duji.ui.screen.SettingsScreen
import com.quenan.duji.ui.screen.ThoseDaysScreen
import com.quenan.duji.ui.theme.DuJiTheme
import com.quenan.duji.ui.theme.LocalEnableBlur
import com.quenan.duji.widget.WidgetIntentFactory
import com.quenan.duji.widget.WidgetTargetType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.All
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Years
import top.yukonga.miuix.kmp.shader.isRenderEffectSupported
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.time.LocalDate
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onResume() {
        super.onResume()
        AppUpdateManager.installPending(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsRepository = SettingsRepository(applicationContext)
        DayReminderScheduler.createNotificationChannel(applicationContext)
        setContent {
            val settingsValue by settingsRepository.observeSettings()
                .map<com.quenan.duji.data.settings.SettingsData, com.quenan.duji.data.settings.SettingsData?> { it }
                .collectAsStateWithLifecycle(initialValue = null)
            val settings = settingsValue ?: return@setContent
            val latestVersion = remember { ReleaseNotesRepository.latestVersionName(applicationContext) }
            val startPage = intent?.getIntExtra(WidgetIntentFactory.EXTRA_START_PAGE, 0) ?: 0
            val startTargetType = intent?.getStringExtra(WidgetIntentFactory.EXTRA_TARGET_TYPE)
            val startTargetId = intent?.getLongExtra(WidgetIntentFactory.EXTRA_TARGET_ID, -1L) ?: -1L
            var widgetTargetConsumed by remember { mutableStateOf(false) }
            val pagerState = rememberPagerState(initialPage = startPage, pageCount = { bottomNavItems.size })
            val duJiPagerState = rememberDuJiPagerState(pagerState)
            val noticeHostState = rememberSystemNoticeHostState()
            val coroutineScope = rememberCoroutineScope()
            var automaticLatestRelease by remember { mutableStateOf<ReleaseNoteEntry?>(null) }
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) { }

            LaunchedEffect(Unit) {
                DayReminderScheduler.rescheduleAll(this@MainActivity)
            }

            LaunchedEffect(settings.notificationPermissionRequested) {
                if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !settings.notificationPermissionRequested &&
                    ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    settingsRepository.markNotificationPermissionRequested()
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            LaunchedEffect(settings.autoCheckUpdates, latestVersion) {
                if (
                    settings.autoCheckUpdates &&
                    settingsRepository.consumeAutoUpdateCheckForToday(LocalDate.now().toString())
                ) {
                    val release = runCatching { ReleaseNotesRepository.fetchLatestReleaseNote() }.getOrNull()
                    if (
                        release != null &&
                        ReleaseNotesRepository.isVersionNewer(release.title, latestVersion)
                    ) {
                        automaticLatestRelease = release
                    }
                }
            }

            DuJiTheme(colorModeIndex = settings.colorModeIndex) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PredictiveBackHandler(enabled = settings.predictiveBackEnabled) { progress ->
                        progress.collect { }
                        if (duJiPagerState.handleBack()) {
                            return@PredictiveBackHandler
                        }
                        finish()
                    }
                }
                val settledPage = pagerState.settledPage
                val contentReady = rememberMainContentReady()
                LaunchedEffect(settledPage) { duJiPagerState.syncPage() }
                val currentPage = pagerState.currentPage
                LaunchedEffect(currentPage) { duJiPagerState.syncPage() }
                val isBlurEnabled = LocalEnableBlur.current && isRenderEffectSupported()
                val surfaceColor = MiuixTheme.colorScheme.surface
                val backdrop = rememberLayerBackdrop {
                    drawRect(surfaceColor)
                    drawContent()
                }

                CompositionLocalProvider(LocalSystemNotice provides noticeHostState) {
                    Scaffold(
                        bottomBar = {
                            Box(
                                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                                contentAlignment = androidx.compose.ui.Alignment.BottomCenter,
                            ) {
                                FloatingBottomBar(
                                    modifier = androidx.compose.ui.Modifier
                                        .clickable(
                                            interactionSource = null,
                                            indication = null,
                                            onClick = {},
                                        )
                                        .padding(
                                            bottom = 12.dp + WindowInsets.navigationBars
                                                .asPaddingValues()
                                                .calculateBottomPadding()
                                        ),
                                    selectedIndex = { duJiPagerState.selectedPage },
                                    onSelected = { duJiPagerState.animateToPage(it) },
                                    backdrop = backdrop,
                                    tabsCount = bottomNavItems.size,
                                    isBlurEnabled = isBlurEnabled,
                                ) {
                                    bottomNavItems.forEachIndexed { index, item ->
                                        FloatingBottomBarItem(
                                            onClick = { duJiPagerState.animateToPage(index) },
                                            modifier = androidx.compose.ui.Modifier.defaultMinSize(minWidth = 76.dp),
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.label,
                                            )
                                            Text(
                                                text = item.label,
                                                fontSize = 11.sp,
                                                lineHeight = 14.sp,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Visible,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        val bottomPadding = innerPadding.calculateBottomPadding()
                        HorizontalPager(
                            modifier = if (isBlurEnabled) {
                                androidx.compose.ui.Modifier.layerBackdrop(backdrop)
                            } else {
                                androidx.compose.ui.Modifier
                            },
                            state = pagerState,
                            beyondViewportPageCount = if (contentReady) bottomNavItems.lastIndex else 0,
                        ) { page ->
                            val isCurrentPage = page == settledPage
                            when (page) {
                                0 -> if (isCurrentPage || contentReady) {
                                    MyItemsScreen(
                                        contentPadding = PaddingValues(bottom = bottomPadding),
                                        openItemId = if (!widgetTargetConsumed && startTargetType == WidgetTargetType.ITEM.name && startTargetId >= 0) startTargetId else null,
                                        onOpenItemConsumed = { widgetTargetConsumed = true },
                                    )
                                }
                                1 -> if (isCurrentPage || contentReady) {
                                    ThoseDaysScreen(
                                        contentPadding = PaddingValues(bottom = bottomPadding),
                                        openDayId = if (!widgetTargetConsumed && startTargetType == WidgetTargetType.DAY.name && startTargetId >= 0) startTargetId else null,
                                        onOpenDayConsumed = { widgetTargetConsumed = true },
                                    )
                                }
                                2 -> if (isCurrentPage || contentReady) {
                                    CheckInScreen(
                                        contentPadding = PaddingValues(bottom = bottomPadding),
                                    )
                                }
                                3 -> if (isCurrentPage || contentReady) {
                                    SettingsScreen(
                                        contentPadding = PaddingValues(bottom = bottomPadding),
                                        versionName = latestVersion,
                                        selectedColorModeIndex = settings.colorModeIndex,
                                        predictiveBackEnabled = settings.predictiveBackEnabled,
                                        autoCheckUpdates = settings.autoCheckUpdates,
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
                                        onAutoCheckUpdatesChange = { enabled ->
                                            coroutineScope.launch {
                                                settingsRepository.updateAutoCheckUpdates(enabled)
                                            }
                                        },
                                    )
                                }
                            }
                        }
                        SystemNoticeHost(hostState = noticeHostState)
                        automaticLatestRelease?.let { release ->
                            UpdateAvailableDialog(
                                release = release,
                                onDismiss = { automaticLatestRelease = null },
                                onUpdate = { targetRelease ->
                                    automaticLatestRelease = null
                                    coroutineScope.launch {
                                        runCatching {
                                            AppUpdateManager.enqueue(this@MainActivity, targetRelease.title)
                                        }.onSuccess {
                                            noticeHostState.show(coroutineScope, "已开始下载更新")
                                        }.onFailure {
                                            noticeHostState.show(coroutineScope, "下载更新失败")
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

}

@Composable
private fun rememberMainContentReady(): Boolean {
    var contentReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withFrameNanos { }
        contentReady = true
    }

    return contentReady
}

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem("我的物品", MiuixIcons.All),
    BottomNavItem("那些日子", MiuixIcons.Years),
    BottomNavItem("打卡", MiuixIcons.Ok),
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
