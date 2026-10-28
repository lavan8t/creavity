package com.kd.creavity.presentation.ui.components

import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kd.creavity.R
import kotlinx.coroutines.delay

/**
 * High-performance Animated Vector Drawable splash screen for CreaVITy.
 * Displays the isometric dot-canvas and plays the sleek drawing animation
 * where the digital stylus actively draws the 'cr' logo.
 *
 * Automatically triggers [onFinished] upon animation completion, or on user tap.
 */
@Composable
fun SplashDrawingOverlay(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }
    val interactionSource = remember { MutableInteractionSource() }

    // Fallback safety timeout: If AVD callback is delayed on any OEM, proceed after 2.2s
    LaunchedEffect(Unit) {
        delay(2200)
        if (isVisible) {
            isVisible = false
            onFinished()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        exit = fadeOut(animationSpec = tween(durationMillis = 350)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F5F7))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    if (isVisible) {
                        isVisible = false
                        onFinished()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier.size(340.dp),
                factory = { context ->
                    ImageView(context).apply {
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        val drawable = ContextCompat.getDrawable(context, R.drawable.avd_creavity_splash_logo)
                        setImageDrawable(drawable)

                        if (drawable is AnimatedVectorDrawable) {
                            drawable.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                                override fun onAnimationEnd(d: Drawable?) {
                                    post {
                                        if (isVisible) {
                                            isVisible = false
                                            onFinished()
                                        }
                                    }
                                }
                            })
                            drawable.start()
                        } else if (drawable is android.graphics.drawable.Animatable) {
                            drawable.start()
                        }
                    }
                }
            )
        }
    }
}
