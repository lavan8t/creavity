package com.kd.creavity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.kd.creavity.data.nearby.ConnectivityCheckResult
import com.kd.creavity.data.nearby.ConnectivityHelper
import com.kd.creavity.presentation.ui.AdaptiveWhiteboardScreen
import com.kd.creavity.presentation.ui.OotbAction
import com.kd.creavity.presentation.viewmodel.WhiteboardViewModel
import com.kd.creavity.presentation.viewmodel.dispatch
import com.kd.creavity.ui.theme.OutofTheBoxTheme

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestRequiredPermissions()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val isDark = when (uiState.themeMode) {
                com.kd.creavity.presentation.viewmodel.ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
                com.kd.creavity.presentation.viewmodel.ThemeMode.LIGHT -> false
                com.kd.creavity.presentation.viewmodel.ThemeMode.DARK -> true
            }

            OutofTheBoxTheme(
                darkTheme = isDark,
                dynamicColor = true
            ) {
                AdaptiveWhiteboardScreen(
                    uiState = uiState,
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