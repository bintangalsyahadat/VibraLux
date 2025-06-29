package com.vibralux

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class QRCodeScanActivity : AppCompatActivity() {
    private val cameraPermission = Manifest.permission.CAMERA

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startQrScanner()
        else {
            Toast.makeText(this, "Izin kamera dibutuhkan", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkCameraPermissionAndStart()
    }

    private fun checkCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, cameraPermission) == PackageManager.PERMISSION_GRANTED) {
            startQrScanner()
        } else {
            requestPermissionLauncher.launch(cameraPermission)
        }
    }

    private fun startQrScanner() {
        val integrator = IntentIntegrator(this).apply {
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            setPrompt("Arahkan kamera ke QR Code ESP")
            setCameraId(0)
            setBeepEnabled(true)
            setBarcodeImageEnabled(false)
            captureActivity = MyCaptureActivity::class.java
        }
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result.contents != null) {
            handleQrResult(result.contents)
        } else {
            Toast.makeText(this, "QR Code dibatalkan", Toast.LENGTH_SHORT).show()
            finish()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleQrResult(qrText: String) {
        if (!qrText.startsWith("WIFI:")) {
            Toast.makeText(this, "Format QR tidak valid", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        val cleaned = qrText.removePrefix("WIFI:").removeSuffix(";")
        val parts = cleaned.split(";")

        var ssid = ""
        var password = ""

        for (part in parts) {
            when {
                part.startsWith("S:") -> ssid = part.removePrefix("S:")
                part.startsWith("P:") -> password = part.removePrefix("P:")
            }
        }

        if (ssid.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "QR Code tidak mengandung SSID atau Password", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        val intent = Intent(this, WifiConnectActivity::class.java).apply {
            putExtra("esp_ssid", ssid)
            putExtra("esp_password", password)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }
}