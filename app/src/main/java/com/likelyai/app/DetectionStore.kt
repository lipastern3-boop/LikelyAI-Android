package com.likelyai.app

import com.likelyai.app.model.DetectionRecord
import java.util.concurrent.CopyOnWriteArrayList

class DetectionStore {
    val history = CopyOnWriteArrayList<DetectionRecord>()

    fun add(record: DetectionRecord) {
        history.add(record)
    }
}
