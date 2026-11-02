package com.likelyai.app

class AIConfidenceEngine {

    fun scoreText(text: String): Float {
        val normalized = text.lowercase()
        val signals = mutableListOf<Float>()

        if (normalized.contains("generated")) signals.add(0.25f)
        if (normalized.contains("synthetic")) signals.add(0.30f)
        if (normalized.contains("ai")) signals.add(0.20f)

        val repetitivePhrases = listOf(
            "in today\'s world",
            "it is important to note",
            "this is a game-changer"
        )

        val repetitionScore = repetitivePhrases.count { normalized.contains(it) } * 0.12f
        signals.add(repetitionScore)

        val average = if (signals.isNotEmpty()) signals.average().toFloat() else 0f
        return average.coerceIn(0f, 1f)
    }

    fun scoreImage(): Float = 0.82f
    fun scoreVideo(): Float = 0.76f
    fun scoreVoice(): Float = 0.88f
}
