package com.kd.ootb.presentation.ui

import android.app.Activity
import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke as DrawScopeStroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.kd.ootb.domain.model.AppThemePreset
import com.kd.ootb.domain.model.Board
import com.kd.ootb.domain.model.BoardPage
import com.kd.ootb.domain.model.BoardVersionSnapshot
import com.kd.ootb.domain.model.DrawingTool
import com.kd.ootb.domain.model.ImageElement
import com.kd.ootb.domain.model.PeerPresence
import com.kd.ootb.domain.model.Point
import com.kd.ootb.domain.model.Stroke
import com.kd.ootb.domain.model.TextElement
import com.kd.ootb.presentation.canvas.WhiteboardCanvas
import com.kd.ootb.presentation.ui.components.MaterialSymbol
import com.kd.ootb.presentation.ui.glossy.FastPhysicsSpringDp
import com.kd.ootb.presentation.ui.glossy.FastPhysicsSpringFloat
import com.kd.ootb.presentation.ui.glossy.FastPhysicsSpringIntOffset
import com.kd.ootb.presentation.ui.glossy.GlossyBottomSheet
import com.kd.ootb.presentation.ui.glossy.GlossyButton
import com.kd.ootb.presentation.ui.glossy.GlossyCard
import com.kd.ootb.presentation.ui.glossy.GlossyContextualTooltipBar
import com.kd.ootb.presentation.ui.glossy.GlossyIconButton
import com.kd.ootb.presentation.ui.glossy.GlossyMinimap
import com.kd.ootb.presentation.ui.glossy.GlossyPill
import com.kd.ootb.presentation.ui.glossy.GlossySlider
import com.kd.ootb.presentation.ui.glossy.GlossySwitch
import com.kd.ootb.presentation.ui.glossy.GlossyTextField
import com.kd.ootb.presentation.ui.glossy.dynamicErrorGlossy
import com.kd.ootb.presentation.ui.glossy.dynamicPrimaryGlossy
import com.kd.ootb.presentation.ui.glossy.dynamicSurfaceGlass
import com.kd.ootb.presentation.ui.glossy.dynamicTertiaryGlossy
import com.kd.ootb.presentation.viewmodel.WhiteboardUiState
import com.kd.ootb.ui.theme.AVAILABLE_GOOGLE_FONTS
import com.kd.ootb.ui.theme.RobotoFlexOotbTitle
import com.kd.ootb.ui.theme.getGoogleFontFamily
import com.kd.ootb.ui.theme.robotoFlexAppFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val FastPhysicsSpringDp = spring<androidx.compose.ui.unit.Dp>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMedium
)

private val FastPhysicsSpringFloat = spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMedium
)

private const val PHYSICS_FAST_MS = 150

private val STROKE_COLORS = listOf(
    Color.White,
    Color(0xFFDC5F00),
    Color(0xFF00E5FF),
    Color(0xFFFFEA00),
    Color(0xFFFF1744),
    Color(0xFF00E676),
    Color(0xFFE040FB)
)

private val BACKGROUND_COLORS = listOf(
    Color(0xFF18181C),
    Color(0xFF373A40),
    Color(0xFF0F172A),
    Color(0xFFF8FAFC)
)

fun getDrawingToolIcon(tool: DrawingTool): String = when (tool) {
    DrawingTool.PEN -> "draw"
    DrawingTool.PENCIL -> "pencil"
    DrawingTool.HIGHLIGHTER -> "highlighter"
    DrawingTool.LINE -> "line"
    DrawingTool.ARROW -> "arrow"
    DrawingTool.RECTANGLE -> "rectangle"
    DrawingTool.CIRCLE -> "circle"
    DrawingTool.TEXT -> "text"
    DrawingTool.ERASER -> "eraser"
    DrawingTool.SELECT_ERASE -> "select_erase"
    DrawingTool.SELECT_MOVE, DrawingTool.RECT_SELECT -> "rect_select"
    DrawingTool.LASSO_SELECT -> "lasso_select"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AdaptiveWhiteboardScreen(
    uiState: WhiteboardUiState,
    windowWidthSizeClass: WindowWidthSizeClass,
    onStrokeCompleted: (Stroke) -> Unit,
    onStrokeBroadcastStart: (String, Long, Float, Boolean, Point) -> Unit,
    onStrokeBroadcastPoint: (String, Point) -> Unit,
    onStrokeBroadcastEnd: (String) -> Unit,
    onTransform: (Float, Offset) -> Unit,
    onResetTransform: () -> Unit,
    onSelectTool: (DrawingTool) -> Unit,
    onAddTextElement: (String, Float, Float, Color, Float, String) -> Unit,
    onDeleteStroke: (String) -> Unit,
    onDeleteText: (String) -> Unit,
    onElementsMoved: (List<Stroke>, List<TextElement>, List<ImageElement>) -> Unit = { _, _, _ -> },
    onDuplicateElements: (List<String>, List<String>) -> Unit = { _, _ -> },
    onRecolorElements: (List<String>, List<String>, Color) -> Unit = { _, _, _ -> },
    onColorSelected: (Color) -> Unit,
    onBackgroundColorSelected: (Color) -> Unit,
    onWidthChanged: (Float) -> Unit,
    onClearCanvas: () -> Unit,
    onAddNewPage: () -> Unit,
    onSwitchPage: (Int) -> Unit,
    onDeletePage: (Int) -> Unit,
    onCreateNewBoard: (String) -> Unit,
    onSwitchBoard: (Board) -> Unit,
    onRenameBoard: (String, String) -> Unit,
    onDuplicateBoard: (String) -> Unit = {},
    onDeleteBoard: (String) -> Unit,
    onUpdateDisplayName: (String) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onSelectThemePreset: (AppThemePreset) -> Unit = {},
    onStartAdvertising: (String) -> Unit,
    onStopAdvertising: () -> Unit,
    onStartDiscovery: () -> Unit,
    onStopDiscovery: () -> Unit,
    onConnectPeer: (String, String) -> Unit,
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
    onCreateVersionSnapshot: (String) -> Unit = {},
    onRestoreVersionSnapshot: (BoardVersionSnapshot) -> Unit = {},
    onToggleReadOnlyBoard: (String, Boolean) -> Unit = { _, _ -> },
    onSetCanvasTransform: (Float, Offset) -> Unit = { _, _ -> },
    onSetPeerPermission: (String, Boolean) -> Unit = { _, _ -> },
    onAddImageElement: (Uri) -> Unit = {},
    onDeleteImage: (String) -> Unit = {},
    onDisconnectPeer: (String) -> Unit = {},
    onLeaveRoom: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val activity = context as? Activity

    // Immersive mode: Hide status bar in landscape with transient swipe
    DisposableEffect(isLandscape) {
        val window = activity?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (isLandscape) {
                insetsController.hide(WindowInsetsCompat.Type.statusBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.statusBars())
            }
        }
        onDispose {
            val window = activity?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.statusBars())
            }
        }
    }

    fun performHaptic(type: HapticFeedbackType = HapticFeedbackType.TextHandleMove) {
        if (uiState.hapticsEnabled) {
            haptic.performHapticFeedback(type)
        }
    }

    val navController = rememberNavController()

    var isTransforming by remember { mutableStateOf(false) }
    var isFabMenuExpanded by remember { mutableStateOf(false) }
    var selectedHubTab by remember { mutableIntStateOf(0) }

    var showNewBoardDialog by remember { mutableStateOf(false) }
    var newBoardTitle by remember { mutableStateOf("") }

    var showTextDialog by remember { mutableStateOf(false) }
    var textPlacementPoint by remember { mutableStateOf(Point(0f, 0f)) }
    var textInputContent by remember { mutableStateOf("") }
    var selectedGoogleFont by remember { mutableStateOf("Roboto Flex") }
    var textFontSize by remember { mutableFloatStateOf(28f) }

    var showVersionHistorySheet by remember { mutableStateOf(false) }
    val versionHistorySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var activeSelectedStrokeIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var activeSelectedTextIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var activeSelectedImageIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var activeSelectionRect by remember { mutableStateOf<Rect?>(null) }
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }

    var showBoardDetailsCollabSheet by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onAddImageElement(it) }
    }

    val textSheetState = rememberModalBottomSheetState()

    val topChromeOffset by animateDpAsState(
        targetValue = if (isTransforming) (-140).dp else 0.dp,
        animationSpec = FastPhysicsSpringDp,
        label = "topChromeOffset"
    )

    val topChromeAlpha by animateFloatAsState(
        targetValue = if (isTransforming) 0f else 1f,
        animationSpec = FastPhysicsSpringFloat,
        label = "topChromeAlpha"
    )

    val bottomChromeOffset by animateDpAsState(
        targetValue = if (isTransforming) 150.dp else 0.dp,
        animationSpec = FastPhysicsSpringDp,
        label = "bottomChromeOffset"
    )

    val sideChromeOffset by animateDpAsState(
        targetValue = if (isTransforming) 150.dp else 0.dp,
        animationSpec = FastPhysicsSpringDp,
        label = "sideChromeOffset"
    )

    val bottomChromeAlpha by animateFloatAsState(
        targetValue = if (isTransforming) 0f else 1f,
        animationSpec = FastPhysicsSpringFloat,
        label = "bottomChromeAlpha"
    )

    NavHost(
        navController = navController,
        startDestination = "boards",
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = FastPhysicsSpringIntOffset
            ) + fadeIn(tween(PHYSICS_FAST_MS))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 4 },
                animationSpec = FastPhysicsSpringIntOffset
            ) + fadeOut(tween(PHYSICS_FAST_MS))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 4 },
                animationSpec = FastPhysicsSpringIntOffset
            ) + fadeIn(tween(PHYSICS_FAST_MS))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = FastPhysicsSpringIntOffset
            ) + fadeOut(tween(PHYSICS_FAST_MS))
        },
        modifier = Modifier
            .fillMaxSize()
            .background(uiState.backgroundColor)
    ) {
        composable("boards") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush = Brush.verticalGradient(dynamicSurfaceGlass()))
                    .drawBehind {
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.18f),
                                    Color.White.copy(alpha = 0.0f)
                                ),
                                startY = 0f,
                                endY = this.size.height * 0.35f
                            ),
                            size = Size(this.size.width, this.size.height * 0.35f)
                        )
                    }
                    .windowInsetsPadding(if (isLandscape) WindowInsets(0.dp) else WindowInsets.statusBars)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.size(40.dp))

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            val tabWidth = 100.dp
                            val tabHeight = 36.dp
                            val pillPadding = 3.dp

                            val activeThumbOffset by animateDpAsState(
                                targetValue = if (selectedHubTab == 0) 0.dp else tabWidth + 4.dp,
                                animationSpec = FastPhysicsSpringDp,
                                label = "activeTabOffset"
                            )

                            GlossyPill(
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.height(tabHeight + pillPadding * 2)
                            ) {
                                Box(modifier = Modifier.padding(pillPadding)) {
                                    Box(
                                        modifier = Modifier
                                            .offset(x = activeThumbOffset)
                                            .width(tabWidth)
                                            .height(tabHeight)
                                            .clip(RoundedCornerShape(17.dp))
                                            .background(brush = Brush.verticalGradient(dynamicPrimaryGlossy()))
                                            .border(
                                                width = 1.dp,
                                                brush = Brush.verticalGradient(
                                                    listOf(
                                                        Color.White.copy(alpha = 0.55f),
                                                        Color.White.copy(alpha = 0.15f),
                                                        Color.Black.copy(alpha = 0.35f)
                                                    )
                                                ),
                                                shape = RoundedCornerShape(17.dp)
                                            )
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        listOf("Boards", "Connect").forEachIndexed { index, title ->
                                            val isSelected = selectedHubTab == index
                                            Box(
                                                modifier = Modifier
                                                    .width(tabWidth)
                                                    .height(tabHeight)
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = null
                                                    ) {
                                                        performHaptic(HapticFeedbackType.TextHandleMove)
                                                        selectedHubTab = index
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = title,
                                                    fontSize = 14.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else Color(0xFFB0B4C0)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        GlossyIconButton(
                            icon = "settings",
                            onClick = {
                                performHaptic(HapticFeedbackType.TextHandleMove)
                                navController.navigate("settings")
                            },
                            size = 40.dp,
                            iconSize = 20.dp,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.10f))

                    AnimatedContent(
                        targetState = selectedHubTab,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally(animationSpec = FastPhysicsSpringIntOffset) { it } + fadeIn(tween(PHYSICS_FAST_MS))).togetherWith(
                                    slideOutHorizontally(animationSpec = FastPhysicsSpringIntOffset) { -it } + fadeOut(tween(PHYSICS_FAST_MS))
                                )
                            } else {
                                (slideInHorizontally(animationSpec = FastPhysicsSpringIntOffset) { -it } + fadeIn(tween(PHYSICS_FAST_MS))).togetherWith(
                                    slideOutHorizontally(animationSpec = FastPhysicsSpringIntOffset) { it } + fadeOut(tween(PHYSICS_FAST_MS))
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        label = "boardsConnectSlide"
                    ) { tabIndex ->
                        when (tabIndex) {
                            0 -> GlossyBoardsTabPage(
                                uiState = uiState,
                                showNewBoardDialog = showNewBoardDialog,
                                newBoardTitle = newBoardTitle,
                                onNewBoardTitleChange = { newBoardTitle = it },
                                onToggleNewBoardDialog = { showNewBoardDialog = it },
                                onCreateNewBoard = { title ->
                                    onCreateNewBoard(title)
                                    navController.navigate("canvas")
                                },
                                onSwitchBoard = { board ->
                                    onSwitchBoard(board)
                                    navController.navigate("canvas")
                                },
                                onRenameBoard = onRenameBoard,
                                onDuplicateBoard = onDuplicateBoard,
                                onDeleteBoard = onDeleteBoard,
                                onToggleReadOnlyBoard = onToggleReadOnlyBoard,
                                onPerformHaptic = { performHaptic() }
                            )
                            1 -> GlossyConnectTabPage(
                                uiState = uiState,
                                onUpdateDisplayName = onUpdateDisplayName,
                                onStartAdvertising = onStartAdvertising,
                                onStopAdvertising = onStopAdvertising,
                                onStartDiscovery = onStartDiscovery,
                                onStopDiscovery = onStopDiscovery,
                                onConnectPeer = onConnectPeer,
                                onCopyRoomCode = { code ->
                                    clipboardManager.setText(AnnotatedString(code))
                                    Toast.makeText(context, "Room code copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                onSwitchBoard = { board ->
                                    onSwitchBoard(board)
                                    navController.navigate("canvas")
                                },
                                onSetPeerPermission = onSetPeerPermission,
                                onDisconnectPeer = onDisconnectPeer,
                                onLeaveRoom = onLeaveRoom,
                                onPerformHaptic = { performHaptic() }
                            )
                        }
                    }
                }
            }
        }

        composable("settings") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush = Brush.verticalGradient(dynamicSurfaceGlass()))
                    .drawBehind {
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.18f),
                                    Color.White.copy(alpha = 0.0f)
                                ),
                                startY = 0f,
                                endY = this.size.height * 0.35f
                            ),
                            size = Size(this.size.width, this.size.height * 0.35f)
                        )
                    }
                    .windowInsetsPadding(if (isLandscape) WindowInsets(0.dp) else WindowInsets.statusBars)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlossyIconButton(
                            icon = "arrow_back",
                            onClick = {
                                performHaptic(HapticFeedbackType.TextHandleMove)
                                navController.popBackStack()
                            },
                            size = 40.dp,
                            iconSize = 20.dp,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text(
                            text = "Settings",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.size(40.dp))
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.10f))

                    GlossySettingsTabPage(
                        uiState = uiState,
                        onBackgroundColorSelected = onBackgroundColorSelected,
                        onWidthChanged = onWidthChanged,
                        onToggleHaptics = onToggleHaptics,
                        onSelectThemePreset = onSelectThemePreset,
                        onPerformHaptic = { performHaptic() }
                    )
                }
            }
        }

        composable("canvas") {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(uiState.backgroundColor)
            ) {
                val screenMaxWidth = maxWidth
                val screenMaxHeight = maxHeight

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { canvasSize = it }
                ) {
                    WhiteboardCanvas(
                        strokes = uiState.strokes,
                        textElements = uiState.textElements,
                        imageElements = uiState.imageElements,
                        peerPresences = uiState.livePeerPresences,
                        currentColor = uiState.currentColor,
                        currentWidth = uiState.currentWidth,
                        selectedTool = uiState.selectedTool,
                        backgroundColor = uiState.backgroundColor,
                        zoom = uiState.zoom,
                        panOffset = uiState.panOffset,
                        onTransform = onTransform,
                        onTransformStateChanged = { isTransforming = it },
                        onStrokeCompleted = onStrokeCompleted,
                        onStrokeBroadcastStart = onStrokeBroadcastStart,
                        onStrokeBroadcastPoint = onStrokeBroadcastPoint,
                        onStrokeBroadcastEnd = onStrokeBroadcastEnd,
                        onDeleteStroke = { id ->
                            performHaptic(HapticFeedbackType.LongPress)
                            onDeleteStroke(id)
                        },
                        onDeleteText = { id ->
                            performHaptic(HapticFeedbackType.LongPress)
                            onDeleteText(id)
                        },
                        onDeleteImage = { id ->
                            performHaptic(HapticFeedbackType.LongPress)
                            onDeleteImage(id)
                        },
                        onElementsMoved = onElementsMoved,
                        onSelectionChanged = { strokeIds, textIds, rect ->
                            activeSelectedStrokeIds = strokeIds
                            activeSelectedTextIds = textIds
                            activeSelectionRect = rect
                        },
                        onTapForText = { point ->
                            performHaptic(HapticFeedbackType.TextHandleMove)
                            textPlacementPoint = point
                            textInputContent = ""
                            showTextDialog = true
                        },
                        isReadOnly = uiState.isCanvasReadOnly,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = topChromeOffset)
                            .alpha(topChromeAlpha)
                            .windowInsetsPadding(if (isLandscape) WindowInsets(0.dp) else WindowInsets.statusBars)
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        GlossyPill(
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .height(36.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                GlossyIconButton(
                                    icon = "arrow_back",
                                    onClick = {
                                        performHaptic(HapticFeedbackType.TextHandleMove)
                                        navController.popBackStack()
                                    },
                                    size = 28.dp,
                                    iconSize = 16.dp,
                                    shape = RoundedCornerShape(14.dp)
                                )
                            }
                        }

                        val boardHeaderInteraction = remember { MutableInteractionSource() }
                        val isBoardHeaderPressed by boardHeaderInteraction.collectIsPressedAsState()
                        val boardHeaderPush by animateDpAsState(
                            if (isBoardHeaderPressed) 2.dp else 0.dp,
                            animationSpec = FastPhysicsSpringDp,
                            label = "boardHeaderPush"
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(y = boardHeaderPush)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF14161B).copy(alpha = 0.94f))
                                .border(
                                    1.dp,
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.25f),
                                            Color.White.copy(alpha = 0.05f),
                                            Color.Black.copy(alpha = 0.65f)
                                        )
                                    ),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable(
                                    interactionSource = boardHeaderInteraction,
                                    indication = null
                                ) {
                                    performHaptic(HapticFeedbackType.TextHandleMove)
                                    showBoardDetailsCollabSheet = true
                                }
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = uiState.currentBoard.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = robotoFlexAppFontFamily,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val drawingPeer = uiState.livePeerPresences.values.firstOrNull { it.isDrawing }
                                val connectedCount = uiState.connectedPeers.size
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val dotColor = when {
                                        drawingPeer != null -> Color(drawingPeer.color)
                                        connectedCount > 0 -> Color(0xFF00E676)
                                        uiState.isCanvasReadOnly -> Color(0xFFFF9D42)
                                        else -> Color(0xFF7E8494)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                    )
                                    val statusSubtitle = when {
                                        drawingPeer != null -> "${drawingPeer.name} is drawing"
                                        connectedCount > 0 -> "$connectedCount ${if (connectedCount == 1) "user" else "users"} connected"
                                        uiState.isCanvasReadOnly -> if (uiState.myAccessReadOnly) "View only access" else "View only"
                                        else -> "Solo canvas"
                                    }
                                    Text(
                                        text = statusSubtitle,
                                        fontSize = 10.sp,
                                        fontFamily = robotoFlexAppFontFamily,
                                        color = if (connectedCount > 0 || drawingPeer != null) Color(0xFF00E676) else if (uiState.isCanvasReadOnly) Color(0xFFFF9D42) else Color(0xFF9EABB8)
                                    )
                                }
                            }
                        }

                        GlossyPill(
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .height(36.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                GlossyIconButton(
                                    icon = "undo",
                                    onClick = {
                                        performHaptic(HapticFeedbackType.TextHandleMove)
                                        onUndo()
                                    },
                                    size = 28.dp,
                                    iconSize = 14.dp,
                                    shape = RoundedCornerShape(14.dp)
                                )
                            }
                        }
                    }

                    if (activeSelectedStrokeIds.isNotEmpty() || activeSelectedTextIds.isNotEmpty() || activeSelectedImageIds.isNotEmpty()) {
                        val density = LocalDensity.current
                        val rect = activeSelectionRect

                        val tooltipWidthPx = with(density) { 260.dp.toPx() }
                        val tooltipHeightPx = with(density) { 52.dp.toPx() }
                        val screenWidthPx = with(density) { screenMaxWidth.toPx() }
                        val screenHeightPx = with(density) { screenMaxHeight.toPx() }

                        val (targetXDp, targetYDp) = if (rect != null) {
                            val screenLeft = rect.left * uiState.zoom + uiState.panOffset.x
                            val screenTop = rect.top * uiState.zoom + uiState.panOffset.y
                            val screenRight = rect.right * uiState.zoom + uiState.panOffset.x
                            val screenBottom = rect.bottom * uiState.zoom + uiState.panOffset.y
                            val screenCenterX = (screenLeft + screenRight) / 2f

                            val targetX = (screenCenterX - tooltipWidthPx / 2f).coerceIn(16f, (screenWidthPx - tooltipWidthPx - 16f).coerceAtLeast(16f))
                            val preferredY = screenTop - tooltipHeightPx - 14f
                            val targetY = if (preferredY >= 70f) {
                                preferredY
                            } else {
                                (screenBottom + 14f).coerceIn(70f, (screenHeightPx - tooltipHeightPx - 90f).coerceAtLeast(70f))
                            }
                            with(density) { Pair(targetX.toDp(), targetY.toDp()) }
                        } else {
                            with(density) { Pair(((screenWidthPx - tooltipWidthPx) / 2f).toDp(), 80.dp) }
                        }

                        Box(
                            modifier = Modifier
                                .offset(x = targetXDp, y = targetYDp)
                                .alpha(topChromeAlpha)
                        ) {
                            GlossyContextualTooltipBar(
                                onCopy = {
                                    performHaptic()
                                    onDuplicateElements(activeSelectedStrokeIds, activeSelectedTextIds)
                                },
                                onDelete = {
                                    performHaptic(HapticFeedbackType.LongPress)
                                    activeSelectedStrokeIds.forEach { onDeleteStroke(it) }
                                    activeSelectedTextIds.forEach { onDeleteText(it) }
                                    activeSelectedImageIds.forEach { onDeleteImage(it) }
                                    activeSelectedStrokeIds = emptyList()
                                    activeSelectedTextIds = emptyList()
                                    activeSelectedImageIds = emptyList()
                                },
                                onColorSelected = { color ->
                                    performHaptic()
                                    onRecolorElements(activeSelectedStrokeIds, activeSelectedTextIds, color)
                                },
                                onDeselect = {
                                    performHaptic()
                                    activeSelectedStrokeIds = emptyList()
                                    activeSelectedTextIds = emptyList()
                                    activeSelectedImageIds = emptyList()
                                },
                                strokeColors = STROKE_COLORS
                            )
                        }
                    }

                    // MINIMAP — top-left, visible only while panning/zooming
                    if (isTransforming) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .windowInsetsPadding(WindowInsets.statusBars)
                                .padding(start = 16.dp, top = 52.dp)
                        ) {
                            GlossyMinimap(
                                strokes = uiState.strokes,
                                textElements = uiState.textElements,
                                zoom = uiState.zoom,
                                panOffset = uiState.panOffset,
                                canvasSize = canvasSize,
                                onPanTo = { targetPan ->
                                    performHaptic(HapticFeedbackType.TextHandleMove)
                                    onSetCanvasTransform(uiState.zoom, targetPan)
                                }
                            )
                        }
                    }

                    // Zoom % reset button — bottom-left, only when transformed
                    if (uiState.zoom != 1.0f || uiState.panOffset != Offset.Zero) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .offset(y = bottomChromeOffset)
                                .alpha(bottomChromeAlpha)
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .padding(start = 16.dp, bottom = 14.dp)
                                .height(38.dp)
                                .shadow(3.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.verticalGradient(listOf(Color(0xFF2E323B), Color(0xFF1C1E24), Color(0xFF141518))))
                                .border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.50f), Color.Black.copy(alpha = 0.50f))), RoundedCornerShape(12.dp))
                                .clickable {
                                    performHaptic(HapticFeedbackType.TextHandleMove)
                                    onResetTransform()
                                }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${(uiState.zoom * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // 3-COLUMN GRID FAB SPEED DIAL TOOLS (12 Tools)
                    val fabGridTools = listOf(
                        Pair(DrawingTool.PEN, "draw"),
                        Pair(DrawingTool.PENCIL, "pencil"),
                        Pair(DrawingTool.HIGHLIGHTER, "highlighter"),
                        Pair(DrawingTool.LINE, "line"),
                        Pair(DrawingTool.ARROW, "arrow"),
                        Pair(DrawingTool.TEXT, "text"),
                        Pair(DrawingTool.RECTANGLE, "rectangle"),
                        Pair(DrawingTool.CIRCLE, "circle"),
                        Pair(DrawingTool.ERASER, "eraser"),
                        Pair(DrawingTool.RECT_SELECT, "rect_select"),
                        Pair(DrawingTool.LASSO_SELECT, "lasso_select"),
                        Pair(DrawingTool.SELECT_ERASE, "select_erase")
                    )

                    val toolBoundsMap = remember { mutableMapOf<DrawingTool, Rect>() }
                    var fabRootPosition by remember { mutableStateOf(Offset.Zero) }

                    val swatchInteraction = remember { MutableInteractionSource() }
                    val isSwatchPressed by swatchInteraction.collectIsPressedAsState()
                    val swatchPush by animateDpAsState(
                        if (isSwatchPressed) 1.5.dp else 0.dp,
                        animationSpec = FastPhysicsSpringDp,
                        label = "swatchPush"
                    )

                    val fabShape = RoundedCornerShape(14.dp)
                    val fabInteraction = remember { MutableInteractionSource() }
                    val isFabPressed by fabInteraction.collectIsPressedAsState()
                    val fabPush by animateDpAsState(
                        if (isFabPressed) 2.dp else 0.dp,
                        animationSpec = FastPhysicsSpringDp,
                        label = "fabPush"
                    )

                    if (!isLandscape) {
                        // PORTRAIT LAYOUT
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .offset(y = bottomChromeOffset)
                                .alpha(bottomChromeAlpha)
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Column(
                                modifier = Modifier.align(Alignment.BottomEnd),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AnimatedVisibility(
                                    visible = isFabMenuExpanded,
                                    enter = fadeIn(tween(PHYSICS_FAST_MS)) + scaleIn(tween(PHYSICS_FAST_MS), transformOrigin = TransformOrigin(0.5f, 1f)) + slideInVertically(tween(PHYSICS_FAST_MS)) { it / 2 },
                                    exit = fadeOut(tween(100)) + scaleOut(tween(100), transformOrigin = TransformOrigin(0.5f, 1f)) + slideOutVertically(tween(100)) { it / 2 },
                                    modifier = Modifier.padding(bottom = 0.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        GlossyPill(shape = RoundedCornerShape(16.dp)) {
                                            Column(
                                                modifier = Modifier.padding(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                fabGridTools.chunked(3).forEach { rowTools ->
                                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        rowTools.forEach { (tool, icon) ->
                                                            val isSelected = uiState.selectedTool == tool
                                                            GlossyIconButton(
                                                                icon = icon,
                                                                onClick = {
                                                                    performHaptic(HapticFeedbackType.TextHandleMove)
                                                                    onSelectTool(tool)
                                                                    isFabMenuExpanded = false
                                                                },
                                                                isSelected = isSelected,
                                                                size = 40.dp,
                                                                iconSize = 20.dp,
                                                                modifier = Modifier.onGloballyPositioned { coordinates ->
                                                                    toolBoundsMap[tool] = coordinates.boundsInRoot()
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        GlossyButton(
                                            onClick = {
                                                performHaptic()
                                                isFabMenuExpanded = false
                                                imagePickerLauncher.launch("image/*")
                                            },
                                            gradient = dynamicTertiaryGlossy(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            MaterialSymbol(name = "image", tint = Color.White, size = 16.dp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Add Image", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 1. Color Swatch
                                    Box(
                                        modifier = Modifier
                                            .offset(y = swatchPush)
                                            .size(52.dp)
                                            .shadow(if (isSwatchPressed) 1.dp else 4.dp, RoundedCornerShape(14.dp))
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0xFF14161B).copy(alpha = 0.94f))
                                            .border(1.5.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.40f), Color.Black.copy(alpha = 0.50f))), RoundedCornerShape(14.dp))
                                            .clickable(interactionSource = swatchInteraction, indication = null) {
                                                performHaptic(HapticFeedbackType.TextHandleMove)
                                                val nextIndex = (STROKE_COLORS.indexOf(uiState.currentColor) + 1) % STROKE_COLORS.size
                                                onColorSelected(STROKE_COLORS[nextIndex])
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(uiState.currentColor)
                                                .border(1.5.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                                        )
                                    }

                                    // 2. Tool FAB
                                    Box(
                                        modifier = Modifier
                                            .offset(y = fabPush)
                                            .size(52.dp)
                                            .shadow(if (isFabPressed) 1.dp else 5.dp, fabShape)
                                            .clip(fabShape)
                                            .background(brush = Brush.verticalGradient(if (isFabMenuExpanded) dynamicSurfaceGlass() else dynamicPrimaryGlossy()))
                                            .border(1.5.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.70f), Color.White.copy(alpha = 0.15f), Color.Black.copy(alpha = 0.45f))), fabShape)
                                            .drawBehind {
                                                drawRect(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.50f), Color.Transparent), startY = 0f, endY = size.height * 0.52f), size = Size(size.width, size.height * 0.52f))
                                            }
                                            .onGloballyPositioned { coordinates ->
                                                fabRootPosition = coordinates.positionInRoot()
                                            }
                                            .pointerInput(uiState.selectedTool) {
                                                detectDragGesturesAfterLongPress(
                                                    onDragStart = {
                                                        performHaptic(HapticFeedbackType.LongPress)
                                                        isFabMenuExpanded = true
                                                    },
                                                    onDrag = { change, _ ->
                                                        val pointerInRoot = fabRootPosition + change.position
                                                        for ((tool, rect) in toolBoundsMap) {
                                                            if (rect.contains(pointerInRoot)) {
                                                                if (uiState.selectedTool != tool) {
                                                                    performHaptic(HapticFeedbackType.TextHandleMove)
                                                                    onSelectTool(tool)
                                                                }
                                                                break
                                                            }
                                                        }
                                                    },
                                                    onDragEnd = {
                                                        isFabMenuExpanded = false
                                                    },
                                                    onDragCancel = {
                                                        isFabMenuExpanded = false
                                                    }
                                                )
                                            }
                                            .clickable(interactionSource = fabInteraction, indication = null) {
                                                performHaptic(HapticFeedbackType.TextHandleMove)
                                                isFabMenuExpanded = !isFabMenuExpanded
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        MaterialSymbol(
                                            name = getDrawingToolIcon(uiState.selectedTool),
                                            tint = Color.White,
                                            size = 24.dp
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // LANDSCAPE LAYOUT
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .align(Alignment.CenterEnd)
                                .offset(x = sideChromeOffset)
                                .alpha(bottomChromeAlpha)
                                .windowInsetsPadding(WindowInsets.statusBars)
                                .padding(end = 16.dp, top = 16.dp, bottom = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.align(Alignment.CenterEnd),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AnimatedVisibility(
                                    visible = isFabMenuExpanded,
                                    enter = fadeIn(tween(PHYSICS_FAST_MS)) + scaleIn(tween(PHYSICS_FAST_MS), transformOrigin = TransformOrigin(1f, 0.5f)) + slideInHorizontally(tween(PHYSICS_FAST_MS)) { it / 2 },
                                    exit = fadeOut(tween(100)) + scaleOut(tween(100), transformOrigin = TransformOrigin(1f, 0.5f)) + slideOutHorizontally(tween(100)) { it / 2 }
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        GlossyPill(shape = RoundedCornerShape(16.dp)) {
                                            Column(
                                                modifier = Modifier.padding(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                fabGridTools.chunked(3).forEach { rowTools ->
                                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        rowTools.forEach { (tool, icon) ->
                                                            val isSelected = uiState.selectedTool == tool
                                                            GlossyIconButton(
                                                                icon = icon,
                                                                onClick = {
                                                                    performHaptic(HapticFeedbackType.TextHandleMove)
                                                                    onSelectTool(tool)
                                                                    isFabMenuExpanded = false
                                                                },
                                                                isSelected = isSelected,
                                                                size = 40.dp,
                                                                iconSize = 20.dp,
                                                                modifier = Modifier.onGloballyPositioned { coordinates ->
                                                                    toolBoundsMap[tool] = coordinates.boundsInRoot()
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        GlossyButton(
                                            onClick = {
                                                performHaptic()
                                                isFabMenuExpanded = false
                                                imagePickerLauncher.launch("image/*")
                                            },
                                            gradient = dynamicTertiaryGlossy(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            MaterialSymbol(name = "image", tint = Color.White, size = 16.dp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Add Image", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .offset(y = swatchPush)
                                        .size(52.dp)
                                        .shadow(if (isSwatchPressed) 1.dp else 4.dp, RoundedCornerShape(14.dp))
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF14161B).copy(alpha = 0.94f))
                                        .border(1.5.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.40f), Color.Black.copy(alpha = 0.50f))), RoundedCornerShape(14.dp))
                                        .clickable(interactionSource = swatchInteraction, indication = null) {
                                            performHaptic(HapticFeedbackType.TextHandleMove)
                                            val nextIndex = (STROKE_COLORS.indexOf(uiState.currentColor) + 1) % STROKE_COLORS.size
                                            onColorSelected(STROKE_COLORS[nextIndex])
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(uiState.currentColor)
                                            .border(1.5.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .offset(y = fabPush)
                                        .size(52.dp)
                                        .shadow(if (isFabPressed) 1.dp else 5.dp, fabShape)
                                        .clip(fabShape)
                                        .background(brush = Brush.verticalGradient(if (isFabMenuExpanded) dynamicSurfaceGlass() else dynamicPrimaryGlossy()))
                                        .border(1.5.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.70f), Color.White.copy(alpha = 0.15f), Color.Black.copy(alpha = 0.45f))), fabShape)
                                        .drawBehind {
                                            drawRect(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.50f), Color.Transparent), startY = 0f, endY = size.height * 0.52f), size = Size(size.width, size.height * 0.52f))
                                        }
                                        .onGloballyPositioned { coordinates ->
                                            fabRootPosition = coordinates.positionInRoot()
                                        }
                                        .pointerInput(uiState.selectedTool) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = {
                                                    performHaptic(HapticFeedbackType.LongPress)
                                                    isFabMenuExpanded = true
                                                },
                                                onDrag = { change, _ ->
                                                    val pointerInRoot = fabRootPosition + change.position
                                                    for ((tool, rect) in toolBoundsMap) {
                                                        if (rect.contains(pointerInRoot)) {
                                                            if (uiState.selectedTool != tool) {
                                                                performHaptic(HapticFeedbackType.TextHandleMove)
                                                                onSelectTool(tool)
                                                            }
                                                            break
                                                        }
                                                    }
                                                },
                                                onDragEnd = {
                                                    isFabMenuExpanded = false
                                                },
                                                onDragCancel = {
                                                    isFabMenuExpanded = false
                                                }
                                            )
                                        }
                                        .clickable(interactionSource = fabInteraction, indication = null) {
                                            performHaptic(HapticFeedbackType.TextHandleMove)
                                            isFabMenuExpanded = !isFabMenuExpanded
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    MaterialSymbol(
                                        name = getDrawingToolIcon(uiState.selectedTool),
                                        tint = Color.White,
                                        size = 24.dp
                                    )
                                }
                            }
                        }
                    }
                }

                // Tap Outside Dismiss for FAB Speed Dial Menu
                if (isFabMenuExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                performHaptic()
                                isFabMenuExpanded = false
                            }
                    )
                }

        // Text Placement Sheet with Google Fonts Fetcher (Web 2.0 Glossy Sheet)
        if (showTextDialog) {
            GlossyBottomSheet(
                onDismissRequest = { showTextDialog = false },
                sheetState = textSheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Add Text with Google Fonts",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Skeuomorphic Inset Typing Field with Live Font Preview
                    GlossyTextField(
                        value = textInputContent,
                        onValueChange = { textInputContent = it },
                        placeholder = "Type text to place on board...",
                        fontFamily = getGoogleFontFamily(selectedGoogleFont),
                        fontSize = textFontSize.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Select Google Font",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE2E4E9),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(AVAILABLE_GOOGLE_FONTS) { fontName ->
                            val isSelected = fontName == selectedGoogleFont
                            GlossyButton(
                                onClick = {
                                    performHaptic(HapticFeedbackType.TextHandleMove)
                                    selectedGoogleFont = fontName
                                },
                                gradient = if (isSelected) dynamicPrimaryGlossy() else dynamicSurfaceGlass(),
                                shape = RoundedCornerShape(10.dp),
                                bevelHeight = 2.dp
                            ) {
                                Text(
                                    text = fontName,
                                    fontFamily = getGoogleFontFamily(fontName),
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Font Size", fontSize = 14.sp, color = Color.White)
                        Text("${textFontSize.toInt()} sp", fontSize = 14.sp, color = Color(0xFFFF9D42), fontWeight = FontWeight.Bold)
                    }

                    // Skeuomorphic 3D Inset Groove Slider
                    GlossySlider(
                        value = textFontSize,
                        onValueChange = { textFontSize = it },
                        valueRange = 14f..72f
                    )

                    GlossyButton(
                        onClick = {
                            performHaptic(HapticFeedbackType.TextHandleMove)
                            onAddTextElement(
                                textInputContent,
                                textPlacementPoint.x,
                                textPlacementPoint.y,
                                uiState.currentColor,
                                textFontSize,
                                selectedGoogleFont
                            )
                            showTextDialog = false
                        },
                        gradient = dynamicPrimaryGlossy(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Place Text on Board", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // VERSION HISTORY BOTTOM SHEET (Genuine Skeuomorphic Web 2.0 Sheet)
        if (showVersionHistorySheet) {
            val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
            val history = uiState.currentBoard.versionHistory

            GlossyBottomSheet(
                onDismissRequest = { showVersionHistorySheet = false },
                sheetState = versionHistorySheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Version History",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${history.size} snapshots recorded",
                                fontSize = 12.sp,
                                color = Color(0xFF9EA4B4)
                            )
                        }

                        GlossyButton(
                            onClick = {
                                performHaptic(HapticFeedbackType.TextHandleMove)
                                onCreateVersionSnapshot("Manual Snapshot #${history.size + 1}")
                            },
                            gradient = dynamicPrimaryGlossy(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            MaterialSymbol(name = "add", tint = Color.White, size = 16.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save State", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    if (history.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MaterialSymbol(name = "history", tint = Color.White.copy(alpha = 0.3f), size = 36.dp)
                                Text(
                                    text = "No version snapshots yet.\nTap 'Save State' or keep drawing to record milestones.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF7E8494),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(history, key = { it.id }) { snapshot ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF14161B))
                                        .border(
                                            1.dp,
                                            Brush.verticalGradient(
                                                listOf(Color.White.copy(alpha = 0.20f), Color.Black.copy(alpha = 0.50f))
                                            ),
                                            RoundedCornerShape(14.dp)
                                        )
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    StrokesThumbnailPreview(
                                        strokes = snapshot.strokes,
                                        backgroundColor = uiState.currentBoard.backgroundColor,
                                        modifier = Modifier.size(56.dp)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = snapshot.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "By ${snapshot.author} • ${dateFormat.format(Date(snapshot.timestamp))}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF9EA4B4)
                                        )
                                        Text(
                                            text = "${snapshot.strokeCount} strokes",
                                            fontSize = 11.sp,
                                            color = Color(0xFF00E676)
                                        )
                                    }

                                    GlossyButton(
                                        onClick = {
                                            performHaptic(HapticFeedbackType.LongPress)
                                            onRestoreVersionSnapshot(snapshot)
                                            showVersionHistorySheet = false
                                        },
                                        gradient = dynamicSurfaceGlass(),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        MaterialSymbol(name = "restore", tint = Color.White, size = 14.dp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Restore", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // BOARD DETAILS / COLLAB SHEET (triggered by top center board header pill)
        if (showBoardDetailsCollabSheet) {
            val boardDetailsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }
            GlossyBottomSheet(
                onDismissRequest = { showBoardDetailsCollabSheet = false },
                sheetState = boardDetailsSheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        BoardThumbnailPreview(board = uiState.currentBoard, modifier = Modifier.size(56.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(uiState.currentBoard.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = robotoFlexAppFontFamily, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("Modified ${dateFormat.format(Date(uiState.currentBoard.modifiedAt))}", fontSize = 11.sp, color = Color(0xFF9EA4B4))
                            Text("${uiState.currentBoard.pages.size} page${if (uiState.currentBoard.pages.size != 1) "s" else ""}", fontSize = 11.sp, color = Color(0xFF9EA4B4))
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    if (uiState.roomCode != null) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Room Code", fontSize = 11.sp, color = Color(0xFF9EA4B4))
                                Text("#${uiState.roomCode}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676), fontFamily = robotoFlexAppFontFamily)
                            }
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    }

                    if (uiState.connectedPeers.isNotEmpty()) {
                        Text("${uiState.connectedPeers.size} ${if (uiState.connectedPeers.size == 1) "person" else "people"} connected", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        uiState.connectedPeers.forEach { peer ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    MaterialSymbol(name = "devices", tint = if (peer.isReadOnly) Color(0xFFFF9D42) else Color(0xFF00E676), size = 18.dp)
                                    Column {
                                        Text(peer.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White, fontFamily = robotoFlexAppFontFamily)
                                        Text(if (peer.isReadOnly) "View Only" else "Can Edit", fontSize = 10.sp, color = if (peer.isReadOnly) Color(0xFFFF9D42) else Color(0xFF00E676))
                                    }
                                }
                                Row(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Color(0xFF0E0F12)).border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp)).padding(2.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (!peer.isReadOnly) Color(0xFF00E676).copy(alpha = 0.25f) else Color.Transparent).clickable { performHaptic(); onSetPeerPermission(peer.endpointId, false) }.padding(horizontal = 8.dp, vertical = 4.dp), contentAlignment = Alignment.Center) {
                                        Text("Edit", fontSize = 11.sp, fontWeight = if (!peer.isReadOnly) FontWeight.Bold else FontWeight.Normal, color = if (!peer.isReadOnly) Color(0xFF00E676) else Color(0xFF8E95A5))
                                    }
                                    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (peer.isReadOnly) Color(0xFFFF9D42).copy(alpha = 0.25f) else Color.Transparent).clickable { performHaptic(); onSetPeerPermission(peer.endpointId, true) }.padding(horizontal = 8.dp, vertical = 4.dp), contentAlignment = Alignment.Center) {
                                        Text("View", fontSize = 11.sp, fontWeight = if (peer.isReadOnly) FontWeight.Bold else FontWeight.Normal, color = if (peer.isReadOnly) Color(0xFFFF9D42) else Color(0xFF8E95A5))
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                GlossyIconButton(icon = "person_remove", onClick = { performHaptic(); onDisconnectPeer(peer.endpointId) }, size = 30.dp, iconSize = 14.dp, shape = RoundedCornerShape(8.dp), unselectedGradient = listOf(Color(0xFF2A1010), Color(0xFF1A0A0A)))
                            }
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                        GlossyButton(onClick = { performHaptic(HapticFeedbackType.LongPress); onLeaveRoom(); showBoardDetailsCollabSheet = false }, gradient = dynamicErrorGlossy(), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                            MaterialSymbol(name = "logout", tint = Color.White, size = 18.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Leave Room", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
}
}

@Composable
fun StrokesThumbnailPreview(
    strokes: List<Stroke>,
    backgroundColor: Long = 0xFF18181C,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(backgroundColor))
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.20f),
                        Color.White.copy(alpha = 0.05f),
                        Color.Black.copy(alpha = 0.40f)
                    )
                ),
                RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (strokes.isEmpty()) {
            MaterialSymbol(
                name = "brush",
                tint = Color.White.copy(alpha = 0.20f),
                size = 24.dp
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                var minX = Float.MAX_VALUE
                var minY = Float.MAX_VALUE
                var maxX = Float.MIN_VALUE
                var maxY = Float.MIN_VALUE

                strokes.forEach { stroke ->
                    stroke.points.forEach { pt ->
                        if (pt.x < minX) minX = pt.x
                        if (pt.y < minY) minY = pt.y
                        if (pt.x > maxX) maxX = pt.x
                        if (pt.y > maxY) maxY = pt.y
                    }
                }

                val strokeWidthX = (maxX - minX).coerceAtLeast(1f)
                val strokeWidthY = (maxY - minY).coerceAtLeast(1f)
                val scale = minOf(size.width / strokeWidthX, size.height / strokeWidthY).coerceIn(0.01f, 1.0f)

                strokes.forEach { stroke ->
                    if (stroke.points.size > 1) {
                        val path = Path()
                        val firstPt = stroke.points.first()
                        val startX = (firstPt.x - minX) * scale + (size.width - strokeWidthX * scale) / 2f
                        val startY = (firstPt.y - minY) * scale + (size.height - strokeWidthY * scale) / 2f
                        path.moveTo(startX, startY)
                        for (i in 1 until stroke.points.size) {
                            val pt = stroke.points[i]
                            val px = (pt.x - minX) * scale + (size.width - strokeWidthX * scale) / 2f
                            val py = (pt.y - minY) * scale + (size.height - strokeWidthY * scale) / 2f
                            path.lineTo(px, py)
                        }
                        drawPath(
                            path = path,
                            color = Color(stroke.color),
                            style = DrawScopeStroke(
                                width = (stroke.strokeWidth * scale).coerceAtLeast(1.5f),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardThumbnailPreview(
    board: Board,
    modifier: Modifier = Modifier
) {
    val firstPage = board.pages.getOrNull(board.activePageIndex) ?: board.pages.firstOrNull()
    val strokes = firstPage?.strokes ?: emptyList()
    StrokesThumbnailPreview(strokes = strokes, backgroundColor = board.backgroundColor, modifier = modifier)
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun GlossyBoardsTabPage(
    uiState: WhiteboardUiState,
    showNewBoardDialog: Boolean,
    newBoardTitle: String,
    onNewBoardTitleChange: (String) -> Unit,
    onToggleNewBoardDialog: (Boolean) -> Unit,
    onCreateNewBoard: (String) -> Unit,
    onSwitchBoard: (Board) -> Unit,
    onRenameBoard: (String, String) -> Unit,
    onDuplicateBoard: (String) -> Unit,
    onDeleteBoard: (String) -> Unit,
    onToggleReadOnlyBoard: (String, Boolean) -> Unit = { _, _ -> },
    onPerformHaptic: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    var selectedBoardForMenu by remember { mutableStateOf<Board?>(null) }
    var boardToRename by remember { mutableStateOf<Board?>(null) }
    var renameBoardTitle by remember { mutableStateOf("") }
    val menuSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val renameSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AnimatedVisibility(
                visible = showNewBoardDialog,
                enter = fadeIn(tween(PHYSICS_FAST_MS)) + slideInVertically(tween(PHYSICS_FAST_MS)) { -it / 2 } + scaleIn(tween(PHYSICS_FAST_MS)),
                exit = fadeOut(tween(PHYSICS_FAST_MS)) + slideOutVertically(tween(PHYSICS_FAST_MS)) { -it / 2 } + scaleOut(tween(PHYSICS_FAST_MS))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlossyTextField(
                        value = newBoardTitle,
                        onValueChange = onNewBoardTitleChange,
                        placeholder = "Board name...",
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    GlossyButton(
                        onClick = {
                            if (newBoardTitle.isNotBlank()) {
                                onPerformHaptic()
                                onCreateNewBoard(newBoardTitle)
                                onNewBoardTitleChange("")
                                onToggleNewBoardDialog(false)
                            }
                        },
                        gradient = dynamicPrimaryGlossy(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Create", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    GlossyIconButton(
                        icon = "close",
                        onClick = {
                            onPerformHaptic()
                            onToggleNewBoardDialog(false)
                        },
                        size = 38.dp,
                        iconSize = 18.dp,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.savedBoards, key = { it.id }) { board ->
                    val isCurrent = board.id == uiState.currentBoard.id

                    val cardGradients = if (isCurrent) {
                        dynamicPrimaryGlossy()
                    } else {
                        listOf(
                            Color(0xFF282B33),
                            Color(0xFF1B1D23),
                            Color(0xFF121316)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(if (isCurrent) 6.dp else 3.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(brush = Brush.verticalGradient(cardGradients))
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.35f),
                                        Color.White.copy(alpha = 0.08f),
                                        Color.Black.copy(alpha = 0.60f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .drawBehind {
                                // 3D Top specular glass highlight
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            Color.White.copy(alpha = if (isCurrent) 0.30f else 0.16f),
                                            Color.Transparent
                                        ),
                                        startY = 0f,
                                        endY = size.height * 0.45f
                                    ),
                                    size = Size(size.width, size.height * 0.45f)
                                )
                            }
                            .pointerInput(board) {
                                detectTapGestures(
                                    onTap = {
                                        onPerformHaptic()
                                        onSwitchBoard(board)
                                    },
                                    onLongPress = {
                                        onPerformHaptic()
                                        selectedBoardForMenu = board
                                    }
                                )
                            }
                            .padding(10.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Thumbnail container
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(95.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0E0F12))
                                    .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(10.dp))
                            ) {
                                BoardThumbnailPreview(
                                    board = board,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Title & Details outside
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (board.isReadOnly) {
                                        MaterialSymbol(name = "lock", tint = Color(0xFFFF9D42), size = 13.dp)
                                    }
                                    Text(
                                        text = board.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                }

                                // Date on left + Total Pages Outside badge on right
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dateFormat.format(Date(board.modifiedAt)),
                                        fontSize = 11.sp,
                                        color = if (isCurrent) Color.White.copy(alpha = 0.85f) else Color(0xFFB0B4C0),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    // Total Pages Outside Badge
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isCurrent) Color.Black.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f))
                                            .padding(horizontal = 5.dp, vertical = 1.5.dp)
                                    ) {
                                        MaterialSymbol(
                                            name = "layers",
                                            tint = Color.White,
                                            size = 11.dp
                                        )
                                        Text(
                                            text = "${board.pages.size} ${if (board.pages.size == 1) "page" else "pages"}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        GlossyButton(
            onClick = {
                onPerformHaptic()
                onToggleNewBoardDialog(true)
            },
            gradient = dynamicPrimaryGlossy(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp, end = 4.dp)
        ) {
            MaterialSymbol(name = "add", tint = Color.White, size = 20.dp)
            Spacer(modifier = Modifier.width(6.dp))
            Text("New Board", fontWeight = FontWeight.Bold, color = Color.White)
        }

        if (selectedBoardForMenu != null) {
            val board = selectedBoardForMenu!!
            GlossyBottomSheet(
                onDismissRequest = { selectedBoardForMenu = null },
                sheetState = menuSheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF14161B))
                            .border(
                                1.dp,
                                Brush.verticalGradient(
                                    listOf(Color.White.copy(alpha = 0.20f), Color.Black.copy(alpha = 0.50f))
                                ),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BoardThumbnailPreview(
                                board = board,
                                modifier = Modifier.size(56.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = board.title,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Owner: ${board.ownerName} • Created by ${board.createdBy}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF00E676)
                                )
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Created", fontSize = 12.sp, color = Color(0xFF8E95A5))
                                Text(dateFormat.format(Date(board.createdAt)), fontSize = 12.sp, color = Color.White)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Last Modified", fontSize = 12.sp, color = Color(0xFF8E95A5))
                                Text(dateFormat.format(Date(board.modifiedAt)), fontSize = 12.sp, color = Color.White)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Content", fontSize = 12.sp, color = Color(0xFF8E95A5))
                                val totalStrokes = board.pages.sumOf { it.strokes.size }
                                val totalTexts = board.pages.sumOf { it.textElements.size }
                                Text("${board.pages.size} pages • $totalStrokes strokes • $totalTexts text", fontSize = 12.sp, color = Color.White)
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MaterialSymbol(
                                    name = if (board.isReadOnly) "lock" else "visibility",
                                    tint = if (board.isReadOnly) Color(0xFFFF9D42) else Color(0xFF00E676),
                                    size = 20.dp
                                )
                                Column {
                                    Text("View Only Access", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(
                                        if (board.isReadOnly) "Board is locked in read-only mode" else "Editing is enabled",
                                        fontSize = 11.sp,
                                        color = Color(0xFF8E95A5)
                                    )
                                }
                            }
                            GlossySwitch(
                                checked = board.isReadOnly,
                                onCheckedChange = { isChecked ->
                                    onPerformHaptic()
                                    onToggleReadOnlyBoard(board.id, isChecked)
                                    selectedBoardForMenu = board.copy(isReadOnly = isChecked)
                                }
                            )
                        }
                    }

                    // Compact 2-Row Action Buttons Grid
                    // Row 1: Open Board + Rename Board
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlossyButton(
                            onClick = {
                                onPerformHaptic()
                                onSwitchBoard(board)
                                selectedBoardForMenu = null
                            },
                            gradient = dynamicPrimaryGlossy(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            MaterialSymbol(name = "brush", tint = Color.White, size = 16.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        GlossyButton(
                            onClick = {
                                onPerformHaptic()
                                boardToRename = board
                                renameBoardTitle = board.title
                                selectedBoardForMenu = null
                            },
                            gradient = dynamicSurfaceGlass(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            MaterialSymbol(name = "edit", tint = Color.White, size = 16.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Rename", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // Row 2: Duplicate Board + Delete Board
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlossyButton(
                            onClick = {
                                onPerformHaptic()
                                onDuplicateBoard(board.id)
                                selectedBoardForMenu = null
                            },
                            gradient = dynamicSurfaceGlass(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            MaterialSymbol(name = "content_copy", tint = Color.White, size = 16.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Duplicate", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        GlossyButton(
                            onClick = {
                                onPerformHaptic()
                                onDeleteBoard(board.id)
                                selectedBoardForMenu = null
                            },
                            gradient = dynamicErrorGlossy(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            MaterialSymbol(name = "delete", tint = Color.White, size = 16.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if (boardToRename != null) {
            val targetBoard = boardToRename!!
            GlossyBottomSheet(
                onDismissRequest = { boardToRename = null },
                sheetState = renameSheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Rename Board",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    GlossyTextField(
                        value = renameBoardTitle,
                        onValueChange = { renameBoardTitle = it },
                        placeholder = "Enter board name...",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlossyButton(
                            onClick = { boardToRename = null },
                            gradient = dynamicSurfaceGlass(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        GlossyButton(
                            onClick = {
                                onPerformHaptic()
                                if (renameBoardTitle.isNotBlank()) {
                                    onRenameBoard(targetBoard.id, renameBoardTitle.trim())
                                }
                                boardToRename = null
                            },
                            gradient = dynamicPrimaryGlossy(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun GlossyConnectTabPage(
    uiState: WhiteboardUiState,
    onUpdateDisplayName: (String) -> Unit,
    onStartAdvertising: (String) -> Unit,
    onStopAdvertising: () -> Unit,
    onStartDiscovery: () -> Unit,
    onStopDiscovery: () -> Unit,
    onConnectPeer: (String, String) -> Unit,
    onCopyRoomCode: (String) -> Unit,
    onSwitchBoard: (Board) -> Unit,
    onSetPeerPermission: (String, Boolean) -> Unit,
    onDisconnectPeer: (String) -> Unit,
    onLeaveRoom: () -> Unit,
    onPerformHaptic: () -> Unit
) {
    var editDisplayName by remember(uiState.displayName) { mutableStateOf(uiState.displayName) }
    var showSelectBoardSheet by remember { mutableStateOf(false) }
    val selectBoardSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 68.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Profile Display Name (Inline "You are <name>" with underline and variable font)
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = "You are ",
                        fontSize = 20.sp,
                        fontFamily = robotoFlexAppFontFamily,
                        color = Color(0xFFE2E4EA)
                    )
                    BasicTextField(
                        value = editDisplayName,
                        onValueChange = {
                            editDisplayName = it
                            onUpdateDisplayName(it)
                        },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 20.sp,
                            fontFamily = robotoFlexAppFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        cursorBrush = SolidColor(Color.White),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .drawBehind {
                                        drawLine(
                                            brush = Brush.horizontalGradient(
                                                listOf(
                                                    Color.White.copy(alpha = 0.70f),
                                                    Color.White.copy(alpha = 0.20f)
                                                )
                                            ),
                                            start = Offset(0f, size.height + 4f),
                                            end = Offset(size.width, size.height + 4f),
                                            strokeWidth = 2f
                                        )
                                    }
                                    .padding(bottom = 2.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (editDisplayName.isEmpty()) {
                                    Text(
                                        text = "your name...",
                                        fontSize = 20.sp,
                                        fontFamily = robotoFlexAppFontFamily,
                                        color = Color(0xFF7E8494)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }

            // 2. Active Shared Board Card (Shows which board is being shared & lets host switch)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF14161B))
                        .border(
                            1.dp,
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.20f), Color.Black.copy(alpha = 0.50f))
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BoardThumbnailPreview(
                            board = uiState.currentBoard,
                            modifier = Modifier.size(46.dp)
                        )
                        Column {
                            Text("Active Shared Board", fontSize = 11.sp, color = Color(0xFF8E95A5))
                            Text(
                                text = uiState.currentBoard.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    GlossyButton(
                        onClick = {
                            onPerformHaptic()
                            showSelectBoardSheet = true
                        },
                        gradient = dynamicTertiaryGlossy(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        MaterialSymbol(name = "swap_horiz", tint = Color.White, size = 16.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Switch Board", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // 3. Active Room Code Banner (Clean, Minimal, Modern)
            if (uiState.roomCode != null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF14161B))
                            .border(1.dp, Color(0xFF00E676).copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MaterialSymbol(name = "sensors", tint = Color(0xFF00E676), size = 26.dp)
                            Column {
                                Text("Active Room Code", fontSize = 11.sp, color = Color(0xFFB0B4C0))
                                Text(
                                    text = "#${uiState.roomCode}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E676)
                                )
                            }
                        }
                        GlossyButton(
                            onClick = { onCopyRoomCode(uiState.roomCode) },
                            gradient = dynamicTertiaryGlossy(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            MaterialSymbol(name = "content_copy", tint = Color.White, size = 16.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // 4. Connected Peers Section (With Per-Peer Edit / View Access Controls)
            if (uiState.connectedPeers.isNotEmpty()) {
                item {
                    Text(
                        text = "Connected Peers (${uiState.connectedPeers.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                items(uiState.connectedPeers, key = { "connected_${it.endpointId}" }) { peer ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Name + status (no card border)
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MaterialSymbol(name = "devices", tint = if (peer.isReadOnly) Color(0xFFFF9D42) else Color(0xFF00E676), size = 20.dp)
                            Column {
                                Text(peer.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White, fontFamily = robotoFlexAppFontFamily)
                                Text(
                                    text = if (peer.isReadOnly) "View Only" else "Can Edit",
                                    fontSize = 11.sp,
                                    color = if (peer.isReadOnly) Color(0xFFFF9D42) else Color(0xFF00E676)
                                )
                            }
                        }

                        // Permission toggle (Edit | View)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0E0F12))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (!peer.isReadOnly) Color(0xFF00E676).copy(alpha = 0.25f) else Color.Transparent)
                                    .border(if (!peer.isReadOnly) 1.dp else 0.dp, if (!peer.isReadOnly) Color(0xFF00E676).copy(alpha = 0.60f) else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { onPerformHaptic(); onSetPeerPermission(peer.endpointId, false) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    MaterialSymbol(name = "edit", tint = if (!peer.isReadOnly) Color(0xFF00E676) else Color(0xFF8E95A5), size = 12.dp)
                                    Text("Edit", fontSize = 11.sp, fontWeight = if (!peer.isReadOnly) FontWeight.Bold else FontWeight.Normal, color = if (!peer.isReadOnly) Color(0xFF00E676) else Color(0xFF8E95A5))
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (peer.isReadOnly) Color(0xFFFF9D42).copy(alpha = 0.25f) else Color.Transparent)
                                    .border(if (peer.isReadOnly) 1.dp else 0.dp, if (peer.isReadOnly) Color(0xFFFF9D42).copy(alpha = 0.60f) else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { onPerformHaptic(); onSetPeerPermission(peer.endpointId, true) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    MaterialSymbol(name = "visibility", tint = if (peer.isReadOnly) Color(0xFFFF9D42) else Color(0xFF8E95A5), size = 12.dp)
                                    Text("View", fontSize = 11.sp, fontWeight = if (peer.isReadOnly) FontWeight.Bold else FontWeight.Normal, color = if (peer.isReadOnly) Color(0xFFFF9D42) else Color(0xFF8E95A5))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Disconnect button (icon only, button-styled)
                        GlossyIconButton(
                            icon = "person_remove",
                            onClick = { onPerformHaptic(); onDisconnectPeer(peer.endpointId) },
                            size = 32.dp,
                            iconSize = 15.dp,
                            shape = RoundedCornerShape(9.dp),
                            unselectedGradient = listOf(Color(0xFF2A1010), Color(0xFF1A0A0A))
                        )
                    }
                }
            }

            // 5. Discovered Nearby Rooms
            if (uiState.discoveredPeers.isNotEmpty()) {
                item {
                    Text(
                        text = "Nearby Rooms Found",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                items(uiState.discoveredPeers, key = { "discovered_${it.endpointId}" }) { peer ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(peer.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = robotoFlexAppFontFamily)
                        GlossyButton(
                            onClick = { onPerformHaptic(); onConnectPeer(peer.endpointId, editDisplayName) },
                            gradient = dynamicPrimaryGlossy(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Connect", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // 6. Host / Join / Leave Room at Bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlossyButton(
                    onClick = {
                        onPerformHaptic()
                        if (uiState.isAdvertising) onStopAdvertising() else onStartAdvertising(editDisplayName)
                    },
                    gradient = if (uiState.isAdvertising) dynamicErrorGlossy() else dynamicPrimaryGlossy(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    MaterialSymbol(name = if (uiState.isAdvertising) "close" else "sensors", tint = Color.White, size = 18.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (uiState.isAdvertising) "Stop Hosting" else "Host Room", fontWeight = FontWeight.Bold, color = Color.White)
                }

                GlossyButton(
                    onClick = {
                        onPerformHaptic()
                        if (uiState.isDiscovering) onStopDiscovery() else onStartDiscovery()
                    },
                    gradient = if (uiState.isDiscovering) dynamicErrorGlossy() else dynamicTertiaryGlossy(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    MaterialSymbol(name = if (uiState.isDiscovering) "close" else "devices", tint = Color.White, size = 18.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (uiState.isDiscovering) "Stop Search" else "Join Room", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            if (uiState.connectedPeers.isNotEmpty()) {
                GlossyButton(
                    onClick = { onPerformHaptic(); onLeaveRoom() },
                    gradient = dynamicErrorGlossy(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MaterialSymbol(name = "logout", tint = Color.White, size = 18.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Leave Room", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // 7. Select Board to Share Bottom Sheet
        if (showSelectBoardSheet) {
            GlossyBottomSheet(
                onDismissRequest = { showSelectBoardSheet = false },
                sheetState = selectBoardSheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column {
                        Text(
                            text = "Select Board to Share",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Connected peers will immediately sync and open this board",
                            fontSize = 12.sp,
                            color = Color(0xFF8E95A5)
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.savedBoards, key = { "select_share_${it.id}" }) { board ->
                            val isCurrent = board.id == uiState.currentBoard.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isCurrent) dynamicPrimaryGlossy().first().copy(alpha = 0.25f) else Color(0xFF14161B))
                                    .border(
                                        1.dp,
                                        if (isCurrent) dynamicPrimaryGlossy().first().copy(alpha = 0.80f) else Color.White.copy(alpha = 0.10f),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        onPerformHaptic()
                                        onSwitchBoard(board)
                                        showSelectBoardSheet = false
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    BoardThumbnailPreview(
                                        board = board,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Column {
                                        Text(
                                            text = board.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${board.pages.size} pages • ${if (board.isReadOnly) "View Only" else "Editable"}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF8E95A5)
                                        )
                                    }
                                }

                                if (isCurrent) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF00E676).copy(alpha = 0.20f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Active",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00E676)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun GlossySettingsTabPage(
    uiState: WhiteboardUiState,
    onBackgroundColorSelected: (Color) -> Unit,
    onWidthChanged: (Float) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onSelectThemePreset: (AppThemePreset) -> Unit,
    onPerformHaptic: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. App Theme & Studio Colors Selector
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "App Theme & Studio Colors",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Start
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppThemePreset.values().toList().chunked(2).forEach { rowPresets ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowPresets.forEach { preset ->
                                val isSelected = uiState.selectedThemePreset == preset
                                val presetColor = Color(preset.primaryColor)
                                GlossyButton(
                                    onClick = {
                                        onPerformHaptic()
                                        onSelectThemePreset(preset)
                                    },
                                    gradient = if (isSelected) dynamicPrimaryGlossy() else dynamicSurfaceGlass(),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(presetColor)
                                            .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = preset.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            }
                            if (rowPresets.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        item {
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
        }

        // 2. Canvas Background Color
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Canvas Background",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Start
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BACKGROUND_COLORS.forEach { bgColor ->
                        val isSelected = uiState.backgroundColor == bgColor
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .shadow(if (isSelected) 4.dp else 2.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    brush = Brush.verticalGradient(
                                        if (isSelected) listOf(Color.White, Color.White.copy(alpha = 0.6f))
                                        else listOf(Color.White.copy(alpha = 0.35f), Color.Black.copy(alpha = 0.45f))
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    onPerformHaptic()
                                    onBackgroundColorSelected(bgColor)
                                }
                        )
                    }
                }
            }
        }

        item {
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
        }

        // 3. Haptics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Haptic Feedback",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = "Tactile vibration on buttons & tools",
                        fontSize = 12.sp,
                        color = Color(0xFFB0B4C0),
                        textAlign = TextAlign.Start
                    )
                }
                GlossySwitch(
                    checked = uiState.hapticsEnabled,
                    onCheckedChange = {
                        onPerformHaptic()
                        onToggleHaptics(it)
                    }
                )
            }
        }
    }
}
