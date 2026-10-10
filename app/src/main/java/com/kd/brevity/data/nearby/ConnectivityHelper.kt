package com.kd.brevity.data.nearby

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

sealed class ConnectivityCheckResult {
    data object Ready : ConnectivityCheckResult()
    data class MissingPermissions(val permissions: List<String>) : ConnectivityCheckResult()
    data object BluetoothDisabled : ConnectivityCheckResult()
    data object WifiDisabled : ConnectivityCheckResult()
    data object LocationDisabled : ConnectivityCheckResult()
}

object ConnectivityHelper {

    fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissions.add(Manifest.permission.BLUETOOTH)
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        return permissions
    }

    fun checkMissingPermissions(context: Context): List<String> {
        val required = getRequiredPermissions()
        return required.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
    }

    fun isBluetoothEnabled(context: Context): Boolean {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = bluetoothManager?.adapter ?: return false
        return adapter.isEnabled
    }

    fun isWifiEnabled(context: Context): Boolean {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        return wifiManager?.isWifiEnabled ?: false
    }

    fun isLocationEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return true
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return locationManager?.isLocationEnabled ?: false
    }

    fun checkReadiness(context: Context): ConnectivityCheckResult {
        val missing = checkMissingPermissions(context)
        if (missing.isNotEmpty()) {
            return ConnectivityCheckResult.MissingPermissions(missing)
        }

        if (!isBluetoothEnabled(context)) {
            return ConnectivityCheckResult.BluetoothDisabled
        }

        if (!isWifiEnabled(context)) {
            return ConnectivityCheckResult.WifiDisabled
        }

        if (!isLocationEnabled(context)) {
            return ConnectivityCheckResult.LocationDisabled
        }

        return ConnectivityCheckResult.Ready
    }

    fun createBluetoothEnableIntent(): Intent {
        return Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE)
    }

    fun createWifiSettingsIntent(): Intent {
        return Intent(Settings.ACTION_WIFI_SETTINGS)
    }

    fun createLocationSettingsIntent(): Intent {
        return Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    }
}
