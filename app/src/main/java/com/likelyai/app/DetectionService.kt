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
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DetectionService : Service() {

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private val engine = AIConfidenceEngine()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildForegroundNotification()
        startForeground(1001, notification)

        scope.launch {
            while (isActive) {
                val score = engine.scoreText(
                    "This image looks polished, highly synthetic, and likely AI-generated."
                )

                if (score >= 0.65f) {
                    val record = DetectionRecord(
                        label = "Likely AI",
                        type = "text",
                        confidence = score,
                        message = "AI-generated content likely detected."
                    )

                    DetectionStore.add(record)
                    triggerOverlay()
                    showAlertNotification(record)
                }

                delay(15000L)
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        stopService(Intent(this, OverlayService::class.java))
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun triggerOverlay() {
        val overlayIntent = Intent(this, OverlayService::class.java)
        startService(overlayIntent)
    }

    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, "likelyai_channel")
            .setContentTitle("LikelyAI")
            .setContentText("Monitoring for likely AI-generated content")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
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

    private fun showAlertNotification(record: DetectionRecord) {
        val manager = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, "likelyai_channel")
            .setContentTitle("Likely AI detected")
            .setContentText("${record.label} · ${(record.confidence * 100).toInt()}% confidence")
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
