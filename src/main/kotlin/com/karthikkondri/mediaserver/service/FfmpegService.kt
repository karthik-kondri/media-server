package com.karthikkondri.mediaserver.service

import net.bramp.ffmpeg.probe.FFmpegProbeResult
import java.nio.file.Path

interface FfmpegService {
    fun convertToMp4(source: Path, target: Path): Path
    fun generateThumbnails(path: Path): Path
    fun getProbe(path: Path): FFmpegProbeResult
}