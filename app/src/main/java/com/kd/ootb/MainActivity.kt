package com.kd.ootb

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.kd.ootb.data.nearby.ConnectivityCheckResult
import com.kd.ootb.data.nearby.ConnectivityHelper
import com.kd.ootb.presentation.ui.AdaptiveWhiteboardScreen
import com.kd.ootb.presentation.viewmodel.WhiteboardViewModel
import com.kd.ootb.ui.theme.OutofTheBoxTheme

class MainActivity : ComponentActivity() {
    private val viewModel: WhiteboardViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (!allGranted) {
            Toast.makeText(this, "Permissions are required for offline P2P connection", Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestRequiredPermissions()

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val uiState by viewModel.uiState.collectAsState()

            OutofTheBoxTheme(themePreset = uiState.selectedThemePreset) {
                AdaptiveWhiteboardScreen(
                    uiState = uiState,
                    windowWidthSizeClass = windowSizeClass.widthSizeClass,
                    onStrokeCompleted = viewModel::onStrokeCompleted,
                    onStrokeBroadcastStart = viewModel::broadcastStrokeStart,
                    onStrokeBroadcastPoint = viewModel::broadcastStrokePoint,
                    onStrokeBroadcastEnd = viewModel::broadcastStrokeEnd,
                    onTransform = viewModel::onTransform,
                    onResetTransform = viewModel::resetCanvasTransform,
                    onSelectTool = viewModel::selectTool,
                    onAddTextElement = viewModel::addTextElement,
                    onDeleteStroke = viewModel::deleteStrokeById,
                    onDeleteText = viewModel::deleteTextElementById,
                    onElementsMoved = viewModel::updateMovedElements,
                    onDuplicateElements = viewModel::duplicateElements,
                    onRecolorElements = viewModel::recolorElements,
                    onColorSelected = viewModel::setPenColor,
                    onBackgroundColorSelected = viewModel::setCanvasBackgroundColor,
                    onWidthChanged = viewModel::setStrokeWidth,
                    onClearCanvas = viewModel::clearCanvas,
                    onAddNewPage = viewModel::addNewPage,
                    onSwitchPage = viewModel::switchPage,
                    onDeletePage = viewModel::deletePage,
                    onCreateNewBoard = viewModel::createNewBoard,
                    onSwitchBoard = viewModel::switchBoard,
                    onRenameBoard = viewModel::renameBoard,
                    onDuplicateBoard = viewModel::duplicateBoard,
                    onDeleteBoard = viewModel::deleteBoard,
                    onUpdateDisplayName = viewModel::updateDisplayName,
                    onToggleHaptics = viewModel::setHapticsEnabled,
                    onSelectThemePreset = viewModel::setAppThemePreset,
                    onStartAdvertising = ::startAdvertisingWithCheck,
                    onStopAdvertising = viewModel::stopAdvertising,
                    onStartDiscovery = ::startDiscoveryWithCheck,
                    onStopDiscovery = viewModel::stopDiscovery,
                    onConnectPeer = viewModel::connectToPeer,
                    onUndo = viewModel::undo,
                    onRedo = viewModel::redo,
                    onCreateVersionSnapshot = viewModel::createVersionSnapshot,
                    onRestoreVersionSnapshot = viewModel::restoreVersionSnapshot,
                    onToggleReadOnlyBoard = viewModel::toggleReadOnlyBoard,
                    onSetCanvasTransform = viewModel::setCanvasTransform,
                    onSetPeerPermission = viewModel::setPeerPermission,
                    onAddImageElement = viewModel::addImageElement,
                    onDeleteImage = viewModel::deleteImageElementById,
                    onDisconnectPeer = viewModel::disconnectPeer,
                    onLeaveRoom = viewModel::leaveRoom
                )
            }
        }
    }

    private fun startAdvertisingWithCheck(deviceName: String) {
        when (val result = ConnectivityHelper.checkReadiness(this)) {
            is ConnectivityCheckResult.Ready -> {
                viewModel.startAdvertising(deviceName)
            }
            is ConnectivityCheckResult.MissingPermissions -> {
                permissionLauncher.launch(result.permissions.toTypedArray())
            }
            is ConnectivityCheckResult.BluetoothDisabled -> {
                Toast.makeText(this, "Please enable Bluetooth for offline whiteboard", Toast.LENGTH_SHORT).show()
                try {
                    startActivity(ConnectivityHelper.createBluetoothEnableIntent())
                } catch (_: Exception) {}
            }
            is ConnectivityCheckResult.WifiDisabled -> {
                Toast.makeText(this, "Please enable Wi-Fi for direct P2P transfer", Toast.LENGTH_SHORT).show()
                try {
                    startActivity(ConnectivityHelper.createWifiSettingsIntent())
                } catch (_: Exception) {}
            }
            is ConnectivityCheckResult.LocationDisabled -> {
                Toast.makeText(this, "Please enable Location for discovery", Toast.LENGTH_SHORT).show()
                try {
                    startActivity(ConnectivityHelper.createLocationSettingsIntent())
                } catch (_: Exception) {}
            }
        }
    }

    private fun startDiscoveryWithCheck() {
        when (val result = ConnectivityHelper.checkReadiness(this)) {
            is ConnectivityCheckResult.Ready -> {
                viewModel.startDiscovery()
            }
            is ConnectivityCheckResult.MissingPermissions -> {
                permissionLauncher.launch(result.permissions.toTypedArray())
            }
            is ConnectivityCheckResult.BluetoothDisabled -> {
                Toast.makeText(this, "Please enable Bluetooth for offline whiteboard", Toast.LENGTH_SHORT).show()
                try {
                    startActivity(ConnectivityHelper.createBluetoothEnableIntent())
                } catch (_: Exception) {}
            }
            is ConnectivityCheckResult.WifiDisabled -> {
                Toast.makeText(this, "Please enable Wi-Fi for direct P2P transfer", Toast.LENGTH_SHORT).show()
                try {
                    startActivity(ConnectivityHelper.createWifiSettingsIntent())
                } catch (_: Exception) {}
            }
            is ConnectivityCheckResult.LocationDisabled -> {
                Toast.makeText(this, "Please enable Location for discovery", Toast.LENGTH_SHORT).show()
                try {
                    startActivity(ConnectivityHelper.createLocationSettingsIntent())
                } catch (_: Exception) {}
            }
        }
    }

    private fun requestRequiredPermissions() {
        val missing = ConnectivityHelper.checkMissingPermissions(this)
        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }
}