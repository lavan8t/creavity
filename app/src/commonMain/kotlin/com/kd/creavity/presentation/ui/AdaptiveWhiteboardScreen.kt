package com.kd.creavity.presentation.ui

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.presentation.ui.screens.AboutScreen
import com.kd.creavity.presentation.ui.screens.BinScreen
import com.kd.creavity.presentation.ui.screens.BoardsLibraryScreen
import com.kd.creavity.presentation.ui.screens.CanvasViewportScreen
import com.kd.creavity.presentation.ui.screens.PeerConnectScreen
import com.kd.creavity.presentation.ui.screens.SettingsScreen
import com.kd.creavity.presentation.viewmodel.WhiteboardUiState
import com.kd.creavity.util.PlatformSystemBarsEffect

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
    onAction: (BoardAction) -> Unit,
    modifier: Modifier = Modifier
) {
    PlatformSystemBarsEffect(isLandscape = false)

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
        navController.popBackStack()
    }

    val latestUiState by rememberUpdatedState(uiState)
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Keyboard Shortcuts for Desktop / Windows / Stylus Keys
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false

                if (keyEvent.isCtrlPressed) {
                    when (keyEvent.key) {
                        Key.Z -> {
                            if (keyEvent.isShiftPressed) {
                                onAction(BoardAction.Redo)
                            } else {
                                onAction(BoardAction.Undo)
                            }
                            true
                        }
                        Key.Y -> {
                            onAction(BoardAction.Redo)
                            true
                        }
                        else -> false
                    }
                } else {
                    when (keyEvent.key) {
                        Key.P -> {
                            onAction(BoardAction.SelectTool(DrawingTool.PEN))
                            true
                        }
                        Key.E -> {
                            onAction(BoardAction.SelectTool(DrawingTool.ERASER))
                            true
                        }
                        Key.H -> {
                            onAction(BoardAction.SelectTool(DrawingTool.HIGHLIGHTER))
                            true
                        }
                        Key.T -> {
                            onAction(BoardAction.SelectTool(DrawingTool.TEXT))
                            true
                        }
                        Key.W -> {
                            onAction(BoardAction.SetWorksheetMode(!latestUiState.isWorksheetMode))
                            true
                        }
                        Key.V, Key.S -> {
                            onAction(BoardAction.SelectTool(DrawingTool.SELECT))
                            true
                        }
                        Key.R -> {
                            onAction(BoardAction.SelectTool(DrawingTool.RECTANGLE))
                            true
                        }
                        Key.C -> {
                            onAction(BoardAction.SelectTool(DrawingTool.CIRCLE))
                            true
                        }
                        Key.D -> {
                            onAction(BoardAction.SelectTool(DrawingTool.DIAMOND))
                            true
                        }
                        Key.Escape -> {
                            onAction(BoardAction.SelectTool(DrawingTool.PEN))
                            true
                        }
                        else -> false
                    }
                }
            }
    ) {
        val slideSpring = androidx.compose.animation.core.spring<androidx.compose.ui.unit.IntOffset>(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
        )
        val fadeSpring = androidx.compose.animation.core.spring<Float>(
            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
        )

        androidx.compose.runtime.LaunchedEffect(latestUiState.isBoardSwitchingLocked) {
            if (latestUiState.isBoardSwitchingLocked && currentRoute == NavDestination.BOARDS) {
                navigateTo(NavDestination.CANVAS)
            }
        }

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
                            if (!latestUiState.isBoardSwitchingLocked) {
                                navigateBack()
                            }
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
