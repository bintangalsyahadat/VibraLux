package com.vibralux

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.widget.SwitchCompat
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.*

class DeviceConfigActivity : AppCompatActivity() {

    private lateinit var deviceId: String
    private lateinit var tvDeviceId: TextView
    private lateinit var switchAlarm: SwitchCompat
    private lateinit var btnRemove: Button
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_config)

        deviceId = intent.getStringExtra("deviceId") ?: return
        tvDeviceId = findViewById(R.id.tvDeviceId)
        switchAlarm = findViewById(R.id.switchAlarm)
        btnRemove = findViewById(R.id.btnRemove)

        tvDeviceId.text = "VibraLux-$deviceId"

        // Firebase reference to active_quake_alarm
        dbRef = FirebaseDatabase.getInstance()
            .getReference("devices/$deviceId/vibralux/active_quake_alarm")

        // Baca nilai awal dari Firebase
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isActive = snapshot.getValue(Boolean::class.java) ?: true
                switchAlarm.isChecked = isActive
                updateSwitchStyle(isActive)
            }

            override fun onCancelled(error: DatabaseError) {
                // Optional: log error
            }
        })

        // Kirim ke Firebase saat toggle berubah
        switchAlarm.setOnCheckedChangeListener { _, isChecked ->
            dbRef.setValue(isChecked)
            updateSwitchStyle(isChecked)
        }

        btnRemove.setOnClickListener {
            // TODO: remove device dari Firebase
        }
    }

    private fun updateSwitchStyle(isChecked: Boolean) {
        val white = ContextCompat.getColorStateList(this, R.color.white)
        val primary = ContextCompat.getColorStateList(this, R.color.primary)
        val gray = ContextCompat.getColorStateList(this, R.color.gray_light)

        switchAlarm.thumbTintList = white
        switchAlarm.trackTintList = if (isChecked) primary else gray
    }
}
