package com.kd.creavity.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.creavity.presentation.ui.NavDestination
import kotlinx.coroutines.launch

@Composable
fun LibraryNavDrawer(
    drawerState: DrawerState,
    binnedCount: Int,
    onNavigateToDestination: (NavDestination) -> Unit,
    currentDestination: NavDestination = NavDestination.BOARDS,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // Whiteboard Canvas is full screen: never show navigation rail or modal drawer
    if (currentDestination == NavDestination.CANVAS) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
        return
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 720.dp

        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        ) {
                            Text(
                                text = "CreaVITy",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    Spacer(Modifier.height(8.dp))

                    NavigationRailItem(
                        selected = currentDestination == NavDestination.BOARDS,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNavigateToDestination(NavDestination.BOARDS)
                        },
                        icon = {
                            MaterialSymbol(
                                name = "draw",
                                size = 24.dp,
                                tint = if (currentDestination == NavDestination.BOARDS)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = { Text("Boards") }
                    )

                    NavigationRailItem(
                        selected = currentDestination == NavDestination.PEERS,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNavigateToDestination(NavDestination.PEERS)
                        },
                        icon = {
                            MaterialSymbol(
                                name = "sensors",
                                size = 24.dp,
                                tint = if (currentDestination == NavDestination.PEERS)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = { Text("Connect") }
                    )

                    NavigationRailItem(
                        selected = currentDestination == NavDestination.BIN,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNavigateToDestination(NavDestination.BIN)
                        },
                        icon = {
                            if (binnedCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                                        ) {
                                            Text("$binnedCount")
                                        }
                                    }
                                ) {
                                    MaterialSymbol(
                                        name = "delete",
                                        size = 24.dp,
                                        tint = if (currentDestination == NavDestination.BIN)
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                MaterialSymbol(
                                    name = "delete",
                                    size = 24.dp,
                                    tint = if (currentDestination == NavDestination.BIN)
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        label = { Text("Trash") }
                    )

                    NavigationRailItem(
                        selected = currentDestination == NavDestination.SETTINGS,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNavigateToDestination(NavDestination.SETTINGS)
                        },
                        icon = {
                            MaterialSymbol(
                                name = "settings",
                                size = 24.dp,
                                tint = if (currentDestination == NavDestination.SETTINGS)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = { Text("Settings") }
                    )

                    Spacer(Modifier.weight(1f))

                    NavigationRailItem(
                        selected = currentDestination == NavDestination.ABOUT,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNavigateToDestination(NavDestination.ABOUT)
                        },
                        icon = {
                            MaterialSymbol(
                                name = "info",
                                size = 24.dp,
                                tint = if (currentDestination == NavDestination.ABOUT)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = { Text("About") },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    content()
                }
            }
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier
                            .width(300.dp)
                            .fillMaxHeight()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "CreaVITy",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )

                            // 1. Boards
                            NavigationDrawerItem(
                                icon = {
                                    MaterialSymbol(
                                        name = "draw",
                                        size = 22.dp,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Boards",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                selected = currentDestination == NavDestination.BOARDS,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    scope.launch { drawerState.close() }
                                },
                                shape = RoundedCornerShape(50),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )

                            // 2. Connect
                            NavigationDrawerItem(
                                icon = {
                                    MaterialSymbol(
                                        name = "sensors",
                                        size = 22.dp,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Connect",
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                selected = currentDestination == NavDestination.PEERS,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    scope.launch {
                                        drawerState.close()
                                        onNavigateToDestination(NavDestination.PEERS)
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                colors = NavigationDrawerItemDefaults.colors(
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            // 3. Trash
                            NavigationDrawerItem(
                                icon = {
                                    MaterialSymbol(
                                        name = "delete",
                                        size = 22.dp,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Trash",
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                badge = if (binnedCount > 0) {
                                    {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                                        ) {
                                            Text("$binnedCount")
                                        }
                                    }
                                } else null,
                                selected = currentDestination == NavDestination.BIN,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    scope.launch {
                                        drawerState.close()
                                        onNavigateToDestination(NavDestination.BIN)
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                colors = NavigationDrawerItemDefaults.colors(
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )

                            // 4. Settings
                            NavigationDrawerItem(
                                icon = {
                                    MaterialSymbol(
                                        name = "settings",
                                        size = 22.dp,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Settings",
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                selected = currentDestination == NavDestination.SETTINGS,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    scope.launch {
                                        drawerState.close()
                                        onNavigateToDestination(NavDestination.SETTINGS)
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                colors = NavigationDrawerItemDefaults.colors(
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            // 5. About
                            NavigationDrawerItem(
                                icon = {
                                    MaterialSymbol(
                                        name = "info",
                                        size = 22.dp,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                label = {
                                    Text(
                                        text = "About",
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                selected = currentDestination == NavDestination.ABOUT,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    scope.launch {
                                        drawerState.close()
                                        onNavigateToDestination(NavDestination.ABOUT)
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                colors = NavigationDrawerItemDefaults.colors(
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                },
                content = content
            )
        }
    }
}
