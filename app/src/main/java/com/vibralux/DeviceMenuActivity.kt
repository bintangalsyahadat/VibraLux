package com.vibralux

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class DeviceMenuActivity : AppCompatActivity() {

    private lateinit var btnControls: Button
    private lateinit var btnConfig: Button
    private var deviceId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_menu)

        deviceId = intent.getStringExtra("deviceId")

        btnControls = findViewById(R.id.btnControls)
        btnConfig = findViewById(R.id.btnConfig)

        btnControls.setOnClickListener {
            val intent = Intent(this, DeviceDetailActivity::class.java)
            intent.putExtra("deviceId", deviceId)
            startActivity(intent)
        }

        btnConfig.setOnClickListener {
            val intent = Intent(this, DeviceConfigActivity::class.java)
            intent.putExtra("deviceId", deviceId)
            startActivity(intent)
        }
    }
}
