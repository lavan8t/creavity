package com.kd.ootb.presentation.ui.glossy

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.ootb.presentation.ui.components.MaterialSymbol

// Real Newtonian physics profile: High initial acceleration, zero bounce, 150ms settling
val FastPhysicsSpringDp = spring<Dp>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = 1200f
)
val FastPhysicsSpringFloat = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = 1200f
)
val FastPhysicsSpringIntOffset = spring<androidx.compose.ui.unit.IntOffset>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = 1200f
)
private val SubtleSpringSpec = FastPhysicsSpringDp

// Natural Skeuomorphic Gradients (Smooth light-falloff transitions without harsh banding)
@Composable
fun dynamicPrimaryGlossy(): List<Color> {
    val p = MaterialTheme.colorScheme.primary
    val c = MaterialTheme.colorScheme.primaryContainer
    return listOf(
        p.copy(alpha = 0.96f),
        p,
        c.copy(alpha = 0.90f),
        c
    )
}

@Composable
fun dynamicSurfaceGlass(): List<Color> {
    val sHigh = MaterialTheme.colorScheme.surfaceContainerHigh
    val sLow = MaterialTheme.colorScheme.surfaceContainerLow
    return listOf(
        sHigh,
        MaterialTheme.colorScheme.surfaceContainer,
        sLow,
        MaterialTheme.colorScheme.surfaceContainerLowest
    )
}

@Composable
fun dynamicTertiaryGlossy(): List<Color> {
    val t = MaterialTheme.colorScheme.tertiary
    val tc = MaterialTheme.colorScheme.tertiaryContainer
    return listOf(
        t.copy(alpha = 0.96f),
        t,
        tc
    )
}

@Composable
fun dynamicErrorGlossy(): List<Color> {
    val e = MaterialTheme.colorScheme.error
    val ec = MaterialTheme.colorScheme.errorContainer
    return listOf(
        e.copy(alpha = 0.96f),
        e,
        ec
    )
}

// Classic Web 2.0 Studio Fallback Gradients (Natural Harmonic Palettes)
val GlossyOrangeGradient = listOf(
    Color(0xFFFF9438),
    Color(0xFFDC5F00),
    Color(0xFFB04200),
    Color(0xFF8B3000)
)

val GlossyAquaGradient = listOf(
    Color(0xFF48CAE4),
    Color(0xFF0096C7),
    Color(0xFF023E8A),
    Color(0xFF03045E)
)

val GlossyDarkGlassGradient = listOf(
    Color(0xFF2C3038),
    Color(0xFF22252C),
    Color(0xFF191B20),
    Color(0xFF121316)
)

val GlossyGreenGradient = listOf(
    Color(0xFF38E587),
    Color(0xFF00C853),
    Color(0xFF00963E),
    Color(0xFF006D2C)
)

val GlossyRedGradient = listOf(
    Color(0xFFFF5E62),
    Color(0xFFFF2A30),
    Color(0xFFC70014),
    Color(0xFF90000C)
)

/**
 * Natural Web 2.0 Glossy Button with natural light falloff and 3D push-in physics.
 */
@Composable
fun GlossyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradient: List<Color> = dynamicPrimaryGlossy(),
    shape: Shape = RoundedCornerShape(12.dp),
    bevelHeight: Dp = 2.dp,
    contentColor: Color = Color.White,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()

    val pushOffset by animateDpAsState(
        targetValue = if (isPressed) bevelHeight else 0.dp,
        animationSpec = SubtleSpringSpec,
        label = "pushOffset"
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 4.dp,
        animationSpec = SubtleSpringSpec,
        label = "shadowElevation"
    )

    Box(
        modifier = modifier
            .offset(y = pushOffset)
            .shadow(shadowElevation, shape, ambientColor = Color.Black.copy(alpha = 0.6f), spotColor = Color.Black.copy(alpha = 0.8f))
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    if (isPressed) gradient.reversed() else gradient
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.10f),
                        Color.Black.copy(alpha = 0.45f)
                    )
                ),
                shape = shape
            )
            .drawBehind {
                // Natural curved specular glass sheen with smooth fade
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (isPressed) 0.08f else 0.28f),
                            Color.White.copy(alpha = 0.04f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = this.size.height * 0.48f
                    ),
                    size = Size(this.size.width, this.size.height * 0.48f)
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                content = content
            )
        }
    }
}

/**
 * Natural Web 2.0 Glossy Icon Button with balanced specular lighting.
 */
@Composable
fun GlossyIconButton(
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    size: Dp = 42.dp,
    iconSize: Dp = 20.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    bevelHeight: Dp = 2.dp,
    selectedGradient: List<Color> = dynamicPrimaryGlossy(),
    unselectedGradient: List<Color> = dynamicSurfaceGlass(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val gradient = if (isSelected) selectedGradient else unselectedGradient

    val pushOffset by animateDpAsState(
        targetValue = if (isPressed || isSelected) bevelHeight else 0.dp,
        animationSpec = SubtleSpringSpec,
        label = "iconPushOffset"
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isPressed || isSelected) 0.5.dp else 2.5.dp,
        animationSpec = SubtleSpringSpec,
        label = "iconShadowElevation"
    )

    Box(
        modifier = modifier
            .size(size)
            .shadow(shadowElevation, shape)
            .background(
                color = gradient.last().copy(alpha = 0.90f),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .offset(y = pushOffset)
                .size(size)
                .clip(shape)
                .background(
                    brush = Brush.verticalGradient(
                        if (isPressed) gradient.reversed() else gradient
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (isSelected) 0.50f else 0.28f),
                            Color.White.copy(alpha = 0.06f),
                            Color.Black.copy(alpha = 0.35f)
                        )
                    ),
                    shape = shape
                )
                .drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = if (isSelected) 0.35f else 0.22f),
                                Color.White.copy(alpha = 0.04f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = this.size.height * 0.48f
                        ),
                        size = Size(this.size.width, this.size.height * 0.48f)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            MaterialSymbol(
                name = icon,
                tint = if (isSelected) Color.White else Color(0xFFE2E4E9),
                size = iconSize
            )
        }
    }
}

/**
 * Natural Web 2.0 Glossy Pill / Dock Container with subtle 3D glass sheen.
 */
@Composable
fun GlossyPill(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    gradient: List<Color> = dynamicSurfaceGlass(),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(6.dp, shape, ambientColor = Color.Black.copy(alpha = 0.5f), spotColor = Color.Black.copy(alpha = 0.7f))
            .background(
                brush = Brush.verticalGradient(gradient),
                shape = shape
            )
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.40f),
                        Color.White.copy(alpha = 0.10f),
                        Color.Black.copy(alpha = 0.45f)
                    )
                ),
                shape = shape
            )
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.16f),
                            Color.White.copy(alpha = 0.02f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = this.size.height * 0.45f
                    ),
                    size = Size(this.size.width, this.size.height * 0.45f)
                )
            }
            .clip(shape),
        content = content
    )
}

/**
 * Natural Web 2.0 Glossy Card with soft inner borders and 3D depth.
 */
@Composable
fun GlossyCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pushOffset by animateDpAsState(
        targetValue = if (isPressed && onClick != null) 1.5.dp else 0.dp,
        animationSpec = SubtleSpringSpec,
        label = "cardPush"
    )

    Box(
        modifier = modifier
            .shadow(4.dp, shape)
            .offset(y = pushOffset)
            .background(
                brush = Brush.verticalGradient(dynamicSurfaceGlass()),
                shape = shape
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.30f),
                        Color.White.copy(alpha = 0.06f),
                        Color.Black.copy(alpha = 0.40f)
                    )
                ),
                shape = shape
            )
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = this.size.height * 0.35f
                    ),
                    size = Size(this.size.width, this.size.height * 0.35f)
                )
            }
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        content = content
    )
}

/**
 * Natural Web 2.0 Glossy Tab
 */
@Composable
fun GlossyTab(
    title: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val gradient = if (isSelected) dynamicPrimaryGlossy() else dynamicSurfaceGlass()

    val pushOffset by animateDpAsState(
        targetValue = if (isPressed) 1.5.dp else 0.dp,
        animationSpec = SubtleSpringSpec,
        label = "tabPush"
    )

    Box(
        modifier = modifier
            .offset(y = pushOffset)
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.verticalGradient(gradient)
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = if (isSelected) 0.45f else 0.20f),
                        Color.Black.copy(alpha = 0.35f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (isSelected) 0.30f else 0.12f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = this.size.height * 0.45f
                    ),
                    size = Size(this.size.width, this.size.height * 0.45f)
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MaterialSymbol(
                name = icon,
                tint = if (isSelected) Color.White else Color(0xFFB0B4C0),
                size = 18.dp
            )
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFFB0B4C0)
            )
        }
    }
}

/**
 * Natural Web 2.0 Floating Contextual Tooltip Action Bar for selection.
 */
@Composable
fun GlossyContextualTooltipBar(
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onColorSelected: (Color) -> Unit,
    onDeselect: () -> Unit,
    strokeColors: List<Color>,
    modifier: Modifier = Modifier
) {
    GlossyPill(
        shape = RoundedCornerShape(22.dp),
        gradient = dynamicSurfaceGlass(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Copy / Duplicate Button
            GlossyIconButton(
                icon = "content_copy",
                onClick = onCopy,
                size = 36.dp,
                iconSize = 18.dp,
                shape = RoundedCornerShape(16.dp),
                selectedGradient = dynamicPrimaryGlossy(),
                unselectedGradient = dynamicPrimaryGlossy()
            )

            // Delete Button
            GlossyIconButton(
                icon = "delete",
                onClick = onDelete,
                size = 36.dp,
                iconSize = 18.dp,
                shape = RoundedCornerShape(16.dp),
                selectedGradient = dynamicErrorGlossy(),
                unselectedGradient = dynamicErrorGlossy()
            )

            // Quick Color Palette
            strokeColors.take(4).forEach { color ->
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(Color.White.copy(alpha = 0.55f), Color.Transparent),
                                    center = Offset(this.size.width * 0.35f, this.size.height * 0.30f),
                                    radius = this.size.width * 0.40f
                                )
                            )
                        }
                        .clickable { onColorSelected(color) }
                )
            }

            // Close / Deselect
            GlossyIconButton(
                icon = "close",
                onClick = onDeselect,
                size = 32.dp,
                iconSize = 16.dp,
                shape = CircleShape
            )
        }
    }
}

/**
 * Natural Skeuomorphic Toggle Switch with 3D inset track and glossy chrome thumb.
 */
@Composable
fun GlossySwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    activeGradient: List<Color> = dynamicPrimaryGlossy(),
    inactiveGradient: List<Color> = listOf(Color(0xFF262930), Color(0xFF1B1D22), Color(0xFF131418))
) {
    val trackWidth = 52.dp
    val trackHeight = 28.dp
    val thumbSize = 22.dp
    val thumbPadding = 3.dp

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize - thumbPadding else thumbPadding,
        animationSpec = FastPhysicsSpringDp,
        label = "switchThumb"
    )

    Box(
        modifier = modifier
            .size(width = trackWidth, height = trackHeight)
            .shadow(2.dp, RoundedCornerShape(14.dp), ambientColor = Color.Black, spotColor = Color.Black)
            .clip(RoundedCornerShape(14.dp))
            .background(
                brush = Brush.verticalGradient(
                    if (checked) activeGradient else inactiveGradient
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.Black.copy(alpha = 0.65f),
                        Color.White.copy(alpha = 0.18f)
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent),
                        startY = 0f,
                        endY = this.size.height * 0.65f
                    ),
                    size = Size(this.size.width, this.size.height * 0.65f)
                )
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) }
    ) {
        // 3D Natural Glossy Chrome Thumb
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = thumbOffset)
                .size(thumbSize)
                .shadow(3.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFFFFFFF), Color(0xFFE2E4EA), Color(0xFFC0C4D0), Color(0xFF9EA3B0))
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.90f),
                            Color.Black.copy(alpha = 0.35f)
                        )
                    ),
                    shape = CircleShape
                )
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.80f), Color.Transparent),
                            center = Offset(this.size.width * 0.35f, this.size.height * 0.30f),
                            radius = this.size.width * 0.40f
                        )
                    )
                }
        )
    }
}

/**
 * Natural Skeuomorphic Slider with sunken groove track and tactile glossy knob.
 */
@Composable
fun GlossySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    modifier: Modifier = Modifier,
    activeGradient: List<Color> = dynamicPrimaryGlossy()
) {
    val normalizedValue = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .pointerInput(valueRange) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                    val newValue = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                    onValueChange(newValue)
                }
            }
            .pointerInput(valueRange) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val fraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                    val newValue = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                    onValueChange(newValue)
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val totalWidth = maxWidth
        val thumbRadius = 12.dp
        val activeWidth = (totalWidth - thumbRadius * 2) * normalizedValue

        // Inset 3D Groove Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF14161A))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.75f), Color.White.copy(alpha = 0.15f))
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
                .drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent),
                            startY = 0f,
                            endY = this.size.height
                        )
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(activeWidth + thumbRadius)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush = Brush.horizontalGradient(activeGradient))
            )
        }

        // 3D Tactile Glossy Knob
        Box(
            modifier = Modifier
                .offset(x = activeWidth)
                .size(thumbRadius * 2)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFFFFFFF), Color(0xFFDEE0E6), Color(0xFFAFB4C0), Color(0xFF868B98))
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.90f), Color.Black.copy(alpha = 0.45f))
                    ),
                    shape = CircleShape
                )
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.85f), Color.Transparent),
                            center = Offset(this.size.width * 0.35f, this.size.height * 0.30f),
                            radius = this.size.width * 0.40f
                        )
                    )
                }
        )
    }
}

/**
 * Natural Skeuomorphic Inset Typing Field with soft shadow, focus glow, and custom font support.
 */
@Composable
fun GlossyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    fontFamily: FontFamily? = null,
    fontSize: TextUnit = 15.sp,
    textColor: Color = Color.White
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        interactionSource = interactionSource,
        textStyle = TextStyle(
            color = textColor,
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium
        ),
        cursorBrush = SolidColor(Color.White),
        modifier = modifier
            .shadow(if (isFocused) 3.dp else 1.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF14161B))
            .border(
                width = if (isFocused) 1.5.dp else 1.dp,
                brush = Brush.verticalGradient(
                    if (isFocused) dynamicPrimaryGlossy()
                    else listOf(
                        Color.Black.copy(alpha = 0.65f),
                        Color.White.copy(alpha = 0.15f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.50f), Color.Transparent),
                        startY = 0f,
                        endY = this.size.height * 0.40f
                    ),
                    size = Size(this.size.width, this.size.height * 0.40f)
                )
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = fontSize,
                        fontFamily = fontFamily,
                        color = Color(0xFF7E8494)
                    )
                }
                innerTextField()
            }
        }
    )
}

/**
 * Genuine Web 2.0 Skeuomorphic Bottom Sheet with 3D glass sheen, specular rim, and chrome drag pill.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlossyBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    shape: Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    showDragHandle: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        contentColor = Color.White,
        dragHandle = null,
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.60f),
        shape = shape
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .shadow(16.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
                .clip(shape)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF282B33),
                            Color(0xFF1E2127),
                            Color(0xFF16181D),
                            Color(0xFF101114)
                        )
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.55f),
                            Color.White.copy(alpha = 0.12f),
                            Color.Black.copy(alpha = 0.70f)
                        )
                    ),
                    shape = shape
                )
                .drawBehind {
                    // Curved specular glass reflection across the top portion
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.16f),
                                Color.White.copy(alpha = 0.04f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = this.size.height * 0.38f
                        ),
                        size = Size(this.size.width, this.size.height * 0.38f)
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showDragHandle) {
                    // 3D Tactile Chrome Inset Drag Handle
                    Box(
                        modifier = Modifier
                            .padding(top = 10.dp, bottom = 4.dp)
                            .size(width = 46.dp, height = 5.dp)
                            .shadow(2.dp, RoundedCornerShape(3.dp))
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        Color(0xFFFFFFFF),
                                        Color(0xFFD4D8E4),
                                        Color(0xFF9EA4B4),
                                        Color(0xFF6E7484)
                                    )
                                )
                            )
                            .border(
                                0.8.dp,
                                Brush.verticalGradient(
                                    listOf(Color.White.copy(alpha = 0.85f), Color.Black.copy(alpha = 0.60f))
                                ),
                                RoundedCornerShape(3.dp)
                            )
                    )
                }
                content()
            }
        }
    }
}
