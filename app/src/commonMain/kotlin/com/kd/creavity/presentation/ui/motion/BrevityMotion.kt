package com.kd.creavity.presentation.ui.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically


/**
 * Spring-based motion specs and in/out transitions matching Material 3 Expressive motion physics.
 */
object BrevityMotion {
    // Spring physics presets
    fun <T> springSpatial(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    fun <T> springEffects(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    // Standard in & out animation pairs for dialogs, flyouts, and floating bars
    fun enterFadeScale(): EnterTransition =
        fadeIn(animationSpec = springEffects()) + scaleIn(initialScale = 0.88f, animationSpec = springSpatial())

    fun exitFadeScale(): ExitTransition =
        fadeOut(animationSpec = springEffects()) + scaleOut(targetScale = 0.88f, animationSpec = springSpatial())

    fun enterSlideUp(): EnterTransition =
        fadeIn(animationSpec = springEffects()) + slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = springSpatial()
        )

    fun exitSlideDown(): ExitTransition =
        fadeOut(animationSpec = springEffects()) + slideOutVertically(
            targetOffsetY = { it / 2 },
            animationSpec = springSpatial()
        )

    fun enterSlideDown(): EnterTransition =
        fadeIn(animationSpec = springEffects()) + slideInVertically(
            initialOffsetY = { -it / 2 },
            animationSpec = springSpatial()
        )

    fun exitSlideUp(): ExitTransition =
        fadeOut(animationSpec = springEffects()) + slideOutVertically(
            targetOffsetY = { -it / 2 },
            animationSpec = springSpatial()
        )
}
