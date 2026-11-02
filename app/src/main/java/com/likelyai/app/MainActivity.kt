package com.likelyai.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.likelyai.app.model.DetectionRecord

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LikelyAIApp()
        }
    }

    @Composable
    private fun LikelyAIApp() {
        val store = remember { DetectionStore() }
        var enabled by remember { mutableStateOf(true) }
        var sensitivity by remember { mutableFloatStateOf(0.75f) }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "LikelyAI",
                    style = MaterialTheme.typography.headlineMedium
                )

                Switch(
                    checked = enabled,
                    onCheckedChange = {
                        enabled = it
                        if (it) {
                            startService(Intent(this@MainActivity, DetectionService::class.java))
                        } else {
                            stopService(Intent(this@MainActivity, DetectionService::class.java))
                        }
                    }
                )

                Text("Sensitivity: ${(sensitivity * 100).toInt()}%")
                Slider(
                    value = sensitivity,
                    onValueChange = { sensitivity = it },
                    valueRange = 0.1f..1f
                )

                Button(
                    onClick = {
                        startService(Intent(this@MainActivity, DetectionService::class.java))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start monitoring")
                }

                Button(
                    onClick = {
                        stopService(Intent(this@MainActivity, DetectionService::class.java))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Stop monitoring")
                }

                Text(
                    text = "Recent detections",
                    style = MaterialTheme.typography.titleMedium
                )

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(store.history.takeLast(10)) { item ->
                        DetectionEntry(item)
                    }
                }
            }
        }
    }

    @Composable
    private fun DetectionEntry(record: DetectionRecord) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(text = record.label)
            Text(text = "Confidence: ${(record.confidence * 100).toInt()}%")
            Text(text = "Type: ${record.type}")
        }
    }
}
