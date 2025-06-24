package com.vibralux

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.google.firebase.database.*


class QuakeMonitorService : Service() {

    private var hasStartedForeground = false
    private val notificationId = 102
    private val dbRef = FirebaseDatabase.getInstance().getReference("devices")
    private val quakeListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            var anyQuake = false
            Log.d("QuakeService", "QUAKE DETECTED! Menampilkan notifikasi.")

            for (deviceSnap in snapshot.children) {
                Log.d("QuakeService", "Snapshot: ${deviceSnap.toString()}")
                val quakeStatus = deviceSnap.child("vibralux/quake_status").getValue(Boolean::class.java)
                Log.d("quakeStatus", "quakeStatus: $quakeStatus")
                if (quakeStatus == true) {
                    anyQuake = true
                    break
                }
            }

            if (anyQuake) {
                showNotification()
            } else {
                hideNotification()
            }
        }

        override fun onCancelled(error: DatabaseError) {}
    }

    override fun onCreate() {
        super.onCreate()
        dbRef.addValueEventListener(quakeListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        dbRef.removeEventListener(quakeListener)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!hasStartedForeground) {
            startForeground(1, createEmptyNotification())
            hasStartedForeground = true
        }
        return START_STICKY
    }

    private fun createEmptyNotification(): Notification {
        val channelId = "quake_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(channelId, "Gempa", NotificationManager.IMPORTANCE_HIGH)
            getSystemService(NotificationManager::class.java).createNotificationChannel(chan)
        }

        return Notification.Builder(this, channelId)
            .setContentTitle("Deteksi Gempa Aktif")
            .setContentText("Aplikasi memantau gempa secara realtime")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    private fun showNotification() {
        val channelId = "quake_channel"
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Peringatan Gempa", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        val notif = Notification.Builder(this, channelId)
            .setContentTitle("🚨 Peringatan Gempa!")
            .setContentText("Sensor mendeteksi getaran gempa bumi.\nSegera lakukan evakuasi!")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(Notification.PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)

        manager.notify(notificationId, notif.build())
    }

    private fun hideNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.cancel(notificationId)
    }
}
