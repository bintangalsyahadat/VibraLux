package com.vibralux

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class DeviceDetailActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var spinnerMode: Spinner
    private lateinit var switchManual: Switch
    private lateinit var tvDeviceId: TextView
    private lateinit var tvLampStatus: TextView
    private lateinit var tvScheduleInline: TextView
    private lateinit var tvScheduleLabel: TextView

    private lateinit var deviceId: String

    private var hourFrom = 0
    private var minuteFrom = 0
    private var hourTo = 0
    private var minuteTo = 0

    private val modeOptions = listOf("auto", "manual", "schedule")

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
        tvScheduleInline = findViewById(R.id.tvScheduleInline)
        tvScheduleLabel = findViewById(R.id.tvScheduleLabel)

        tvDeviceId.text = "Device ID: $deviceId"

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
        }

        // Jadwal inline klik
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

        // Load status awal
        dbRef.child("vibralux").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lampStatus = snapshot.child("lamp_status").getValue(Boolean::class.java) ?: false
                tvLampStatus.text = if (lampStatus) "Lampu: NYALA" else "Lampu: MATI"

                val mode = snapshot.child("controls/mode").getValue(String::class.java)
                val index = modeOptions.indexOf(mode ?: "auto")
                if (index != -1) spinnerMode.setSelection(index)

                val from = snapshot.child("controls/schedule/from")
                hourFrom = from.child("hour").getValue(Int::class.java) ?: 0
                minuteFrom = from.child("minute").getValue(Int::class.java) ?: 0

                val to = snapshot.child("controls/schedule/to")
                hourTo = to.child("hour").getValue(Int::class.java) ?: 0
                minuteTo = to.child("minute").getValue(Int::class.java) ?: 0

                tvScheduleInline.text = String.format("%02d:%02d - %02d:%02d", hourFrom, minuteFrom, hourTo, minuteTo)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateModeUI(mode: String) {
        when (mode) {
            "auto" -> {
                switchManual.visibility = View.GONE
                tvScheduleInline.visibility = View.GONE
                tvScheduleLabel.visibility = View.GONE
            }
            "manual" -> {
                switchManual.visibility = View.VISIBLE
                tvScheduleInline.visibility = View.GONE
                tvScheduleLabel.visibility = View.GONE
            }
            "schedule" -> {
                switchManual.visibility = View.GONE
                tvScheduleInline.visibility = View.VISIBLE
                tvScheduleLabel.visibility = View.VISIBLE
            }
        }
    }
}
