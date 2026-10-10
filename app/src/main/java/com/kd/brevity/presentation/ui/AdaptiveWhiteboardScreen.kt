package com.kd.brevity.presentation.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kd.brevity.domain.model.DrawingTool
import com.kd.brevity.presentation.ui.components.MaterialSymbol
import com.kd.brevity.presentation.ui.components.TooltipHint
import com.kd.brevity.presentation.ui.motion.BrevityMotion
import com.kd.brevity.presentation.ui.screens.AboutScreen
import com.kd.brevity.presentation.ui.screens.BinScreen
import com.kd.brevity.presentation.ui.screens.BoardsLibraryScreen
import com.kd.brevity.presentation.ui.screens.CanvasViewportScreen
import com.kd.brevity.presentation.ui.screens.PeerConnectScreen
import com.kd.brevity.presentation.ui.screens.SettingsScreen
import com.kd.brevity.presentation.viewmodel.WhiteboardUiState

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
    windowWidthSizeClass: WindowWidthSizeClass,
    onAction: (OotbAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val isLargeScreen = windowWidthSizeClass != WindowWidthSizeClass.Compact
    val isExpanded = windowWidthSizeClass == WindowWidthSizeClass.Expanded

    // Compose Navigation: NavController with spring animations and flipped predictive back slide
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

    BackHandler(enabled = currentRoute != NavDestination.BOARDS) {
        navigateBack()
    }

    val latestUiState by rememberUpdatedState(uiState)

    // Adaptive Side Pane State for Expanded/Desktop Screens
    var sidePaneDestination by remember { mutableStateOf<NavDestination?>(null) }
    var isRailExpanded by remember { mutableStateOf(false) }

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
        Row(modifier = Modifier.fillMaxSize()) {
            // 1. Adaptive Navigation Rail for Tablets and Desktop
            AnimatedVisibility(
                visible = isLargeScreen,
                enter = BrevityMotion.enterSlideRight(),
                exit = BrevityMotion.exitSlideLeft()
            ) {
                FlatAdaptiveNavRail(
                    currentRoute = if (isExpanded && sidePaneDestination != null) sidePaneDestination!! else currentRoute,
                    isExpanded = isRailExpanded,
                    onToggleExpand = { isRailExpanded = !isRailExpanded },
                    canUndo = latestUiState.canUndo,
                    canRedo = latestUiState.canRedo,
                    onUndo = { onAction(OotbAction.Undo) },
                    onRedo = { onAction(OotbAction.Redo) },
                    onNavigate = { dest ->
                        if (isExpanded) {
                            if (dest == NavDestination.CANVAS) {
                                sidePaneDestination = null
                            } else {
                                sidePaneDestination = if (sidePaneDestination == dest) null else dest
                            }
                        } else {
                            navigateTo(dest)
                        }
                    }
                )
            }

            // 2. Main Workspace
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                if (isExpanded) {
                    // Multi-Pane Mode: Always keep Canvas live, with side companion panel if opened
                    Row(modifier = Modifier.fillMaxSize()) {
                        CanvasViewportScreen(
                            uiState = latestUiState,
                            onAction = onAction,
                            onNavigateToDestination = { dest ->
                                if (dest == NavDestination.CANVAS) {
                                    sidePaneDestination = null
                                } else {
                                    sidePaneDestination = dest
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )

                        // Companion Supporting Side-Pane
                        AnimatedVisibility(
                            visible = sidePaneDestination != null,
                            enter = BrevityMotion.enterSlideLeft(),
                            exit = BrevityMotion.exitSlideRight()
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                modifier = Modifier
                                    .width(420.dp)
                                    .fillMaxHeight()
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    when (sidePaneDestination) {
                                        NavDestination.BOARDS -> BoardsLibraryScreen(
                                            uiState = latestUiState,
                                            onAction = onAction,
                                            onNavigateToCanvas = { sidePaneDestination = null },
                                            onNavigateToDestination = { dest ->
                                                sidePaneDestination = if (dest == NavDestination.CANVAS) null else dest
                                            }
                                        )
                                        NavDestination.PEERS -> PeerConnectScreen(
                                            uiState = latestUiState,
                                            onAction = onAction,
                                            onNavigateToCanvas = { sidePaneDestination = null }
                                        )
                                        NavDestination.SETTINGS -> SettingsScreen(
                                            uiState = latestUiState,
                                            onAction = onAction,
                                            onNavigateToCanvas = { sidePaneDestination = null }
                                        )
                                        NavDestination.BIN -> BinScreen(
                                            uiState = latestUiState,
                                            onAction = onAction,
                                            onBackPressed = { sidePaneDestination = null }
                                        )
                                        NavDestination.ABOUT -> AboutScreen(
                                            onBackPressed = { sidePaneDestination = null }
                                        )
                                        else -> {}
                                    }

                                    TooltipHint(text = "Close panel") {
                                        IconButton(
                                            onClick = { sidePaneDestination = null },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .statusBarsPadding()
                                                .padding(16.dp)
                                        ) {
                                            MaterialSymbol(name = "close", tint = MaterialTheme.colorScheme.onSurface, size = 20.dp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Compose Navigation implementation with spring animations and flipped predictive back slide
                    NavHost(
                        navController = navController,
                        startDestination = NavDestination.BOARDS.name,
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = BrevityMotion.springSpatial()
                            ) + fadeIn(animationSpec = BrevityMotion.springEffects())
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = BrevityMotion.springSpatial()
                            ) + fadeOut(animationSpec = BrevityMotion.springEffects())
                        },
                        popEnterTransition = {
                            // Flipped predictive back slide: enters from right instead of left
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = BrevityMotion.springSpatial()
                            ) + fadeIn(animationSpec = BrevityMotion.springEffects())
                        },
                        popExitTransition = {
                            // Flipped predictive back slide: exits to left instead of right
                            slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = BrevityMotion.springSpatial()
                            ) + fadeOut(animationSpec = BrevityMotion.springEffects())
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
                        composable(
                            route = NavDestination.CANVAS.name,
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = BrevityMotion.springSpatial()
                                ) + fadeIn(animationSpec = BrevityMotion.springEffects())
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = BrevityMotion.springSpatial()
                                ) + fadeOut(animationSpec = BrevityMotion.springEffects())
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = BrevityMotion.springSpatial()
                                ) + fadeIn(animationSpec = BrevityMotion.springEffects())
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = BrevityMotion.springSpatial()
                                ) + fadeOut(animationSpec = BrevityMotion.springEffects())
                            }
                        ) {
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
                        composable(
                            route = NavDestination.PEERS.name,
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = BrevityMotion.springSpatial()
                                ) + fadeIn(animationSpec = BrevityMotion.springEffects())
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = BrevityMotion.springSpatial()
                                ) + fadeOut(animationSpec = BrevityMotion.springEffects())
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = BrevityMotion.springSpatial()
                                ) + fadeIn(animationSpec = BrevityMotion.springEffects())
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = BrevityMotion.springSpatial()
                                ) + fadeOut(animationSpec = BrevityMotion.springEffects())
                            }
                        ) {
                            PeerConnectScreen(
                                uiState = latestUiState,
                                onAction = onAction,
                                onNavigateToCanvas = { navigateBack() },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        composable(
                            route = NavDestination.SETTINGS.name,
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = BrevityMotion.springSpatial()
                                ) + fadeIn(animationSpec = BrevityMotion.springEffects())
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = BrevityMotion.springSpatial()
                                ) + fadeOut(animationSpec = BrevityMotion.springEffects())
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = BrevityMotion.springSpatial()
                                ) + fadeIn(animationSpec = BrevityMotion.springEffects())
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = BrevityMotion.springSpatial()
                                ) + fadeOut(animationSpec = BrevityMotion.springEffects())
                            }
                        ) {
                            SettingsScreen(
                                uiState = latestUiState,
                                onAction = onAction,
                                onNavigateToCanvas = { navigateBack() },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        composable(
                            route = NavDestination.BIN.name,
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = BrevityMotion.springSpatial()
                                ) + fadeIn(animationSpec = BrevityMotion.springEffects())
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = BrevityMotion.springSpatial()
                                ) + fadeOut(animationSpec = BrevityMotion.springEffects())
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = BrevityMotion.springSpatial()
                                ) + fadeIn(animationSpec = BrevityMotion.springEffects())
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = BrevityMotion.springSpatial()
                                ) + fadeOut(animationSpec = BrevityMotion.springEffects())
                            }
                        ) {
                            BinScreen(
                                uiState = latestUiState,
                                onAction = onAction,
                                onBackPressed = { navigateBack() },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        composable(
                            route = NavDestination.ABOUT.name,
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = BrevityMotion.springSpatial()
                                ) + fadeIn(animationSpec = BrevityMotion.springEffects())
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = BrevityMotion.springSpatial()
                                ) + fadeOut(animationSpec = BrevityMotion.springEffects())
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = BrevityMotion.springSpatial()
                                ) + fadeIn(animationSpec = BrevityMotion.springEffects())
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = BrevityMotion.springSpatial()
                                ) + fadeOut(animationSpec = BrevityMotion.springEffects())
                            }
                        ) {
                            AboutScreen(
                                onBackPressed = { navigateBack() },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlatAdaptiveNavRail(
    currentRoute: NavDestination,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val railWidth = if (isExpanded) 190.dp else 76.dp

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
            .width(railWidth)
            .fillMaxHeight()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(vertical = 14.dp, horizontal = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: App Logo & Expand Toggle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isExpanded) Arrangement.SpaceBetween else Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        MaterialSymbol(name = "draw", tint = MaterialTheme.colorScheme.onPrimaryContainer, size = 22.dp)
                    }

                    if (isExpanded) {
                        Text(
                            text = "breVITy",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        )
                        TooltipHint(text = "Collapse rail") {
                            IconButton(
                                onClick = onToggleExpand,
                                modifier = Modifier.size(32.dp)
                            ) {
                                MaterialSymbol(name = "chevron_left", tint = MaterialTheme.colorScheme.onSurface, size = 18.dp)
                            }
                        }
                    }
                }

                if (!isExpanded) {
                    TooltipHint(text = "Expand rail") {
                        IconButton(
                            onClick = onToggleExpand,
                            modifier = Modifier.size(32.dp)
                        ) {
                            MaterialSymbol(name = "chevron_right", tint = MaterialTheme.colorScheme.onSurface, size = 18.dp)
                        }
                    }
                }

                // Undo / Redo Quick Controls on Rail
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TooltipHint(text = "Undo") {
                        IconButton(
                            onClick = onUndo,
                            enabled = canUndo,
                            modifier = Modifier.size(34.dp)
                        ) {
                            MaterialSymbol(
                                name = "undo",
                                tint = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                size = 18.dp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    TooltipHint(text = "Redo") {
                        IconButton(
                            onClick = onRedo,
                            enabled = canRedo,
                            modifier = Modifier.size(34.dp)
                        ) {
                            MaterialSymbol(
                                name = "redo",
                                tint = if (canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                size = 18.dp
                            )
                        }
                    }
                }
            }

            // Middle: Navigation Destinations
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NavDestination.entries.forEach { dest ->
                    val isSelected = currentRoute == dest
                    val itemBg = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        Color.Transparent
                    }

                    TooltipHint(text = dest.label) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(itemBg)
                                .clickable { onNavigate(dest) }
                                .padding(vertical = 10.dp, horizontal = if (isExpanded) 12.dp else 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = if (isExpanded) Arrangement.Start else Arrangement.Center
                        ) {
                            MaterialSymbol(
                                name = dest.icon,
                                filled = isSelected,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 22.dp
                            )
                            if (isExpanded) {
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = dest.label,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
