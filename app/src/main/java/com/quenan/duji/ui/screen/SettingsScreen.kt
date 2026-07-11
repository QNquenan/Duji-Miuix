package com.quenan.duji.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quenan.duji.ui.component.rememberNoticeAction
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
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
    var currentPage by remember { mutableStateOf(SettingsSubPage.Main) }

    BackHandler(enabled = currentPage == SettingsSubPage.ReleaseNotes) {
        currentPage = SettingsSubPage.Main
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = if (currentPage == SettingsSubPage.Main) "设置" else "更新日志",
                navigationIcon = {
                    if (currentPage == SettingsSubPage.ReleaseNotes) {
                        IconButton(
                            onClick = { currentPage = SettingsSubPage.Main },
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { innerPadding ->
        if (currentPage == SettingsSubPage.ReleaseNotes) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SmallTitle(text = "更新日志", insideMargin = PaddingValues(16.dp, 2.dp), modifier = Modifier.fillMaxWidth())
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    insideMargin = PaddingValues(0.dp),
                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(text = "当前版本", color = MiuixTheme.colorScheme.onBackgroundVariant)
                        Text(text = versionName, color = MiuixTheme.colorScheme.onBackground)
                        HorizontalDivider()
                        Text(
                            text = "这里将展示完整的版本更新记录。",
                            color = MiuixTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = "当前为占位页面，后续会补充每个版本的新增内容、修复项和兼容性说明。",
                            color = MiuixTheme.colorScheme.onBackgroundVariant,
                            textAlign = TextAlign.Start,
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SmallTitle(text = "外观", insideMargin = PaddingValues(16.dp, 2.dp), modifier = Modifier.fillMaxWidth())
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
                        )
                        SwitchPreference(
                            title = "启用预见式返回手势",
                            checked = predictiveBackEnabled,
                            onCheckedChange = { predictiveBackEnabled = it },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        )
                    }
                }

                SmallTitle(text = "备份", insideMargin = PaddingValues(16.dp, 2.dp), modifier = Modifier.fillMaxWidth())
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    insideMargin = PaddingValues(0.dp),
                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        BasicComponent(title = "导出", onClick = { showNotice("导出暂未实现") })
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        BasicComponent(title = "导入", onClick = { showNotice("导入暂未实现") })
                    }
                }

                SmallTitle(text = "关于", insideMargin = PaddingValues(16.dp, 2.dp), modifier = Modifier.fillMaxWidth())
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    insideMargin = PaddingValues(0.dp),
                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        BasicComponent(
                            title = "GitHub",
                            endActions = { Text(text = "QNquenan/DuJi", color = MiuixTheme.colorScheme.onBackgroundVariant) },
                            onClick = { uriHandler.openUri("https://github.com/QNquenan/DuJi") },
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        BasicComponent(
                            title = "Miuix",
                            endActions = { Text(text = "compose-miuix-ui", color = MiuixTheme.colorScheme.onBackgroundVariant) },
                            onClick = { uriHandler.openUri("https://compose-miuix-ui.github.io/miuix/zh_CN/") },
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        BasicComponent(
                            title = "更新日志",
                            onClick = { currentPage = SettingsSubPage.ReleaseNotes },
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        BasicComponent(
                            title = "当前软件版本",
                            endActions = { Text(text = versionName, color = MiuixTheme.colorScheme.onBackgroundVariant) },
                            onClick = { },
                        )
                    }
                }
            }
        }
    }
}

private enum class SettingsSubPage {
    Main,
    ReleaseNotes,
}
