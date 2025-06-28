package com.vibralux

import Device
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class DeviceAdapter(private val context: Context, private val list: List<Device>) :
    BaseAdapter() {

    override fun getCount() = list.size
    override fun getItem(position: Int) = list[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.item_device, parent, false)

        val device = list[position]
        val tvName = view.findViewById<TextView>(R.id.tvDeviceName)
        val tvStatus = view.findViewById<TextView>(R.id.tvDeviceStatus)
        val statusDot = view.findViewById<View>(R.id.statusDot)

        tvName.text = "VibraLux - ${device.id}"
        tvStatus.text = device.status

        if (device.status == "connected") {
            tvStatus.setTextColor(context.getColor(android.R.color.black))
            statusDot.setBackgroundResource(R.drawable.green_circle)
        } else {
            tvStatus.setTextColor(context.getColor(android.R.color.black))
            statusDot.setBackgroundResource(R.drawable.red_circle)
        }

        view.setOnClickListener {
            if (device.status == "connected") {
                val intent = Intent(context, DeviceMenuActivity::class.java)
                intent.putExtra("deviceId", device.id)
                context.startActivity(intent)
            } else {
                // Tampilkan alert jika tidak terkoneksi
                android.app.AlertDialog.Builder(context)
                    .setTitle("Device Disconnected")
                    .setMessage("This device is currently offline.\nYou can't control it until it's reconnected.")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

        return view
    }


}
