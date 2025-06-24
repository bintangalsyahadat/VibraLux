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

        tvName.text = "ID: ${device.id}"
        tvStatus.text = "Status: ${device.status}"
        tvStatus.setTextColor(if (device.status == "connected") context.getColor(android.R.color.holo_green_dark) else context.getColor(android.R.color.holo_red_dark))

        view.setOnClickListener {
            val intent = Intent(context, DeviceDetailActivity::class.java)
            intent.putExtra("deviceId", device.id)
            context.startActivity(intent)
        }

        return view
    }

}
