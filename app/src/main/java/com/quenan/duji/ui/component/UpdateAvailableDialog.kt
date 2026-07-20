package com.quenan.duji.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quenan.duji.data.ReleaseNoteEntry
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@androidx.compose.runtime.Composable
fun UpdateAvailableDialog(
    release: ReleaseNoteEntry,
    onDismiss: () -> Unit,
    onUpdate: (ReleaseNoteEntry) -> Unit,
) {
    WindowDialog(
        title = "\u53d1\u73b0\u65b0\u7248\u672c",
        show = true,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "\u8fdc\u7aef\u7248\u672c\uff1a${release.title}",
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
                    Text(text = "\u6682\u65e0\u66f4\u65b0\u65e5\u5fd7")
                } else {
                    release.items.forEach { item ->
                        Text(text = "\u2022 $item")
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(
                    text = "\u53d6\u6d88",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = "\u66f4\u65b0",
                    onClick = { onUpdate(release) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        }
    }
}
