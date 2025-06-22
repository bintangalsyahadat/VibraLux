package com.vibralux

import Device
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class DeviceListActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private val devices = mutableListOf<Device>()
    private lateinit var adapter: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)

        listView = findViewById(R.id.listViewDevices)
        adapter = DeviceAdapter(this, devices)
        listView.adapter = adapter

        FirebaseDatabase.getInstance().getReference("devices")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    devices.clear()
                    for (child in snapshot.children) {
                        val device = child.getValue(Device::class.java)
                        if (device != null) devices.add(device)
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DeviceListActivity, "Gagal ambil data", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
