package com.quenan.duji.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.quenan.duji.data.day.DayData
import com.quenan.duji.data.day.computeStatus
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Pin
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun DayListItem(
    day: DayData,
    onClick: () -> Unit,
) {
    val status = day.computeStatus()
    val daysColor = when {
        status.statusText == "就是今天！" -> Color(0xFFFF9800)
        status.diff > 0 && status.diff <= 7 -> Color(0xFFFF9800)
        status.diff < 0 && day.repeatCycle.name == "NONE" -> MiuixTheme.colorScheme.onBackgroundVariant
        else -> MiuixTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        insideMargin = androidx.compose.foundation.layout.PaddingValues(16.dp),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MiuixTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = day.emoji, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.padding(start = 14.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (day.isPinned) {
                        Icon(
                            imageVector = MiuixIcons.Pin,
                            contentDescription = "置顶",
                            tint = MiuixTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.padding(start = 4.dp))
                    }
                    Text(
                        text = day.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MiuixTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = status.dateText,
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onBackgroundVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.padding(start = 8.dp))
            Text(
                text = status.statusText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = daysColor,
            )
        }
    }
}

@Composable
fun DayGridItem(
    day: DayData,
    onClick: () -> Unit,
) {
    val status = day.computeStatus()
    val daysColor = when {
        status.statusText == "就是今天！" -> Color(0xFFFF9800)
        status.diff > 0 && status.diff <= 7 -> Color(0xFFFF9800)
        status.diff < 0 && day.repeatCycle.name == "NONE" -> MiuixTheme.colorScheme.onBackgroundVariant
        else -> MiuixTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        insideMargin = androidx.compose.foundation.layout.PaddingValues(14.dp),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MiuixTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = day.emoji, fontSize = 26.sp)
                }
                if (day.isPinned) {
                    Spacer(modifier = Modifier.padding(start = 6.dp))
                    Icon(
                        imageVector = MiuixIcons.Pin,
                        contentDescription = "置顶",
                        tint = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = day.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = status.dateText,
                fontSize = 11.sp,
                color = MiuixTheme.colorScheme.onBackgroundVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = status.statusText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = daysColor,
            )
        }
    }
}
