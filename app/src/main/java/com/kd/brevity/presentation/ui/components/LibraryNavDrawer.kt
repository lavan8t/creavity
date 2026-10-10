package com.kd.brevity.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
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
import com.kd.brevity.presentation.ui.NavDestination
import kotlinx.coroutines.launch

@Composable
fun LibraryNavDrawer(
    drawerState: DrawerState,
    binnedCount: Int,
    onNavigateToDestination: (NavDestination) -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

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
                    // Header: breVITy branding
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            MaterialSymbol(
                                name = "draw",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                size = 24.dp
                            )
                        }

                        Column {
                            Text(
                                text = "breVITy",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Whiteboard Library",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // 1. Whiteboards
                    NavigationDrawerItem(
                        icon = {
                            MaterialSymbol(
                                name = "folder",
                                filled = true,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                size = 22.dp
                            )
                        },
                        label = {
                            Text(
                                text = "Whiteboards",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        selected = true,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            scope.launch { drawerState.close() }
                        },
                        shape = RoundedCornerShape(50),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )

                    // 2. Recycle Bin
                    NavigationDrawerItem(
                        icon = {
                            MaterialSymbol(
                                name = "delete",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 22.dp
                            )
                        },
                        label = {
                            Text(
                                text = "Recycle Bin",
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
                        selected = false,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            scope.launch {
                                drawerState.close()
                                onNavigateToDestination(NavDestination.BIN)
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // 3. Settings
                    NavigationDrawerItem(
                        icon = {
                            MaterialSymbol(
                                name = "settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 22.dp
                            )
                        },
                        label = {
                            Text(
                                text = "Settings",
                                fontWeight = FontWeight.Medium
                            )
                        },
                        selected = false,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            scope.launch {
                                drawerState.close()
                                onNavigateToDestination(NavDestination.SETTINGS)
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    // 4. About
                    NavigationDrawerItem(
                        icon = {
                            MaterialSymbol(
                                name = "info",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 22.dp
                            )
                        },
                        label = {
                            Text(
                                text = "About",
                                fontWeight = FontWeight.Medium
                            )
                        },
                        selected = false,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            scope.launch {
                                drawerState.close()
                                onNavigateToDestination(NavDestination.ABOUT)
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        },
        content = content
    )
}
