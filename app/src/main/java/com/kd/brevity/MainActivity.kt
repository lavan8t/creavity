package com.kd.brevity

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
import com.kd.brevity.data.nearby.ConnectivityCheckResult
import com.kd.brevity.data.nearby.ConnectivityHelper
import com.kd.brevity.presentation.ui.AdaptiveWhiteboardScreen
import com.kd.brevity.presentation.ui.OotbAction
import com.kd.brevity.presentation.viewmodel.WhiteboardViewModel
import com.kd.brevity.presentation.viewmodel.dispatch
import com.kd.brevity.ui.theme.OutofTheBoxTheme

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

            OutofTheBoxTheme {
                AdaptiveWhiteboardScreen(
                    uiState = uiState,
                    windowWidthSizeClass = windowSizeClass.widthSizeClass,
                    onAction = { action ->
                        when (action) {
                            is OotbAction.StartAdvertising -> startAdvertisingWithCheck(action.deviceName)
                            is OotbAction.StartDiscovery -> startDiscoveryWithCheck()
                            else -> viewModel.dispatch(action)
                        }
                    }
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