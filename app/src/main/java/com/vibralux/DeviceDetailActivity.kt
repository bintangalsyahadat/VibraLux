package com.vibralux

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.*

class DeviceDetailActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var spinnerMode: Spinner
    private lateinit var switchManual: SwitchCompat
    private lateinit var tvDeviceId: TextView
    private lateinit var tvLampStatus: TextView
    private lateinit var tvSSID: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvScheduleInline: TextView
    private lateinit var tvScheduleLabel: TextView
    private lateinit var layoutManual: LinearLayout
    private lateinit var lastDivider: View

    private lateinit var deviceId: String

    private var hourFrom = 0
    private var minuteFrom = 0
    private var hourTo = 0
    private var minuteTo = 0

    private val modeOptions = listOf("auto", "manual", "schedule")

    private lateinit var vibraluxListener: ValueEventListener
    private lateinit var statusListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_detail)

        deviceId = intent.getStringExtra("deviceId") ?: return
        dbRef = FirebaseDatabase.getInstance().getReference("devices").child(deviceId)

        // View Binding
        spinnerMode = findViewById(R.id.spinnerMode)
        switchManual = findViewById(R.id.switchManual)
        tvDeviceId = findViewById(R.id.tvDeviceId)
        tvLampStatus = findViewById(R.id.tvLampStatus)
        tvSSID = findViewById(R.id.tvSSID)
        tvStatus = findViewById(R.id.tvStatus)
        tvScheduleInline = findViewById(R.id.tvScheduleInline)
        tvScheduleLabel = findViewById(R.id.tvScheduleLabel)
        layoutManual = findViewById(R.id.layoutManual)
        lastDivider = findViewById(R.id.lastDivider)

        tvDeviceId.text = "VibraLux-$deviceId"

        // Spinner setup
        spinnerMode.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, modeOptions)
        spinnerMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val mode = modeOptions[position]
                updateModeUI(mode)
                dbRef.child("vibralux/controls/mode").setValue(mode)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Manual switch
        switchManual.setOnCheckedChangeListener { _, isChecked ->
            dbRef.child("vibralux/controls/manual_status").setValue(isChecked)
            updateSwitchStyle(isChecked)
        }

        // Schedule time picker
        tvScheduleInline.setOnClickListener {
            TimePickerDialog(this, { _, h1, m1 ->
                hourFrom = h1
                minuteFrom = m1

                TimePickerDialog(this, { _, h2, m2 ->
                    hourTo = h2
                    minuteTo = m2

                    tvScheduleInline.text = String.format("%02d:%02d - %02d:%02d", hourFrom, minuteFrom, hourTo, minuteTo)

                    dbRef.child("vibralux/controls/schedule/from").setValue(mapOf("hour" to hourFrom, "minute" to minuteFrom))
                    dbRef.child("vibralux/controls/schedule/to").setValue(mapOf("hour" to hourTo, "minute" to minuteTo))

                }, hourTo, minuteTo, true).show()

            }, hourFrom, minuteFrom, true).show()
        }

        // Realtime listener: Data kontrol dan lampu
        vibraluxListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lampStatus = snapshot.child("lamp_status").getValue(Boolean::class.java) ?: false
                tvLampStatus.text = if (lampStatus) "Light Status : On" else "Light Status : Off"

                val mode = snapshot.child("controls/mode").getValue(String::class.java) ?: "auto"
                val index = modeOptions.indexOf(mode)
                if (index != -1) spinnerMode.setSelection(index)

                val manualStatus = snapshot.child("controls/manual_status").getValue(Boolean::class.java) ?: false
                switchManual.isChecked = manualStatus
                updateSwitchStyle(manualStatus)

                val from = snapshot.child("controls/schedule/from")
                hourFrom = from.child("hour").getValue(Int::class.java) ?: 0
                minuteFrom = from.child("minute").getValue(Int::class.java) ?: 0

                val to = snapshot.child("controls/schedule/to")
                hourTo = to.child("hour").getValue(Int::class.java) ?: 0
                minuteTo = to.child("minute").getValue(Int::class.java) ?: 0

                tvScheduleInline.text = String.format("%02d:%02d - %02d:%02d", hourFrom, minuteFrom, hourTo, minuteTo)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        // Realtime listener: Status koneksi & SSID
        statusListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.child("status").getValue(String::class.java) ?: "unknown"
                tvStatus.text = if (status == "connected") "● connected" else "● disconnected"
                tvStatus.setTextColor(
                    if (status == "connected") getColor(android.R.color.holo_green_light)
                    else getColor(android.R.color.holo_red_light)
                )

                if (status == "connected") {
                    val ssid = snapshot.child("ssid").getValue(String::class.java) ?: "-"
                    tvSSID.text = "SSID : $ssid"
                    tvSSID.visibility = View.VISIBLE
                } else {
                    tvSSID.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        dbRef.child("vibralux").addValueEventListener(vibraluxListener)
        dbRef.addValueEventListener(statusListener)
    }

    private fun updateModeUI(mode: String) {
        when (mode) {
            "auto" -> {
                layoutManual.visibility = View.GONE
                tvScheduleInline.visibility = View.GONE
                tvScheduleLabel.visibility = View.GONE
                lastDivider.visibility = View.GONE
            }
            "manual" -> {
                layoutManual.visibility = View.VISIBLE
                tvScheduleInline.visibility = View.GONE
                tvScheduleLabel.visibility = View.GONE
                lastDivider.visibility = View.VISIBLE
            }
            "schedule" -> {
                layoutManual.visibility = View.GONE
                tvScheduleInline.visibility = View.VISIBLE
                tvScheduleLabel.visibility = View.VISIBLE
                lastDivider.visibility = View.VISIBLE
            }
        }
    }

    private fun updateSwitchStyle(isChecked: Boolean) {
        val white = ContextCompat.getColorStateList(this, R.color.white)
        val primary = ContextCompat.getColorStateList(this, R.color.primary)
        val gray = ContextCompat.getColorStateList(this, R.color.gray_light)

        switchManual.thumbTintList = white
        switchManual.trackTintList = if (isChecked) primary else gray
    }

    override fun onDestroy() {
        super.onDestroy()
        dbRef.child("vibralux").removeEventListener(vibraluxListener)
        dbRef.removeEventListener(statusListener)
    }
}
