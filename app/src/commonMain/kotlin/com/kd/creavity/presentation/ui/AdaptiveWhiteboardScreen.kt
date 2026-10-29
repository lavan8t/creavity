package com.kd.creavity.presentation.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kd.creavity.domain.model.DrawingTool
import com.kd.creavity.presentation.ui.components.LibraryNavDrawer
import com.kd.creavity.presentation.ui.motion.BrevityMotion
import com.kd.creavity.presentation.ui.screens.AboutScreen
import com.kd.creavity.presentation.ui.screens.BinScreen
import com.kd.creavity.presentation.ui.screens.BoardsLibraryScreen
import com.kd.creavity.presentation.ui.screens.CanvasViewportScreen
import com.kd.creavity.presentation.ui.screens.PeerConnectScreen
import com.kd.creavity.presentation.ui.screens.SettingsScreen
import com.kd.creavity.presentation.viewmodel.WhiteboardUiState
import com.kd.creavity.util.PlatformSystemBarsEffect
import kotlinx.coroutines.launch

enum class NavDestination(val route: String, val icon: String, val label: String) {
    BOARDS("boards", "folder", "Boards"),
    CANVAS("canvas", "draw", "Canvas"),
    PEERS("peers", "sensors", "Peers"),
    SETTINGS("settings", "settings", "Settings"),
    BIN("bin", "delete", "Bin")
}

private fun destinationOrder(route: String?): Int = when (route) {
    NavDestination.BOARDS.name -> 0
    NavDestination.PEERS.name -> 1
    NavDestination.BIN.name -> 2
    NavDestination.SETTINGS.name -> 3
    NavDestination.CANVAS.name -> 99
    else -> 0
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

    val binnedCount = remember(uiState.savedBoards) {
        uiState.savedBoards.count { it.isInBin }
    }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun navigateTo(dest: NavDestination) {
        if (dest == NavDestination.BOARDS) {
            navController.popBackStack(NavDestination.BOARDS.name, inclusive = false)
        } else {
            navController.navigate(dest.name) {
                popUpTo(NavDestination.BOARDS.name) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    fun navigateBack() {
        navController.popBackStack()
    }

    val latestUiState by rememberUpdatedState(uiState)
    val focusRequester = remember { FocusRequester() }
    var isSpacePressed by remember { mutableStateOf(false) }

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
                // Spacebar hold-to-pan: active only on canvas viewport
                if (currentRoute == NavDestination.CANVAS) {
                    if (keyEvent.key == Key.Spacebar) {
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            isSpacePressed = true
                            return@onKeyEvent true
                        } else if (keyEvent.type == KeyEventType.KeyUp) {
                            isSpacePressed = false
                            return@onKeyEvent true
                        }
                    }
                }

                if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false

                // Global fallback shortcuts only when on Canvas screen
                if (currentRoute == NavDestination.CANVAS) {
                    val isCtrlOrMeta = keyEvent.isCtrlPressed || keyEvent.isMetaPressed
                    if (isCtrlOrMeta) {
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
                            Key.Equals, Key.Plus, Key.NumPadAdd -> {
                                val newZoom = (latestUiState.zoom * 1.25f).coerceAtMost(5.0f)
                                onAction(BoardAction.SetCanvasTransform(newZoom, latestUiState.panOffset))
                                true
                            }
                            Key.Minus, Key.NumPadSubtract -> {
                                val newZoom = (latestUiState.zoom / 1.25f).coerceAtLeast(0.1f)
                                onAction(BoardAction.SetCanvasTransform(newZoom, latestUiState.panOffset))
                                true
                            }
                            Key.Zero, Key.NumPad0 -> {
                                onAction(BoardAction.ResetCanvasTransform)
                                true
                            }
                            else -> false
                        }
                    } else {
                        false
                    }
                } else {
                    false
                }
            }
    ) {
        androidx.compose.runtime.LaunchedEffect(latestUiState.isBoardSwitchingLocked) {
            if (latestUiState.isBoardSwitchingLocked && currentRoute == NavDestination.BOARDS) {
                navigateTo(NavDestination.CANVAS)
            }
        }

        androidx.compose.runtime.LaunchedEffect(currentRoute) {
            if (currentRoute != NavDestination.CANVAS) {
                com.kd.creavity.presentation.canvas.resetToolCursor()
            }
        }

        LibraryNavDrawer(
            drawerState = drawerState,
            binnedCount = binnedCount,
            onNavigateToDestination = { dest -> navigateTo(dest) },
            currentDestination = currentRoute
        ) {
            NavHost(
                navController = navController,
                startDestination = NavDestination.BOARDS.name,
                enterTransition = {
                    val from = destinationOrder(initialState.destination.route)
                    val to = destinationOrder(targetState.destination.route)
                    if (targetState.destination.route == NavDestination.CANVAS.name || initialState.destination.route == NavDestination.CANVAS.name) {
                        fadeIn(animationSpec = BrevityMotion.springEffects()) + scaleIn(initialScale = 0.96f, animationSpec = BrevityMotion.springSpatial())
                    } else if (to >= from) {
                        BrevityMotion.enterSlideUp()
                    } else {
                        BrevityMotion.enterSlideDown()
                    }
                },
                exitTransition = {
                    val from = destinationOrder(initialState.destination.route)
                    val to = destinationOrder(targetState.destination.route)
                    if (targetState.destination.route == NavDestination.CANVAS.name || initialState.destination.route == NavDestination.CANVAS.name) {
                        fadeOut(animationSpec = BrevityMotion.springEffects()) + scaleOut(targetScale = 0.96f, animationSpec = BrevityMotion.springSpatial())
                    } else if (to >= from) {
                        BrevityMotion.exitSlideUp()
                    } else {
                        BrevityMotion.exitSlideDown()
                    }
                },
                popEnterTransition = {
                    val from = destinationOrder(initialState.destination.route)
                    val to = destinationOrder(targetState.destination.route)
                    if (targetState.destination.route == NavDestination.CANVAS.name || initialState.destination.route == NavDestination.CANVAS.name) {
                        fadeIn(animationSpec = BrevityMotion.springEffects())
                    } else if (to <= from) {
                        BrevityMotion.enterSlideDown()
                    } else {
                        BrevityMotion.enterSlideUp()
                    }
                },
                popExitTransition = {
                    val from = destinationOrder(initialState.destination.route)
                    val to = destinationOrder(targetState.destination.route)
                    if (targetState.destination.route == NavDestination.CANVAS.name || initialState.destination.route == NavDestination.CANVAS.name) {
                        fadeOut(animationSpec = BrevityMotion.springEffects())
                    } else if (to <= from) {
                        BrevityMotion.exitSlideDown()
                    } else {
                        BrevityMotion.exitSlideUp()
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                composable(NavDestination.BOARDS.name) {
                    BoardsLibraryScreen(
                        uiState = latestUiState,
                        onAction = onAction,
                        onNavigateToCanvas = { navigateTo(NavDestination.CANVAS) },
                        onNavigateToDestination = { dest -> navigateTo(dest) },
                        onOpenDrawer = { scope.launch { drawerState.open() } },
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
                        isSpacePressed = isSpacePressed,
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
            }
        }
    }
}
