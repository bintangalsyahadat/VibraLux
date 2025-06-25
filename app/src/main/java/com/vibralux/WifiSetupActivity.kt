package com.vibralux

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.IOException
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody


class WifiSetupActivity : AppCompatActivity() {

    private lateinit var etSsid: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSend: Button

    private val client = OkHttpClient()
    private val espSetupUrl = "http://192.168.4.1/setup"

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
        }
    }

    private fun sendWifiCredentialsToESP(ssid: String, password: String) {
        val json = JSONObject()
        json.put("ssid", ssid)
        json.put("password", password)

        val mediaType = "application/json".toMediaType()
        val body = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(espSetupUrl)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    showErrorDialog("Gagal terhubung ke ESP.")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val resStr = response.body?.string() ?: ""

                runOnUiThread {
                    if (response.isSuccessful && resStr.contains("connected")) {
                        val deviceId = extractDeviceId(resStr)
                        goToDetail(deviceId)
                    } else {
                        showErrorDialog("ESP gagal terhubung ke WiFi rumah.")
                    }
                }
            }

        })
    }

    private fun extractDeviceId(response: String): String {
        // Asumsikan format respon JSON dari ESP seperti:
        // {"connected":true, "device_id":"A43329B3A3A0"}
        return try {
            val json = JSONObject(response)
            json.getString("device_id")
        } catch (e: Exception) {
            "UNKNOWN"
        }
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
