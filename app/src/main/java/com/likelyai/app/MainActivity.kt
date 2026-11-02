package com.likelyai.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.likelyai.app.model.DetectionRecord

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestRuntimePermissions()
        setContent {
            LikelyAIApp()
        }
    }

    private fun requestRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    @Composable
    private fun LikelyAIApp() {
        var enabled by remember { mutableStateOf(true) }
        var sensitivity by remember { mutableFloatStateOf(0.75f) }
        val detections = remember { DetectionStore.history }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "LikelyAI",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text("Personal AI detector")

                Switch(
                    checked = enabled,
                    onCheckedChange = {
                        enabled = it
                        if (it) startMonitoring() else stopMonitoring()
                    }
                )

                Text("Sensitivity: ${(sensitivity * 100).toInt()}%")
                Slider(
                    value = sensitivity,
                    onValueChange = { sensitivity = it },
                    valueRange = 0.1f..1f,
                    steps = 9
                )

                Button(
                    onClick = { startMonitoring() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start monitoring")
                }

                Button(
                    onClick = { stopMonitoring() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Stop monitoring")
                }

                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                        intent.data = Uri.parse("package:$packageName")
                        startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Allow overlay")
                }

                Text(
                    text = "Recent detections",
                    style = MaterialTheme.typography.titleMedium
                )

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(detections.takeLast(10)) { item ->
                        DetectionEntry(item)
                    }
                }
            }
        }
    }

    private fun startMonitoring() {
        startService(Intent(this, DetectionService::class.java))
    }

    private fun stopMonitoring() {
        stopService(Intent(this, DetectionService::class.java))
        stopService(Intent(this, OverlayService::class.java))
    }

    @Composable
    private fun DetectionEntry(record: DetectionRecord) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(text = record.label)
            Text(text = "Type: ${record.type}")
            Text(text = "Confidence: ${(record.confidence * 100).toInt()}%")
        }
    }
}
