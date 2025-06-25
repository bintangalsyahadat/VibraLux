package com.vibralux

import Device
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {

    private lateinit var deviceListView: ListView
    private val deviceList = mutableListOf<Device>()
    private lateinit var adapter: DeviceAdapter
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        val intent = Intent(this, QuakeMonitorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        deviceListView = findViewById(R.id.deviceListView)
        adapter = DeviceAdapter(this, deviceList)
        deviceListView.adapter = adapter

        database = FirebaseDatabase.getInstance().getReference("devices")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                deviceList.clear()
                for (child in snapshot.children) {
                    val device = child.getValue(Device::class.java)
                    if (device != null) deviceList.add(device)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Gagal ambil data", Toast.LENGTH_SHORT).show()
            }
        })

        val btnAddDevice = findViewById<Button>(R.id.btnAddDevices)
        btnAddDevice.setOnClickListener {
            val intent = Intent(this, QRCodeScanActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        showStopAlarmDialogIfQuakeDetected()
    }

    private fun showStopAlarmDialogIfQuakeDetected() {
        val dbRef = FirebaseDatabase.getInstance().getReference("devices")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var anyQuake = false
                for (deviceSnap in snapshot.children) {
                    val status = deviceSnap.child("vibralux/quake_status").getValue(Boolean::class.java)
                    if (status == true) {
                        anyQuake = true
                        break
                    }
                }

                if (anyQuake) {
                    showStopAlarmDialog()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showStopAlarmDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Peringatan Gempa")
        builder.setMessage("Gempa terdeteksi. Hentikan alarm?")
        builder.setCancelable(false)
        builder.setPositiveButton("Stop Alarm") { _, _ ->
            stopAllAlarms()
        }
        builder.setNegativeButton("Batal", null)
        builder.show()
    }

    private fun stopAllAlarms() {
        val dbRef = FirebaseDatabase.getInstance().getReference("devices")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (deviceSnap in snapshot.children) {
                    val deviceId = deviceSnap.key ?: continue
                    val deviceRef = dbRef.child(deviceId).child("vibralux")

                    // Set quake_status = false dan force_stop = true
                    deviceRef.child("quake_status").setValue(false)
                    deviceRef.child("force_stop").setValue(true)

                    // Handler untuk set force_stop = false setelah 5 menit
                    Handler(Looper.getMainLooper()).postDelayed({
                        deviceRef.child("force_stop").setValue(false)
                    }, 5 * 60 * 1000)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
