package com.plcoding.echojournal.echos.presentation.util

import kotlin.math.roundToInt

object AmplitudeNormalizer {
    const val MAX_OUTPUT = 1f
    const val MIN_OUTPUT = 0.25f
    const val AMPLITUDE_MIN_OUTPUT_THRESHHOLD = 0.1f

    fun normalize(
        source: List<Float>,
        trackWidth: Float,
        barWidth: Float,
        spacing: Float
    ): List<Float> {
        require(trackWidth >= 0f) { "Track width must be postitive." }
        require(trackWidth >= barWidth + spacing) { "Tarck width must be at least one bar wide plus spacing." }
        if (source.isEmpty()) {
            return emptyList()
        }

        val barCount = (trackWidth / (barWidth + spacing)).roundToInt()
        val resampled = when {
            barCount == source.size -> source
            barCount < source.size -> downsample(source, barCount)
            else -> upsample(source,barCount)
        }
        val remapped = remapAmplitudes(resampled)

        return remapped
    }

    private fun remapAmplitudes(amplitudes: List<Float>): List<Float> {
        val outputRange = MAX_OUTPUT - MIN_OUTPUT
        val scaleFactor = MAX_OUTPUT - AMPLITUDE_MIN_OUTPUT_THRESHHOLD
        return amplitudes.map { amp ->
            if (amp < AMPLITUDE_MIN_OUTPUT_THRESHHOLD) {
                MIN_OUTPUT
            } else {
                val amplitudeRange = amp - AMPLITUDE_MIN_OUTPUT_THRESHHOLD
                MIN_OUTPUT + (amplitudeRange * outputRange / scaleFactor)
            }
        }
    }

    private fun downsample(source: List<Float>, target: Int): List<Float> {
        val ratio = source.size.toFloat() / target
        return List(target) { index ->
            val start = (index * ratio).toInt()
            val end = ((index + 1) * ratio).toInt().coerceAtMost(source.size)
            source.subList(start, end).max()
        }

    }

    private fun upsample(source: List<Float>, target: Int): List<Float> {
        val result = mutableListOf<Float>()
        val step = (source.size - 1).toFloat() / (target - 1)
        for (i in 0 until target) {
            val pos = i * step
            val index = pos.toInt()
            val fraction = pos - index
            val value = if (index + 1 < source.size) {
                (1 - fraction) * source[index] + fraction * source[index + 1]
            } else source[index]

            result.add(value)
        }
        return result.toList()
    }
}