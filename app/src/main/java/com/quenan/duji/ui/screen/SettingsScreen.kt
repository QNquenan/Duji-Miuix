package com.quenan.duji.ui.screen

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.quenan.duji.ReleaseNotesActivity
import com.quenan.duji.ui.component.rememberNoticeAction
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.MoveFile
import top.yukonga.miuix.kmp.icon.extended.Remove
import top.yukonga.miuix.kmp.icon.extended.Forward
import top.yukonga.miuix.kmp.icon.extended.Link
import top.yukonga.miuix.kmp.icon.extended.NotesFill
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Background
import top.yukonga.miuix.kmp.icon.extended.Reset
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SettingsScreen() {
    val scrollBehavior = MiuixScrollBehavior()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val showNotice = rememberNoticeAction()
    val colorModeOptions = remember { listOf("跟随系统", "浅色", "深色") }
    val versionName = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "未知版本"
        }.getOrDefault("未知版本")
    }
    var selectedColorModeIndex by remember { mutableIntStateOf(0) }
    var predictiveBackEnabled by remember { mutableStateOf(true) }

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
                        onSelectedIndexChange = { selectedColorModeIndex = it },
                        insideMargin = PaddingValues(horizontal = 16.dp),
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Background,
                                modifier = Modifier.padding(end = 6.dp),
                                contentDescription = "颜色模式"
                            )
                        },
                    )
                    SwitchPreference(
                        title = "启用预见式返回手势",
                        checked = predictiveBackEnabled,
                        onCheckedChange = { predictiveBackEnabled = it },
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Remove,
                                modifier = Modifier.padding(end = 6.dp),
                                contentDescription = "启用预见式返回手势"
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
                        onClick = { showNotice("导出暂未实现") },
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
                        onClick = { showNotice("导入暂未实现") },
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
                        title = "当前软件版本",
                        endActions = { Text(text = versionName, color = MiuixTheme.colorScheme.onBackgroundVariant) },
                        onClick = { },
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

            // 底部间距
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
