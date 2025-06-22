package com.vibralux

import Device
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var deviceListView: ListView
    private val deviceList = mutableListOf<Device>()
    private lateinit var adapter: DeviceAdapter
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                // Log error
            }
        })
    }
}
