package com.kd.creavity.presentation.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import com.kd.creavity.presentation.ui.components.AdaptiveModalSheet
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.creavity.presentation.ui.BoardAction
import com.kd.creavity.presentation.ui.components.MaterialSymbol
import com.kd.creavity.presentation.ui.components.TooltipHint
import com.kd.creavity.presentation.viewmodel.ThemeMode
import com.kd.creavity.presentation.viewmodel.WhiteboardUiState

private val BACKGROUND_COLOR_OPTIONS = listOf(
    Pair("Studio Dark", Color(0xFF18181C)),
    Pair("Deep Slate Navy", Color(0xFF0F172A)),
    Pair("Warm Espresso", Color(0xFF1C1917)),
    Pair("Pine Black", Color(0xFF141F1A)),
    Pair("Twilight Plum", Color(0xFF1E1424)),
    Pair("Neutral Charcoal", Color(0xFF2B2D35))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onBackPressed: () -> Unit,
    isWideScreen: Boolean = false
) {
    val haptic = LocalHapticFeedback.current

    LargeTopAppBar(
        title = {
            Text(
                text = "Settings",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = if (isWideScreen) 32.dp else 16.dp)
            )
        },
        navigationIcon = {
            if (!isWideScreen) {
                TooltipHint(text = "Back") {
                    FilledIconButton(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(40.dp)
                            .clip(CircleShape),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onBackPressed()
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        )
                    ) {
                        MaterialSymbol(
                            name = "arrow_back",
                            tint = MaterialTheme.colorScheme.primary,
                            size = 20.dp
                        )
                    }
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        content()
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    trailingContent: @Composable () -> Unit = {},
    shape: Shape = RoundedCornerShape(4.dp),
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            })
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier.padding(start = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                trailingContent()
            }
        }
    }
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasBackgroundBottomSheet(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current

    AdaptiveModalSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Canvas Background",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(16.dp))

            BACKGROUND_COLOR_OPTIONS.forEach { (name, color) ->
                val isSelected = color == selectedColor
                val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onColorSelected(color)
                            }
                        )
                        .padding(vertical = 12.dp, horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = name,
                        color = contentColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.W500,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: WhiteboardUiState,
    onAction: (BoardAction) -> Unit,
    onNavigateToCanvas: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var showBackgroundSheet by remember { mutableStateOf(false) }
    var showThemeModeSheet by remember { mutableStateOf(false) }
    var showEditDisplayNameDialog by remember { mutableStateOf(false) }
    var tempDisplayName by remember { mutableStateOf("") }

    val currentBgName = remember(uiState.backgroundColor) {
        BACKGROUND_COLOR_OPTIONS.find { it.second == uiState.backgroundColor }?.first ?: "Custom"
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 720.dp

        Scaffold(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .fillMaxSize(),
            topBar = {
                SettingsTopBar(
                    scrollBehavior = scrollBehavior,
                    onBackPressed = onNavigateToCanvas,
                    isWideScreen = isWideScreen
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = MaterialTheme.colorScheme.surface
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 840.dp)
                        .fillMaxWidth()
                        .padding(horizontal = if (isWideScreen) 24.dp else 0.dp)
                ) {
            // APPEARANCE
            item {
                SettingsSection(title = "Appearance") {
                    Column(modifier = Modifier.clip(shape = RoundedCornerShape(24.dp))) {
                        SettingsItem(
                            title = "App Theme",
                            subtitle = when (uiState.themeMode) {
                                ThemeMode.SYSTEM -> "System Default"
                                ThemeMode.LIGHT -> "Light"
                                ThemeMode.DARK -> "Dark"
                            },
                            trailingContent = {
                                MaterialSymbol(
                                    name = when (uiState.themeMode) {
                                        ThemeMode.SYSTEM -> "brightness_auto"
                                        ThemeMode.LIGHT -> "light_mode"
                                        ThemeMode.DARK -> "dark_mode"
                                    },
                                    tint = MaterialTheme.colorScheme.primary,
                                    size = 22.dp
                                )
                            },
                            onClick = {
                                showThemeModeSheet = true
                            }
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        SettingsItem(
                            title = "Canvas Background",
                            subtitle = currentBgName,
                            trailingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(uiState.backgroundColor)
                                        .border(
                                            width = 1.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                            shape = CircleShape
                                        )
                                )
                            },
                            onClick = {
                                showBackgroundSheet = true
                            }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // INKING & CANVAS
            item {
                SettingsSection(title = "Inking & Canvas") {
                    Column(modifier = Modifier.clip(shape = RoundedCornerShape(24.dp))) {
                        // Default Stroke Width with embedded slider
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "Default Inking Width",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Thickness for pen, pencils & shapes",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${uiState.currentWidth.toInt()} px",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = uiState.currentWidth,
                                    onValueChange = { onAction(BoardAction.ChangeStrokeWidth(it)) },
                                    valueRange = 1f..40f,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        SettingsItem(
                            title = "Tactile Drawing Haptics",
                            subtitle = "Real-time micro friction ticks while inking on glass",
                            trailingContent = {
                                Switch(
                                    checked = uiState.hapticsEnabled,
                                    onCheckedChange = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onAction(BoardAction.ToggleHaptics(it))
                                    }
                                )
                            },
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onAction(BoardAction.ToggleHaptics(!uiState.hapticsEnabled))
                            }
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        SettingsItem(
                            title = "Infinite Canvas",
                            subtitle = if (uiState.isInfiniteCanvas) "Unlimited panning and zooming space" else "Fixed Sheet (16:9)",
                            trailingContent = {
                                Switch(
                                    checked = uiState.isInfiniteCanvas,
                                    onCheckedChange = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onAction(BoardAction.SetInfiniteCanvas(it))
                                    }
                                )
                            },
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onAction(BoardAction.SetInfiniteCanvas(!uiState.isInfiniteCanvas))
                            }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // COLLABORATION
            item {
                SettingsSection(title = "Collaboration") {
                    Column(modifier = Modifier.clip(shape = RoundedCornerShape(24.dp))) {
                        SettingsItem(
                            title = "Connection Name",
                            subtitle = uiState.displayName,
                            trailingContent = {
                                MaterialSymbol(
                                    name = "person",
                                    tint = MaterialTheme.colorScheme.primary,
                                    size = 22.dp
                                )
                            },
                            onClick = {
                                tempDisplayName = uiState.displayName
                                showEditDisplayNameDialog = true
                            }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // ABOUT
            item {
                SettingsSection(title = "About") {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 1. App Icon
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(18.dp))
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    MaterialSymbol(
                                        name = "draw",
                                        size = 32.dp,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            // 2. Name & Version + Code
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "CreaVITy",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "v1.2.0 • Build 2",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // 3. Description
                            Text(
                                text = "High-performance collaborative offline whiteboard with pressure sensitivity, stylus palm rejection, and zero-latency P2P mesh syncing.",
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )

                            // 4. Contributor IDs in outlined badges
                            val contributors = remember {
                                listOf(
                                    Pair("@lavan8t", 69)
                                ).sortedByDescending { it.second }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                contributors.forEach { (contributorId, _) ->
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Color.Transparent,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                        modifier = Modifier.clip(RoundedCornerShape(50))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            MaterialSymbol(
                                                name = "person",
                                                size = 16.dp,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = contributorId,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Bottom Spacer for full immersive scrolling under navigation bar
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

    if (showEditDisplayNameDialog) {
        val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        AdaptiveModalSheet(
            onDismissRequest = { showEditDisplayNameDialog = false },
            sheetState = editSheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Connection Name",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = tempDisplayName,
                    onValueChange = { tempDisplayName = it },
                    label = { Text("Device Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showEditDisplayNameDialog = false }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val trimmed = tempDisplayName.trim()
                            if (trimmed.isNotEmpty()) {
                                onAction(BoardAction.UpdateDisplayName(trimmed))
                            }
                            showEditDisplayNameDialog = false
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showBackgroundSheet) {
        CanvasBackgroundBottomSheet(
            selectedColor = uiState.backgroundColor,
            onColorSelected = { color ->
                onAction(BoardAction.SelectBackgroundColor(color))
                showBackgroundSheet = false
            },
            onDismiss = { showBackgroundSheet = false }
        )
    }

    if (showThemeModeSheet) {
        val themeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        AdaptiveModalSheet(
            onDismissRequest = { showThemeModeSheet = false },
            sheetState = themeSheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "App Theme",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(16.dp))

                listOf(
                    Triple(ThemeMode.SYSTEM, "System Default", "brightness_auto"),
                    Triple(ThemeMode.LIGHT, "Light", "light_mode"),
                    Triple(ThemeMode.DARK, "Dark", "dark_mode")
                ).forEach { (mode, name, icon) ->
                    val isSelected = uiState.themeMode == mode
                    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(),
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onAction(BoardAction.SetThemeMode(mode))
                                    showThemeModeSheet = false
                                }
                            )
                            .padding(vertical = 12.dp, horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MaterialSymbol(
                            name = icon,
                            tint = contentColor,
                            size = 24.dp
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = name,
                            color = contentColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.W500,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            MaterialSymbol(
                                name = "check",
                                tint = MaterialTheme.colorScheme.primary,
                                size = 20.dp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
}

