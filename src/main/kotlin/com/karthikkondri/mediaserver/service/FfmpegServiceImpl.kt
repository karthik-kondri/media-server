package com.karthikkondri.mediaserver.service

import com.karthikkondri.mediaserver.utils.createThumb
import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.FFmpegUtils
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.builder.FFmpegBuilder
import net.bramp.ffmpeg.probe.FFmpegProbeResult
import net.bramp.ffmpeg.progress.ProgressListener
import org.apache.commons.io.FilenameUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.util.concurrent.TimeUnit


@Service
class FfmpegServiceImpl(@Value("\${ffmpeg.path}") private val ffmpegPath: String) : FfmpegService {
    private val ffmpeg = FFmpeg("${ffmpegPath}/ffmpeg")
    private val ffprobe = FFprobe("${ffmpegPath}/ffprobe")
    private val ffmpegExecutor = FFmpegExecutor(ffmpeg, ffprobe)

    override fun convertToMp4(source: Path, target: Path): Path {
        val pb = ffprobe.probe(source.toString())
        val builder = FFmpegBuilder()
            .setInput(pb)
            .overrideOutputFiles(true)
            .addOutput(target.toString())
            .setFormat("mp4")
            .done()

        val durationNs = pb.format.duration * TimeUnit.SECONDS.toNanos(1)
        val progressListener = ProgressListener { progress ->
            val percentage: Double = progress.out_time_ns / durationNs
            // Print out interesting information about the progress
            println(
                java.lang.String.format(
                    "[%.0f%%] status:%s frame:%d time:%s ms fps:%.0f speed:%.2fx",
                    percentage * 100,
                    progress.status,
                    progress.frame,
                    FFmpegUtils.toTimecode(progress.out_time_ns, TimeUnit.NANOSECONDS),
                    progress.fps.toDouble(),
                    progress.speed
                )
            )
        }

        // Run a one-pass encode
        val job = ffmpegExecutor.createJob(builder, progressListener)

        // Or run a two-pass encode (which is better quality at the cost of being slower)
        // val job = ffmpegExecutor.createTwoPassJob(builder)

        job.run()
        return target
    }

    override fun generateThumbnails(path: Path): Path {
        val filepath = path.toString()
        val title = FilenameUtils.removeExtension(path.toFile().name)
        val outPath = path.parent.resolve("${title}.png")

        val builder = FFmpegBuilder()
            .addInput(filepath)
            .overrideOutputFiles(true)
            .addOutput(outPath.toString())
            .setStartOffset(1, TimeUnit.SECONDS)
            .setVideoFrameRate(1, 1)
            .setFrames(1)
            .done()

        // Run a one-pass encode
        ffmpegExecutor.createJob(builder).run()

        // Or run a two-pass encode (which is better quality at the cost of being slower)
        // ffmpegExecutor.createTwoPassJob(builder).run()

        // Create small image
        val thumbFile = outPath.toFile()
        createThumb(thumbFile, 128)
        createThumb(thumbFile, 256)
        createThumb(thumbFile, 512)

        return outPath
    }

    override fun getProbe(path: Path): FFmpegProbeResult = ffprobe.probe(path.toString())
}
