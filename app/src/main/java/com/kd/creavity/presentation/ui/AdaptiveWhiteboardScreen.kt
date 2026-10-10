package com.kd.creavity.presentation.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import android.content.res.Configuration
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.presentation.ui.screens.AboutScreen
import com.kd.creavity.presentation.ui.screens.BinScreen
import com.kd.creavity.presentation.ui.screens.BoardsLibraryScreen
import com.kd.creavity.presentation.ui.screens.CanvasViewportScreen
import com.kd.creavity.presentation.ui.screens.PeerConnectScreen
import com.kd.creavity.presentation.ui.screens.SettingsScreen
import com.kd.creavity.presentation.viewmodel.WhiteboardUiState

enum class NavDestination(val route: String, val icon: String, val label: String) {
    BOARDS("boards", "folder", "Boards"),
    CANVAS("canvas", "draw", "Canvas"),
    PEERS("peers", "sensors", "Peers"),
    SETTINGS("settings", "settings", "Settings"),
    BIN("bin", "delete", "Bin"),
    ABOUT("about", "info", "About")
}

@Composable
fun AdaptiveWhiteboardScreen(
    uiState: WhiteboardUiState,
    onAction: (OotbAction) -> Unit,
    modifier: Modifier = Modifier,
    windowWidthSizeClass: WindowWidthSizeClass? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val view = LocalView.current

    DisposableEffect(isLandscape) {
        val window = activity?.window ?: return@DisposableEffect onDispose {}
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (isLandscape) {
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route?.let { route ->
        runCatching { NavDestination.valueOf(route) }.getOrNull()
    } ?: NavDestination.BOARDS

    fun navigateTo(dest: NavDestination) {
        if (dest == NavDestination.BOARDS) {
            navController.popBackStack(NavDestination.BOARDS.name, inclusive = false)
        } else {
            navController.navigate(dest.name) {
                launchSingleTop = true
            }
        }
    }

    fun navigateBack() {
        if (!navController.popBackStack()) {
            activity?.finish()
        }
    }

    val latestUiState by rememberUpdatedState(uiState)

    // Keyboard Shortcuts for Desktop / Windows / Stylus Keys
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .onKeyEvent { keyEvent ->
                if (keyEvent.isCtrlPressed) {
                    when (keyEvent.key) {
                        Key.Z -> {
                            onAction(OotbAction.Undo)
                            true
                        }
                        Key.Y -> {
                            onAction(OotbAction.Redo)
                            true
                        }
                        else -> false
                    }
                } else {
                    when (keyEvent.key) {
                        Key.P -> {
                            onAction(OotbAction.SelectTool(DrawingTool.PEN))
                            true
                        }
                        Key.E -> {
                            onAction(OotbAction.SelectTool(DrawingTool.ERASER))
                            true
                        }
                        Key.H -> {
                            onAction(OotbAction.SelectTool(DrawingTool.HIGHLIGHTER))
                            true
                        }
                        Key.T -> {
                            onAction(OotbAction.SelectTool(DrawingTool.TEXT))
                            true
                        }
                        Key.R -> {
                            onAction(OotbAction.SelectTool(DrawingTool.RECTANGLE))
                            true
                        }
                        Key.C -> {
                            onAction(OotbAction.SelectTool(DrawingTool.CIRCLE))
                            true
                        }
                        else -> false
                    }
                }
            }
            .focusable()
    ) {
        val slideSpring = androidx.compose.animation.core.spring<androidx.compose.ui.unit.IntOffset>(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
        )
        val fadeSpring = androidx.compose.animation.core.spring<Float>(
            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
        )

        NavHost(
            navController = navController,
            startDestination = NavDestination.BOARDS.name,
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = slideSpring) + androidx.compose.animation.fadeIn(animationSpec = fadeSpring)
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = slideSpring) + androidx.compose.animation.fadeOut(animationSpec = fadeSpring)
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = slideSpring) + androidx.compose.animation.fadeIn(animationSpec = fadeSpring)
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = slideSpring) + androidx.compose.animation.fadeOut(animationSpec = fadeSpring)
            },
            modifier = Modifier.fillMaxSize()
        ) {
            composable(NavDestination.BOARDS.name) {
                BoardsLibraryScreen(
                    uiState = latestUiState,
                    onAction = onAction,
                    onNavigateToCanvas = { navigateTo(NavDestination.CANVAS) },
                    onNavigateToDestination = { dest -> navigateTo(dest) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(NavDestination.CANVAS.name) {
                CanvasViewportScreen(
                    uiState = latestUiState,
                    onAction = onAction,
                    onNavigateToDestination = { dest ->
                        if (dest == NavDestination.BOARDS) {
                            navigateBack()
                        } else {
                            navigateTo(dest)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(NavDestination.PEERS.name) {
                PeerConnectScreen(
                    uiState = latestUiState,
                    onAction = onAction,
                    onNavigateToCanvas = { navigateBack() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(NavDestination.SETTINGS.name) {
                SettingsScreen(
                    uiState = latestUiState,
                    onAction = onAction,
                    onNavigateToCanvas = { navigateBack() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(NavDestination.BIN.name) {
                BinScreen(
                    uiState = latestUiState,
                    onAction = onAction,
                    onBackPressed = { navigateBack() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(NavDestination.ABOUT.name) {
                AboutScreen(
                    onBackPressed = { navigateBack() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
