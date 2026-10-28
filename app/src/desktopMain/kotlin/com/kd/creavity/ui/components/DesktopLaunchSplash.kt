package com.kd.creavity.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Material 3 Expressive Launch Splash Animation for Desktop.
 *
 * Provides a professional, branded launch sequence featuring the CreaVITy logo mark,
 * ambient glow pulse, typography reveal, and progressive loading bar before seamlessly
 * crossfading into the workspace. Users can click anywhere to skip immediately.
 */
@Composable
fun DesktopLaunchSplash(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val logoScale = remember { Animatable(0.7f) }
    val logoAlpha = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition()
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {
        // Step 1: Logo springs in with scale & fade
        launch {
            logoAlpha.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))
        }
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f)
            )
        }

        // Step 2: Content text fades in
        delay(200)
        launch {
            contentAlpha.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
        }

        // Step 3: Progress bar smoothly sweeps to 100%
        launch {
            progress.animateTo(1f, animationSpec = tween(800, easing = FastOutSlowInEasing))
        }

        // Step 4: Settle and finish
        delay(1100)
        onSplashFinished()
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSplashFinished
            )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Subtle ambient radial background glow
            Box(
                modifier = Modifier
                    .size(420.dp)
                    .scale(haloPulse)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                // Branded Logo Mark with Elevation and Rounded Corners
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                ) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shadowElevation = 12.dp,
                        tonalElevation = 6.dp,
                        modifier = Modifier.size(112.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize().padding(14.dp)
                        ) {
                            Image(
                                painter = painterResource("icon.png"),
                                contentDescription = "CreaVITy Logo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(20.dp))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Brand Title & Tagline
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.alpha(contentAlpha.value)
                ) {
                    Text(
                        text = "CreaVITy",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Offline Collaborative Whiteboard",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Expressive progress bar
                    Box(
                        modifier = Modifier
                            .width(180.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    ) {
                        LinearProgressIndicator(
                            progress = { progress.value },
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = Color.Transparent,
                            strokeCap = StrokeCap.Round,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Version info at bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .alpha(contentAlpha.value)
            ) {
                Text(
                    text = "Desktop Edition • Skiko Skia Engine",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}
