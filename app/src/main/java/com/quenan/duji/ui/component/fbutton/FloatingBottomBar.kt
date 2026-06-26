// Adapted from KernelSU / compose-miuix-ui example (Apache 2.0)
package com.quenan.duji.ui.component.fbutton

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import com.quenan.duji.ui.component.animation.DampedDragAnimation
import com.quenan.duji.ui.component.liquid.InnerShadow
import com.quenan.duji.ui.component.liquid.innerShadow
import com.quenan.duji.ui.component.liquid.rememberCombinedBackdrop
import com.quenan.duji.ui.theme.isInDarkTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.blur
import top.yukonga.miuix.kmp.blur.drawBackdrop
import top.yukonga.miuix.kmp.blur.highlight.BloomStroke
import top.yukonga.miuix.kmp.blur.highlight.Highlight
import top.yukonga.miuix.kmp.blur.highlight.LightPosition
import top.yukonga.miuix.kmp.blur.highlight.LightSource
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.sensor.rememberDeviceTilt
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt

private val iosIndicatorSpecular: Highlight = Highlight(
    width = 1.dp,
    alpha = 1f,
    style = BloomStroke(
        color = Color.White.copy(alpha = 0.12f),
        innerBlurRadius = 2.0.dp,
        primaryLight = LightSource(LightPosition(0.5f, -0.3f, -0.05f), Color.White, 1f),
        secondaryLight = LightSource(LightPosition(0.5f, 0.8f, -0.5f), Color.White, 0.4f),
        dualPeak = true,
    ),
)

private const val LIGHT_REF_X = 0.5f
private const val LIGHT_REF_Y = 0.7f
private const val GRAVITY_DIR_THRESHOLD_SQ = 0.01f

@Composable
private fun rememberGravityRotatedHighlight(base: Highlight, extraDegrees: Float = 0f): Highlight {
    val baseStyle = base.style as BloomStroke
    val tilt by rememberDeviceTilt()
    val rotatedPrimary = remember(tilt, baseStyle.primaryLight, extraDegrees) {
        val basePrimary = baseStyle.primaryLight
        val gx = tilt.gravityX; val gy = tilt.gravityY; val gMagSq = gx * gx + gy * gy
        val (lx0, ly0) = if (gMagSq > GRAVITY_DIR_THRESHOLD_SQ) {
            val invMag = 1f / sqrt(gMagSq); (gx * invMag) to (gy * invMag)
        } else 0f to -1f
        val rad = extraDegrees * PI / 180.0; val c = cos(rad).toFloat(); val s = sin(rad).toFloat()
        basePrimary.copy(position = LightPosition(LIGHT_REF_X + c * lx0 - s * ly0, LIGHT_REF_Y + s * lx0 + c * ly0, basePrimary.position.z))
    }
    return remember(base, rotatedPrimary) { base.copy(style = baseStyle.copy(primaryLight = rotatedPrimary)) }
}

@Composable
fun RowScope.FloatingBottomBarItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier
            .clip(CircleShape)
            .clickable(interactionSource = null, indication = null, role = Role.Tab, onClick = onClick)
            .fillMaxHeight()
            .weight(1f),
        verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
fun FloatingBottomBar(
    modifier: Modifier = Modifier,
    selectedIndex: () -> Int,
    onSelected: (index: Int) -> Unit,
    backdrop: Backdrop,
    tabsCount: Int,
    isBlurEnabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val isInDark = isInDarkTheme()
    val pillShape = remember { CircleShape }
    val accentColor = MiuixTheme.colorScheme.primary
    val surfaceContainer = MiuixTheme.colorScheme.surfaceContainer
    val containerColor = if (isBlurEnabled) surfaceContainer.copy(0.4f) else surfaceContainer
    val tabsBackdrop = rememberLayerBackdrop()
    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val animationScope = rememberCoroutineScope()
    var tabWidthPx by remember { mutableFloatStateOf(0f) }
    var totalWidthPx by remember { mutableFloatStateOf(0f) }
    val offsetAnimation = remember { Animatable(0f) }
    val rubberBandPx = with(density) { 4.dp.toPx() }
    val panelOffset by remember(rubberBandPx) {
        derivedStateOf {
            if (totalWidthPx == 0f) 0f
            else {
                val fraction = (offsetAnimation.value / totalWidthPx).fastCoerceIn(-1f, 1f)
                rubberBandPx * fraction.sign * EaseOut.transform(abs(fraction))
            }
        }
    }
    var currentIndex by remember(selectedIndex) { mutableIntStateOf(selectedIndex()) }

    val dampedDragAnimation = remember(animationScope, tabsCount, density, isLtr) {
        DampedDragAnimation(
            animationScope = animationScope,
            initialValue = selectedIndex().toFloat(),
            valueRange = 0f..(tabsCount - 1).toFloat(),
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            pressedScale = 78f / 56f,
            canDrag = { offset ->
                if (tabWidthPx == 0f) return@DampedDragAnimation false
                val padding = with(density) { 4.dp.toPx() }
                val globalTouchX = if (isLtr) padding + tabWidthPx + offset.x
                                  else totalWidthPx - padding - tabWidthPx + offset.x
                globalTouchX in 0f..totalWidthPx
            },
            onDragStarted = {},
            onDragStopped = {
                val targetIndex = targetValue.fastRoundToInt().fastCoerceIn(0, tabsCount - 1)
                currentIndex = targetIndex
                animateToValue(targetIndex.toFloat())
                animationScope.launch { offsetAnimation.animateTo(0f, spring(1f, 300f, 0.5f)) }
            },
            onDrag = { _, dragAmount ->
                if (tabWidthPx > 0) {
                    updateValue((targetValue + dragAmount.x / tabWidthPx * if (isLtr) 1f else -1f)
                        .fastCoerceIn(0f, (tabsCount - 1).toFloat()))
                    animationScope.launch { offsetAnimation.snapTo(offsetAnimation.value + dragAmount.x) }
                }
            }
        )
    }

    LaunchedEffect(selectedIndex) { snapshotFlow { selectedIndex() }.collectLatest { currentIndex = it } }
    LaunchedEffect(dampedDragAnimation) {
        snapshotFlow { currentIndex }.drop(1).collectLatest { index ->
            dampedDragAnimation.animateToValue(index.toFloat()); onSelected(index)
        }
    }

    val interactiveHighlight = remember(animationScope, tabWidthPx) {
        com.quenan.duji.ui.component.animation.InteractiveHighlight(
            animationScope = animationScope,
            position = { _, _ ->
                val cx = if (isLtr) (dampedDragAnimation.value + 0.5f) * tabWidthPx + panelOffset
                         else -tabWidthPx + panelOffset
                androidx.compose.ui.geometry.Offset(cx, 0f)
            }
        )
    }

    val baseHighlight = rememberGravityRotatedHighlight(iosIndicatorSpecular, extraDegrees = -45f)
    val pillHighlight = rememberGravityRotatedHighlight(iosIndicatorSpecular, extraDegrees = 90f)
    val combinedBackdrop = rememberCombinedBackdrop(backdrop, tabsBackdrop)

    Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
        // 主底栏 Row
        Row(
            Modifier
                .onGloballyPositioned { coords ->
                    totalWidthPx = coords.size.width.toFloat()
                    val contentWidthPx = totalWidthPx - with(density) { 8.dp.toPx() }
                    tabWidthPx = (contentWidthPx / tabsCount).coerceAtLeast(0f)
                }
                .graphicsLayer { translationX = panelOffset }
                .dropShadow(shape = pillShape, shadow = Shadow(
                    radius = 10.dp, color = Color.Black, alpha = if (isInDark) 0.2f else 0.1f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {})
                .then(
                    if (isBlurEnabled) Modifier.drawBackdrop(backdrop = backdrop, shape = { pillShape },
                        effects = {
                            blur(4.dp.toPx(), 4.dp.toPx())
                        },
                        highlight = { baseHighlight.copy(alpha = 0.75f) },
                        layerBlock = {
                            val w = size.width.coerceAtLeast(1f)
                            val s = lerp(1f, 1f + 16.dp.toPx() / w, dampedDragAnimation.pressProgress)
                            scaleX = s; scaleY = s
                        },
                        onDrawSurface = { drawRect(containerColor) },
                    ) else Modifier.background(containerColor, pillShape)
                )
                .then(if (isBlurEnabled) interactiveHighlight.modifier else Modifier)
                .height(64.dp).padding(4.dp),
            verticalAlignment = Alignment.CenterVertically, content = content
        )

        // 透明层获取毛玻璃 tabs 的 backdrop
        if (isBlurEnabled) {
            Row(
                Modifier
                    .alpha(0f).layerBackdrop(tabsBackdrop)
                    .graphicsLayer { translationX = panelOffset }
                    .drawBackdrop(backdrop = backdrop, shape = { pillShape },
                        effects = { blur(4.dp.toPx(), 4.dp.toPx()) },
                        onDrawSurface = { drawRect(containerColor) })
                    .then(interactiveHighlight.modifier)
                    .height(56.dp).padding(horizontal = 4.dp)
                    .graphicsLayer(colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(accentColor)),
                verticalAlignment = Alignment.CenterVertically, content = content
            )
        }

        // 动画胶囊指示器
        if (tabWidthPx > 0f) {
            val tabWidthDp = with(density) { tabWidthPx.toDp() }
            if (isBlurEnabled) {
                Box(
                    Modifier.padding(horizontal = 4.dp)
                        .graphicsLayer {
                            val progressOffset = dampedDragAnimation.value * tabWidthPx
                            translationX = if (isLtr) progressOffset + panelOffset else -progressOffset + panelOffset
                        }
                        .then(interactiveHighlight.gestureModifier)
                        .then(dampedDragAnimation.modifier)
                        .drawBackdrop(backdrop = combinedBackdrop, shape = { pillShape },
                            effects = {
                                val p = dampedDragAnimation.pressProgress
                                blur(4.dp.toPx(), 4.dp.toPx())
                            },
                            highlight = { pillHighlight.copy(alpha = dampedDragAnimation.pressProgress) },
                            layerBlock = {
                                scaleX = dampedDragAnimation.scaleX
                                scaleY = dampedDragAnimation.scaleY
                                val v = dampedDragAnimation.velocity / 10f
                                scaleX /= 1f - (v * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                                scaleY *= 1f - (v * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                            },
                            onDrawSurface = {
                                val p = dampedDragAnimation.pressProgress
                                drawRect(if (!isInDark) Color.Black.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.1f), alpha = 1f - p)
                                drawRect(Color.Black.copy(alpha = 0.03f * p))
                            })
                        .innerShadow(shape = pillShape) {
                            InnerShadow(radius = 8.dp * dampedDragAnimation.pressProgress,
                                color = Color.Black.copy(alpha = 0.15f), alpha = dampedDragAnimation.pressProgress)
                        }
                        .height(56.dp).width(tabWidthDp)
                )
            } else {
                Box(
                    Modifier.padding(horizontal = 4.dp)
                        .graphicsLayer {
                            val progressOffset = dampedDragAnimation.value * tabWidthPx
                            translationX = if (isLtr) progressOffset + panelOffset else -progressOffset + panelOffset
                        }
                        .then(dampedDragAnimation.modifier)
                        .clip(pillShape).background(accentColor.copy(alpha = 0.15f), pillShape)
                        .height(56.dp).width(tabWidthDp)
                )
            }
        }
    }
}

private fun Modifier.alpha(v: Float): Modifier = this.graphicsLayer { alpha = v }
