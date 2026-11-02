package com.likelyai.app

import com.likelyai.app.model.DetectionRecord
import java.util.concurrent.CopyOnWriteArrayList

object DetectionStore {
    private val _history = CopyOnWriteArrayList<DetectionRecord>()
    val history: List<DetectionRecord>
        get() = _history.toList()

    fun add(record: DetectionRecord) {
        _history.add(0, record)
        if (_history.size > 25) {
            _history.removeAt(_history.lastIndex)
        }
    }

    fun clear() {
        _history.clear()
    }
}
