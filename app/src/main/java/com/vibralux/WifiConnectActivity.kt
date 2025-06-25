package com.vibralux

import android.Manifest
import android.content.*
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class WifiConnectActivity : AppCompatActivity() {

    private lateinit var ssid: String
    private lateinit var password: String

    private val wifiManager by lazy {
        applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
    }

    private val wifiPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            connectToESP()
        } else {
            Toast.makeText(this, "Izin diperlukan untuk menyambung ke WiFi", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ssid = intent.getStringExtra("esp_ssid") ?: ""
        password = intent.getStringExtra("esp_password") ?: ""

        if (ssid.isEmpty()) {
            Toast.makeText(this, "Data SSID tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        checkPermissionsAndConnect()
    }

    private fun checkPermissionsAndConnect() {
        val neededPermissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            neededPermissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        } else {
            neededPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        wifiPermissionLauncher.launch(neededPermissions.toTypedArray())
    }

    private fun connectToESP() {
        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(this, "WiFi dinonaktifkan, mengaktifkan...", Toast.LENGTH_SHORT).show()
            wifiManager.isWifiEnabled = true
        }

        val wifiConfig = WifiConfiguration().apply {
            SSID = "\"$ssid\""
            preSharedKey = "\"$password\""
        }

        val netId = wifiManager.addNetwork(wifiConfig)
        if (netId == -1) {
            Toast.makeText(this, "Gagal menambahkan konfigurasi WiFi", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        wifiManager.disconnect()
        wifiManager.enableNetwork(netId, true)
        wifiManager.reconnect()

        Toast.makeText(this, "Menyambung ke $ssid...", Toast.LENGTH_SHORT).show()

        // Lanjut ke WifiSetupActivity setelah delay pendek
        android.os.Handler().postDelayed({
            val intent = Intent(this, WifiSetupActivity::class.java).apply {
                putExtra("esp_ssid", ssid)
            }
            startActivity(intent)
            finish()
        }, 5000) // delay 5 detik cukup aman untuk koneksi awal
    }
}
