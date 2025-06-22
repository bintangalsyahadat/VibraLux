package com.vibralux

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class DeviceDetailActivity : AppCompatActivity() {

    private lateinit var databaseRef: DatabaseReference
    private lateinit var deviceId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_detail)

        val tvDetail = findViewById<TextView>(R.id.tvDetail)
        deviceId = intent.getStringExtra("deviceId") ?: return

        databaseRef = FirebaseDatabase.getInstance().getReference("devices").child(deviceId)

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val device = snapshot.getValue(Device::class.java)
                tvDetail.text = device?.let {
                    "ID: ${it.id}\nSSID: ${it.ssid}\nStatus: ${it.status}\nLampu: ${it.vibralux.lamp_status}"
                } ?: "Device not found"
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
