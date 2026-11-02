package com.likelyai.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.likelyai.app.model.DetectionRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DetectionService : Service() {

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private val engine = AIConfidenceEngine()

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        startForeground(1001, notification)

        scope.launch {
            while (true) {
                val textSample = "This generative image looks highly polished and synthetic."
                val score = engine.scoreText(textSample)

                if (score > 0.7f) {
                    val record = DetectionRecord(
                        label = "Likely AI",
                        type = "text",
                        confidence = score,
                        message = "AI-generated text likely detected."
                    )
                    DetectionStore().add(record)
                    showNotification(record)
                    startService(Intent(this@DetectionService, OverlayService::class.java))
                }

                delay(15000L)
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, "likelyai_channel")
            .setContentTitle("LikelyAI")
            .setContentText("Monitoring for likely AI-generated content")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "likelyai_channel",
                "LikelyAI Monitoring",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(record: DetectionRecord) {
        val manager = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, "likelyai_channel")
            .setContentTitle("Likely AI detected")
            .setContentText("${record.label} (${(record.confidence * 100).toInt()}%)")
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
