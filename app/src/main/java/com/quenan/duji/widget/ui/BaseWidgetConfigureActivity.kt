package com.quenan.duji.widget.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quenan.duji.ui.theme.DuJiTheme
import com.quenan.duji.widget.WidgetSelection
import com.quenan.duji.widget.WidgetSelectionRepository
import com.quenan.duji.widget.WidgetSelectionType
import com.quenan.duji.widget.WidgetUpdateDispatcher
import kotlinx.coroutines.runBlocking
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

abstract class BaseWidgetConfigureActivity<T> : ComponentActivity() {
    protected var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            DuJiTheme {
                var entries by remember { mutableStateOf<List<T>>(emptyList()) }
                LaunchedEffect(Unit) {
                    entries = loadEntries()
                }
                Scaffold(
                    topBar = {
                        TopAppBar(title = screenTitle)
                    }
                ) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(entries) { entry ->
                            EntryCard(entry = entry, onClick = {
                                val selection = toSelection(entry)
                                runBlocking {
                                    WidgetSelectionRepository(applicationContext).saveSelection(
                                        appWidgetId = appWidgetId,
                                        selection = selection,
                                    )
                                }
                                val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                setResult(Activity.RESULT_OK, result)
                                if (selection.type != WidgetSelectionType.THOSE_DAY_SQUARE) {
                                    runBlocking {
                                        WidgetUpdateDispatcher.updateConfiguredWidget(
                                            context = applicationContext,
                                            appWidgetId = appWidgetId,
                                            selection = selection,
                                        )
                                        WidgetUpdateDispatcher.refreshAll(applicationContext)
                                    }
                                }
                                finish()
                            })
                        }
                    }
                }
            }
        }
    }

    protected abstract val screenTitle: String
    protected abstract suspend fun loadEntries(): List<T>
    protected abstract fun toSelection(entry: T): WidgetSelection

    @Composable
    protected abstract fun EntryCard(entry: T, onClick: () -> Unit)

    @Composable
    protected fun DefaultEntryCard(
        title: String,
        subtitle: String,
        trailing: String,
        onClick: () -> Unit,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            insideMargin = androidx.compose.foundation.layout.PaddingValues(16.dp),
            colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = subtitle, fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary)
                }
                Text(text = trailing, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
