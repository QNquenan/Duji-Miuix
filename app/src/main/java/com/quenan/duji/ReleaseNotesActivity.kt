package com.quenan.duji

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quenan.duji.ui.theme.DuJiTheme
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

class ReleaseNotesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val versionName = runCatching {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "未知版本"
        }.getOrDefault("未知版本")

        setContent {
            DuJiTheme {
                val scrollBehavior = MiuixScrollBehavior()

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = "更新日志",
                            navigationIcon = {
                                IconButton(onClick = ::finish) {
                                    Icon(
                                        imageVector = MiuixIcons.Back,
                                        contentDescription = "返回",
                                    )
                                }
                            },
                            scrollBehavior = scrollBehavior,
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SmallTitle(
                            text = "更新日志",
                            insideMargin = PaddingValues(16.dp, 2.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )
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
                                Text(
                                    text = "这里将展示完整的版本更新记录。",
                                    color = MiuixTheme.colorScheme.onBackground,
                                )
                                Text(
                                    text = "当前为占位页面，后续会补充每个版本的新增内容、修复项和兼容性说明。",
                                    color = MiuixTheme.colorScheme.onBackgroundVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
