package com.kd.creavity.data.nearby

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalOnlyHotspotBridgeManager(context: Context) {
    private val appContext = context.applicationContext
    private val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private var reservation: WifiManager.LocalOnlyHotspotReservation? = null

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    private val _ssid = MutableStateFlow<String?>(null)
    val ssid: StateFlow<String?> = _ssid.asStateFlow()

    private val _passphrase = MutableStateFlow<String?>(null)
    val passphrase: StateFlow<String?> = _passphrase.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun startBridgeHotspot(
        onSuccess: (ssid: String, pass: String) -> Unit = { _, _ -> },
        onError: (reason: String) -> Unit = {}
    ) {
        val wm = wifiManager
        if (wm == null) {
            val err = "WifiManager is unavailable on this device"
            _errorMessage.value = err
            onError(err)
            return
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            val err = "Local-only hotspot requires Android 8.0 (API 26) or higher"
            _errorMessage.value = err
            onError(err)
            return
        }

        try {
            wm.startLocalOnlyHotspot(object : WifiManager.LocalOnlyHotspotCallback() {
                override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation?) {
                    super.onStarted(reservation)
                    this@LocalOnlyHotspotBridgeManager.reservation = reservation
                    _isActive.value = true

                    val config = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        reservation?.softApConfiguration
                    } else {
                        @Suppress("DEPRECATION")
                        reservation?.wifiConfiguration
                    }

                    val hotspotSsid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        reservation?.softApConfiguration?.ssid ?: "CreaVITy_Classroom_AP"
                    } else {
                        @Suppress("DEPRECATION")
                        reservation?.wifiConfiguration?.SSID ?: "CreaVITy_Classroom_AP"
                    }

                    val hotspotPassphrase = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        reservation?.softApConfiguration?.passphrase ?: ""
                    } else {
                        @Suppress("DEPRECATION")
                        reservation?.wifiConfiguration?.preSharedKey ?: ""
                    }

                    _ssid.value = hotspotSsid
                    _passphrase.value = hotspotPassphrase
                    _errorMessage.value = null
                    onSuccess(hotspotSsid, hotspotPassphrase)
                }

                override fun onStopped() {
                    super.onStopped()
                    _isActive.value = false
                    _ssid.value = null
                    _passphrase.value = null
                    reservation = null
                }

                override fun onFailed(reason: Int) {
                    super.onFailed(reason)
                    val reasonStr = when (reason) {
                        ERROR_NO_CHANNEL -> "No Wi-Fi channel available"
                        ERROR_GENERIC -> "Generic Wi-Fi failure"
                        ERROR_INCOMPATIBLE_MODE -> "Incompatible Wi-Fi mode"
                        ERROR_TETHERING_DISALLOWED -> "Tethering disallowed by device policy"
                        else -> "Failed with error code $reason"
                    }
                    _isActive.value = false
                    _errorMessage.value = reasonStr
                    onError(reasonStr)
                }
            }, Handler(Looper.getMainLooper()))
        } catch (e: SecurityException) {
            val err = "Permission error starting hotspot: ${e.message}"
            _errorMessage.value = err
            onError(err)
        } catch (e: Exception) {
            val err = "Error starting hotspot: ${e.message}"
            _errorMessage.value = err
            onError(err)
        }
    }

    fun stopBridgeHotspot() {
        try {
            reservation?.close()
        } catch (_: Exception) {}
        reservation = null
        _isActive.value = false
        _ssid.value = null
        _passphrase.value = null
    }
}
