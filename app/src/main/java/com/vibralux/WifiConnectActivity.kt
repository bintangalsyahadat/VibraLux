package com.vibralux

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class WifiConnectActivity : AppCompatActivity() {
    private lateinit var ssid: String
    private lateinit var password: String
    private lateinit var connectivityManager: ConnectivityManager
    private var loadingDialog: AlertDialog? = null

    private fun showLoadingDialog(message: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_loading, null)
        dialogView.findViewById<TextView>(R.id.loadingText).text = message

        loadingDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        loadingDialog?.show()
    }

    private fun hideLoadingDialog() {
        loadingDialog?.dismiss()
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) connectToESP()
        else {
            Toast.makeText(this, "Izin WiFi dibutuhkan", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLoadingDialog("Menghubungkan ke Device...")
        ssid = intent.getStringExtra("esp_ssid") ?: ""
        password = intent.getStringExtra("esp_password") ?: ""

        if (ssid.isEmpty()) {
            Toast.makeText(this, "SSID tidak valid", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        permissionLauncher.launch(Manifest.permission.NEARBY_WIFI_DEVICES)
    }

    private fun connectToESP() {
        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .build()

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(specifier)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                connectivityManager.bindProcessToNetwork(network)
                runOnUiThread {
                    hideLoadingDialog()
                    Toast.makeText(this@WifiConnectActivity, "Berhasil terhubung ke $ssid", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@WifiConnectActivity, WifiSetupActivity::class.java).apply {
                        putExtra("esp_ssid", ssid)
                    }
                    startActivity(intent)
                    finish()
                }
            }

            override fun onUnavailable() {
                super.onUnavailable()
                runOnUiThread {
                    Toast.makeText(this@WifiConnectActivity, "Gagal terhubung ke $ssid", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        connectivityManager.requestNetwork(request, callback)
    }
}
