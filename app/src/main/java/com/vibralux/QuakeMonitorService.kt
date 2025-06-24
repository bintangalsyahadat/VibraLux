package com.vibralux

import android.app.*
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.database.*
import android.media.Ringtone


class QuakeMonitorService : Service() {

    private var hasStartedForeground = false
    private val notificationId = 102
    private var alarmRingtone: Ringtone? = null
    private val handler = android.os.Handler()
    private val loopInterval = 50L

    private val dbRef = FirebaseDatabase.getInstance().getReference("devices")
    private val quakeListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            var anyQuake = false
            for (deviceSnap in snapshot.children) {
                val quakeStatus = deviceSnap.child("vibralux/quake_status").getValue(Boolean::class.java)
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
        val channelId = "quake_monitor_channel"
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
        val manager = getSystemService(NotificationManager::class.java)
        val channelId = "quake_alert_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Peringatan Gempa",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }

        val notif = NotificationCompat.Builder(this, channelId)
            .setContentTitle("🚨 Peringatan Gempa!")
            .setContentText("Sensor mendeteksi getaran gempa bumi. Segera evakuasi!")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        manager.notify(notificationId, notif.build())

        // 🔊 Mainkan alarm menggunakan Ringtone
        if (alarmRingtone == null) {
            val uri = Uri.parse("android.resource://${packageName}/raw/alarm")
            alarmRingtone = RingtoneManager.getRingtone(applicationContext, uri)
            alarmRingtone?.apply {
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                play()
            }
            alarmRingtone?.play()
            handler.postDelayed(loopRunnable, loopInterval)
        }
    }


    private fun hideNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.cancel(notificationId)

        handler.removeCallbacks(loopRunnable)
        alarmRingtone?.stop()
        alarmRingtone = null
    }

    private val loopRunnable = object : Runnable {
        override fun run() {
            alarmRingtone?.let {
                if (!it.isPlaying) {
                    it.play()
                }
            }
            handler.postDelayed(this, loopInterval)
        }
    }
}
