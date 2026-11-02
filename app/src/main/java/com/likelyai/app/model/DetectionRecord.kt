package com.likelyai.app.model

data class DetectionRecord(
    val label: String,
    val type: String,
    val confidence: Float,
    val message: String
)
