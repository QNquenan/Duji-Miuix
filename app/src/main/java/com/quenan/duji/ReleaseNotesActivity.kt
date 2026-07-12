package com.quenan.duji

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quenan.duji.data.ReleaseNoteEntry
import com.quenan.duji.data.ReleaseNotesRepository
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

        setContent {
            DuJiTheme {
                val scrollBehavior = MiuixScrollBehavior()
                val releaseNotes = remember { ReleaseNotesRepository.load(applicationContext) }

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
                    },
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
                        releaseNotes.forEach { note ->
                            SmallTitle(
                                text = note.date,
                                insideMargin = PaddingValues(top = 16.dp, bottom = 2.dp, start = 16.dp),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                insideMargin = PaddingValues(0.dp),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Text(
                                        text = note.title,
                                        color = MiuixTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    note.items.forEach { item ->
                                        Text(
                                            text = item,
                                            color = itemColor(item),
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(
                            modifier = Modifier
                                .height(12.dp)
                                .navigationBarsPadding()
                        )
                    }
                }
            }
        }
    }


    private fun itemColor(item: String): Color {
        return when {
            item.startsWith("feat:") -> Color(0xFF5EBD7D)
            item.startsWith("fix:") -> Color(0xFF6EA8FE)
            item.startsWith("style:") -> Color(0xFFC084FC)
            item.startsWith("docs:") -> Color(0xFF9CA3AF)
            item.startsWith("refactor:") || item.startsWith("refactor(ui):") -> Color(0xFFF6C453)
            else -> Color(0xFFB0B7C3)
        }
    }
}
