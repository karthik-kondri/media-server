package com.karthikkondri.mediaserver.controller

import com.karthikkondri.mediaserver.entity.VideoFileInfo
import com.karthikkondri.mediaserver.service.VideoService
import com.karthikkondri.mediaserver.utils.resourceRegion
import org.apache.commons.io.FilenameUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.core.io.support.ResourceRegion
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import java.nio.file.Paths
import kotlin.io.path.exists

@CrossOrigin
@RestController
@RequestMapping("/videos")
class VideoController(
    val videoService: VideoService,
    @Value("\${media-server.video.location}") val location: String,
    @Value("\${media-server.video.chunk-size}") val chunkSize: Long
) {
    @GetMapping("/v/{id}/full")
    fun videoFull(@PathVariable id: String, @RequestHeader headers: HttpHeaders): ResponseEntity<Resource> {
        val video = UrlResource("file:${location}/${id}.mp4")
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
            .contentType(MediaTypeFactory.getMediaType(video).orElse(MediaType.APPLICATION_OCTET_STREAM))
            .body(video)
    }

    @GetMapping("/v/{id}")
    fun videoPartial(
        @PathVariable id: String,
        @RequestHeader headers: HttpHeaders
    ): ResponseEntity<ResourceRegion> {
        val videoFileInfo = videoService.getVideoFile(id)
        if (videoFileInfo != null) {
            val video = UrlResource("file:${location}/${videoFileInfo.path}")
            if (video.isFile) {
                val region = resourceRegion(video, headers, chunkSize)
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(MediaTypeFactory.getMediaType(video).orElse(MediaType.APPLICATION_OCTET_STREAM))
                    .body(region)
            }
        }
        return ResponseEntity.notFound().build()
    }

    @GetMapping("/t/{id}")
    fun thumbnail(@PathVariable id: String, @RequestParam(defaultValue = "0") size: Int): ResponseEntity<Resource> {
        val videoFileInfo = videoService.getVideoFile(id)
        if (videoFileInfo != null) {
            val thumbFilename = if (size > 0) {
                val thumb = "${FilenameUtils.removeExtension(videoFileInfo.thumbnail)}-${size}.png"
                if (Paths.get(location, thumb).exists()) {
                    thumb
                } else {
                    videoFileInfo.thumbnail
                }
            } else {
                videoFileInfo.thumbnail
            }

            val thumbnail = UrlResource("file:${location}/${thumbFilename}")
            if (thumbnail.isFile) {
                return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaTypeFactory.getMediaType(thumbnail).orElse(MediaType.APPLICATION_OCTET_STREAM))
                    .body(thumbnail)
            }
        }
        return ResponseEntity.notFound().build()
    }

    @GetMapping("/search")
    fun findVideos(@RequestParam q: String): ResponseEntity<List<VideoFileInfo>> =
        ResponseEntity.ok(videoService.findVideos(q))
}
