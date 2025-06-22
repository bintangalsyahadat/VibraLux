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
        val view = convertView ?: inflater.inflate(android.R.layout.simple_list_item_2, parent, false)

        val device = list[position]
        val tv1 = view.findViewById<TextView>(android.R.id.text1)
        val tv2 = view.findViewById<TextView>(android.R.id.text2)

        tv1.text = "ID: ${device.id}"
        tv2.text = "Status: ${device.status}"

        view.setOnClickListener {
            val intent = Intent(context, DeviceDetailActivity::class.java)
            intent.putExtra("deviceId", device.id)
            context.startActivity(intent)
        }

        return view
    }
}
