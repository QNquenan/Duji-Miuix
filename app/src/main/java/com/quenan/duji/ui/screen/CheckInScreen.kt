package com.quenan.duji.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

private const val EXERCISE_DAY_COUNT = 31
private const val EXERCISE_MONTH_COUNT = 5
private const val EXERCISE_TOTAL_COUNT = 66
private val exerciseCompletedColor = Color(0xFF5EBD7D)
private val exerciseBlockSize = 4.dp

@Composable
fun CheckInScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val scrollBehavior = MiuixScrollBehavior()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = "打卡",
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
                .padding(contentPadding)
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            ExerciseCard()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ExerciseCard() {
    val inactiveColor = MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.35f)
    val checkInButtonBackground = exerciseCompletedColor.copy(alpha = 0.18f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🏋️", fontSize = 26.sp)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "运动",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onBackground,
                )
                Text(
                    text = "本月${EXERCISE_MONTH_COUNT}次 • 共${EXERCISE_TOTAL_COUNT}次",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onBackgroundVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(EXERCISE_DAY_COUNT) { index ->
                        Box(
                            modifier = Modifier
                                .size(exerciseBlockSize)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (index in 13..17) {
                                        exerciseCompletedColor
                                    } else {
                                        inactiveColor
                                    },
                                ),
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(50))
                    .background(checkInButtonBackground),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "打卡",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = exerciseCompletedColor,
                )
            }
        }
    }
}
