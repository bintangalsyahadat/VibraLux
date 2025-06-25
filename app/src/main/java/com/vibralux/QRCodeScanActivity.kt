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
        if (isGranted) {
            startQrScanner()
        } else {
            Toast.makeText(this, "Izin kamera dibutuhkan", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkCameraPermissionAndStart()
    }

    private fun checkCameraPermissionAndStart() {
        when {
            ContextCompat.checkSelfPermission(this, cameraPermission) == PackageManager.PERMISSION_GRANTED -> {
                startQrScanner()
            }
            shouldShowRequestPermissionRationale(cameraPermission) -> {
                Toast.makeText(this, "Aplikasi memerlukan akses kamera untuk scan QR", Toast.LENGTH_SHORT).show()
                requestPermissionLauncher.launch(cameraPermission)
            }
            else -> {
                requestPermissionLauncher.launch(cameraPermission)
            }
        }
    }

    private fun startQrScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Arahkan kamera ke QR Code ESP")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(true)
        integrator.setBarcodeImageEnabled(false)
        integrator.captureActivity = MyCaptureActivity::class.java
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
        val parts = qrText.split(";")
        if (parts.size >= 2) {
            val espSsid = parts[0]
            val espPassword = parts[1]

            val intent = Intent(this, WifiConnectActivity::class.java).apply {
                putExtra("esp_ssid", espSsid)
                putExtra("esp_password", espPassword)
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Format QR tidak valid", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}
