package com.karthikkondri.mediaserver.dto

import java.nio.file.Path

data class ConversionProgress(
    val durationNs: Long,
    val processedNs: Long,
    val progressPercent: Long,
    val path: Path?
)
