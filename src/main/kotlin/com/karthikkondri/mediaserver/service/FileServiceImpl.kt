package com.karthikkondri.mediaserver.service

import com.karthikkondri.mediaserver.entity.VideoFileInfo
import org.apache.commons.io.FilenameUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.MediaTypeFactory
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.nio.file.Path
import java.nio.file.Paths
import java.text.DateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.*

@Service
class FileServiceImpl(
    private val ffmpegService: FfmpegService,
    private val videoService: VideoService,
    @Value("\${media-server.video.location}") private val location: String
    ): FileService {

    override fun storePart(filePartMono: Mono<FilePart>): Mono<VideoFileInfo> {

        // Store temporarily
        val dirPath = Paths.get(location, "uploads", System.currentTimeMillis().toString())

        return filePartMono.doFirst { println("Upload started") }
            .doOnNext { println("File received ${it.filename()}") }
            .flatMap { fp ->
                println("Transferring file")
                val filename = fp.filename()
                val path = dirPath.resolve(filename)

                if (!path.parent.isDirectory()) {
                    path.parent.createDirectories()
                }

                fp.transferTo(path)
                    .hasElement()
                    .flatMap {
                        // Move to permanent location
                        val now = LocalDateTime.now()
                        val datestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(now)
                        val timestamp = DateTimeFormatter.ofPattern("HHmmssSSS").format(now)
                        val title = FilenameUtils.removeExtension(filename)

                        // Target path
                        val target = Paths.get(location, datestamp, timestamp, title, "${title}.mp4")
                        target.parent.createDirectories()

                        // Check if the video is mp4
                        val mediaType = MediaTypeFactory.getMediaType(filename).orElseThrow()
                        if (mediaType.toString().equals("video/mp4", true)) {
                            path.moveTo(target)
                        }
                        else {
                            // Convert to mp4
                            println("Converting $mediaType to mp4")
                            ffmpegService.convertToMp4(path, target)

                            // Cleanup
                            path.deleteIfExists() // the file
                        }

                        // uploads/timestamp directory
                        path.parent.deleteIfExists()

                        // Generate thumbnails
                        // Full size
                        println("Generating thumbnails")
                        val thumbnail = ffmpegService.generateThumbnails(target)

                        println("Saving file info")

                        val probe = ffmpegService.getProbe(target)
                        val videoFileInfo = VideoFileInfo(
                            null,
                            title,
                            mediaType.toString(),
                            probe.format.size,
                            probe.format.duration.toLong(),
                            target.toString().replace(Regex("^${location}/"), ""),
                            thumbnail.toString().replace(Regex("^${location}/"), ""),
                            null
                        )

                        Mono.just(videoService.saveVideo(videoFileInfo))
                    }
            }
    }
}
