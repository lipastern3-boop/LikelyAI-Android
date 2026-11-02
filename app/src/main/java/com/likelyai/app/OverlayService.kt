package com.likelyai.app

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: TextView

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WindowManager::class.java)

        overlayView = TextView(this).apply {
            text = "Likely AI"
            setTextColor(ContextCompat.getColor(this@OverlayService, android.R.color.holo_red_dark))
            setBackgroundColor(ContextCompat.getColor(this@OverlayService, android.R.color.holo_red_light))
            setPadding(24, 16, 24, 16)
            textSize = 18f
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 180
        }

        windowManager.addView(overlayView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::windowManager.isInitialized && ::overlayView.isInitialized) {
            windowManager.removeViewImmediate(overlayView)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
