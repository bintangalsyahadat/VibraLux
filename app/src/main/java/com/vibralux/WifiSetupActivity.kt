package com.vibralux

import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class WifiSetupActivity : AppCompatActivity() {
    private lateinit var etSsid: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSend: Button
    private var loadingDialog: AlertDialog? = null

    private val client = OkHttpClient()
    private val espSetupUrl = "http://192.168.4.1/setup"

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_setup)

        etSsid = findViewById(R.id.etWifiSsid)
        etPassword = findViewById(R.id.etWifiPassword)
        btnSend = findViewById(R.id.btnSend)

        btnSend.setOnClickListener {
            val ssid = etSsid.text.toString()
            val pass = etPassword.text.toString()

            if (ssid.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Isi semua data WiFi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendWifiCredentialsToESP(ssid, pass)
            showLoadingDialog("Mengirim data WiFi...")
            sendWifiCredentialsToESP(ssid, pass)
        }
    }

    private fun sendWifiCredentialsToESP(ssid: String, password: String) {
        val json = JSONObject().apply {
            put("ssid", ssid)
            put("password", password)
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(espSetupUrl).post(body).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    hideLoadingDialog()
                    showErrorDialog("Gagal terhubung ke Device.")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val resStr = response.body?.string() ?: ""
                val jsonRes = try { JSONObject(resStr) } catch (_: Exception) { null }
                val status = jsonRes?.optString("status")

                runOnUiThread {
                    hideLoadingDialog()
                    if (response.isSuccessful && status == "success") {
                        val deviceId = jsonRes.optString("device_id", "UNKNOWN")
                        disconnectFromESP {
                            goToDetail(deviceId)
                        }
                    } else {
                        showErrorDialog("Device gagal terhubung ke WiFi.")
                    }
                }
            }
        })
    }

    private fun disconnectFromESP(onDisconnected: () -> Unit) {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.bindProcessToNetwork(null)
        }
        Toast.makeText(this, "Terputus dari jaringan ESP", Toast.LENGTH_SHORT).show()
        Handler(mainLooper).postDelayed({ onDisconnected() }, 1500)
    }

    private fun goToDetail(deviceId: String) {
        Toast.makeText(this, "Berhasil konek, Device ID: $deviceId", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, DeviceDetailActivity::class.java).apply {
            putExtra("deviceId", deviceId)
        }
        startActivity(intent)
        finishAffinity()
    }

    private fun showErrorDialog(msg: String) {
        AlertDialog.Builder(this)
            .setTitle("Gagal")
            .setMessage(msg)
            .setPositiveButton("OK") { _, _ ->
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .setCancelable(false)
            .show()
    }
}
