package com.quenan.duji.ui.screen

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.quenan.duji.ReleaseNotesActivity
import com.quenan.duji.data.backup.APP_BACKUP_VERSION
import com.quenan.duji.data.backup.AppBackup
import com.quenan.duji.data.backup.parseAppBackup
import com.quenan.duji.data.backup.toJsonString
import com.quenan.duji.data.checkin.CheckInRepository
import com.quenan.duji.data.day.DayRepository
import com.quenan.duji.data.item.ItemRepository
import com.quenan.duji.data.AppUpdateManager
import com.quenan.duji.data.ReleaseNoteEntry
import com.quenan.duji.data.ReleaseNotesRepository
import com.quenan.duji.ui.component.rememberNoticeAction
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Background
import top.yukonga.miuix.kmp.icon.extended.Forward
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.MoveFile
import top.yukonga.miuix.kmp.icon.extended.NotesFill
import top.yukonga.miuix.kmp.icon.extended.Remove
import top.yukonga.miuix.kmp.icon.extended.Reset
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    versionName: String,
    selectedColorModeIndex: Int,
    predictiveBackEnabled: Boolean,
    onSelectedColorModeChange: (Int) -> Unit,
    onPredictiveBackEnabledChange: (Boolean) -> Unit,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val context = LocalContext.current
    val appContext = context.applicationContext
    val uriHandler = LocalUriHandler.current
    val showNotice = rememberNoticeAction()
    val colorModeOptions = remember { listOf("跟随系统", "浅色", "深色") }
    val coroutineScope = rememberCoroutineScope()
    val itemRepository = remember(appContext) { ItemRepository(appContext) }
    val dayRepository = remember(appContext) { DayRepository(appContext) }
    val checkInRepository = remember(appContext) { CheckInRepository(appContext) }
    val timestamp = remember {
        SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.getDefault()).format(Date())
    }
    val exportFileName = remember(versionName, timestamp) {
        val safeVersion = versionName.replace('/', '-').replace(':', '-')
        "DuJi_${safeVersion}_${timestamp}.json"
    }
    var latestRelease by remember { mutableStateOf<ReleaseNoteEntry?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            runCatching {
                val backup = AppBackup(
                    version = APP_BACKUP_VERSION,
                    items = itemRepository.getAllItems(),
                    days = dayRepository.getAllDays(),
                    checkInItems = checkInRepository.getAllItems(),
                    checkInRecords = checkInRepository.getAllRecords(),
                )
                val json = backup.toJsonString()
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(json.toByteArray(StandardCharsets.UTF_8))
                    outputStream.flush()
                } ?: error("无法打开导出位置")
                BackupExportResult(
                    itemCount = backup.items.size,
                    dayCount = backup.days.size,
                    checkInItemCount = backup.checkInItems.size,
                    checkInRecordCount = backup.checkInRecords.size,
                    fileName = exportFileName,
                )
            }.onSuccess { result ->
                showNotice(
                    "导出成功：${result.fileName}（备份 v${APP_BACKUP_VERSION}，物品 ${result.itemCount} 条，日子 ${result.dayCount} 条，打卡项 ${result.checkInItemCount} 条，打卡记录 ${result.checkInRecordCount} 条）"
                )
            }.onFailure { throwable ->
                showNotice("导出失败：${throwable.message ?: "未知错误"}")
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            runCatching {
                val raw = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: error("无法读取导入文件")
                val backup = parseAppBackup(raw)
                itemRepository.importItems(backup.items)
                dayRepository.importDays(backup.days)
                checkInRepository.importData(backup.checkInItems, backup.checkInRecords)
                BackupImportResult(
                    version = backup.version,
                    itemCount = backup.items.size,
                    dayCount = backup.days.size,
                    checkInItemCount = backup.checkInItems.size,
                    checkInRecordCount = backup.checkInRecords.size,
                )
            }.onSuccess { result ->
                showNotice(
                    "导入成功：备份 v${result.version}，物品 ${result.itemCount} 条，日子 ${result.dayCount} 条，打卡项 ${result.checkInItemCount} 条，打卡记录 ${result.checkInRecordCount} 条"
                )
            }.onFailure { throwable ->
                showNotice("导入失败：${throwable.message ?: "未知错误"}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "设置",
                scrollBehavior = scrollBehavior,
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SmallTitle(text = "外观", insideMargin = PaddingValues(top = 16.dp, bottom = 2.dp, start = 16.dp), modifier = Modifier.fillMaxWidth())
            Card(
                modifier = Modifier.fillMaxWidth(),
                insideMargin = PaddingValues(0.dp),
                colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    WindowDropdownPreference(
                        title = "颜色模式",
                        items = colorModeOptions,
                        selectedIndex = selectedColorModeIndex,
                        onSelectedIndexChange = onSelectedColorModeChange,
                        insideMargin = PaddingValues(horizontal = 16.dp),
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Background,
                                modifier = Modifier.padding(end = 6.dp),
                                contentDescription = "颜色模式"
                            )
                        },
                    )
                }
            }

            SmallTitle(text = "备份", insideMargin = PaddingValues(top = 16.dp, bottom = 2.dp, start = 16.dp), modifier = Modifier.fillMaxWidth())
            Card(
                modifier = Modifier.fillMaxWidth(),
                insideMargin = PaddingValues(0.dp),
                colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ArrowPreference(
                        title = "导出",
                        summary = "导出 JSON 到本地",
                        onClick = { exportLauncher.launch(exportFileName) },
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.MoveFile,
                                modifier = Modifier.padding(end = 6.dp),
                                contentDescription = "导出"
                            )
                        },
                    )
                    ArrowPreference(
                        title = "导入",
                        summary = "从本地选择 JSON 合并导入",
                        onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Reset,
                                modifier = Modifier.padding(end = 6.dp),
                                contentDescription = "导入"
                            )
                        },
                    )
                }
            }

            SmallTitle(text = "关于", insideMargin = PaddingValues(top = 16.dp, bottom = 2.dp, start = 16.dp), modifier = Modifier.fillMaxWidth())
            Card(
                modifier = Modifier.fillMaxWidth(),
                insideMargin = PaddingValues(0.dp),
                colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ArrowPreference(
                        title = "源代码",
                        summary = "https://github.com/QNquenan/DuJi",
                        onClick = { uriHandler.openUri("https://github.com/QNquenan/DuJi") },
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Forward,
                                modifier = Modifier.padding(end = 6.dp),
                                contentDescription = "Forward"
                            )
                        },
                    )
                    ArrowPreference(
                        title = "Miuix（Apache-2.0）",
                        summary = "compose-miuix-ui",
                        onClick = { uriHandler.openUri("https://compose-miuix-ui.github.io/miuix/zh_CN/") },
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Forward,
                                modifier = Modifier.padding(end = 6.dp),
                                contentDescription = "Forward"
                            )
                        },
                    )
                    ArrowPreference(
                        title = "更新日志",
                        onClick = {
                            context.startActivity(Intent(context, ReleaseNotesActivity::class.java))
                        },
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.NotesFill,
                                modifier = Modifier.padding(end = 6.dp),
                                contentDescription = "NotesFill"
                            )
                        },
                    )
                    ArrowPreference(
                        title = "软件版本",
                        endActions = { Text(text = versionName, color = MiuixTheme.colorScheme.onBackgroundVariant) },
                        onClick = {
                            if (isCheckingUpdate) return@ArrowPreference
                            coroutineScope.launch {
                                isCheckingUpdate = true
                                showNotice("正在检查更新")
                                runCatching { ReleaseNotesRepository.fetchLatestReleaseNote() }
                                    .onSuccess { release ->
                                        val remoteVersion = release?.title
                                        when {
                                            remoteVersion == null -> showNotice("未找到远端版本信息")
                                            ReleaseNotesRepository.isVersionNewer(remoteVersion, versionName) ->
                                                latestRelease = release
                                            else -> showNotice("远端版本：$remoteVersion，已经是最新版本了")
                                        }
                                    }
                                    .onFailure { showNotice("检查更新失败") }
                                isCheckingUpdate = false
                            }
                        },
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Info,
                                modifier = Modifier.padding(end = 6.dp),
                                contentDescription = "Info"
                            )
                        },
                    )
                }
            }

            latestRelease?.let { release ->
                WindowDialog(
                    title = "发现新版本",
                    show = true,
                    onDismissRequest = { latestRelease = null },
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "远端版本：${release.title}",
                            style = MiuixTheme.textStyles.body2,
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 360.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (release.items.isEmpty()) {
                                Text(text = "暂无更新日志")
                            } else {
                                release.items.forEach { item ->
                                    Text(text = "• $item")
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            TextButton(
                                text = "取消",
                                onClick = { latestRelease = null },
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(
                                text = "更新",
                                onClick = {
                                    val targetRelease = release
                                    latestRelease = null
                                    coroutineScope.launch {
                                        runCatching {
                                            AppUpdateManager.enqueue(context, targetRelease.title)
                                        }.onSuccess {
                                            showNotice("已开始下载更新")
                                        }.onFailure {
                                            showNotice("下载更新失败：${it.message ?: "未知错误"}")
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.textButtonColorsPrimary(),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


private data class BackupExportResult(
    val itemCount: Int,
    val dayCount: Int,
    val checkInItemCount: Int,
    val checkInRecordCount: Int,
    val fileName: String,
)

private data class BackupImportResult(
    val version: Int,
    val itemCount: Int,
    val dayCount: Int,
    val checkInItemCount: Int,
    val checkInRecordCount: Int,
)
