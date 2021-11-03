package com.karthikkondri.mediaserver.utils

import org.apache.commons.io.FilenameUtils
import org.springframework.core.io.UrlResource
import org.springframework.core.io.support.ResourceRegion
import org.springframework.http.HttpHeaders
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min

const val DEFAULT_CHUNK_SIZE: Long = 1L * 1024 * 1024

fun resourceRegion(video: UrlResource, headers: HttpHeaders, chunkSize: Long = DEFAULT_CHUNK_SIZE): ResourceRegion {
    val length = video.contentLength()
    val range = headers.range.firstOrNull()
    return if (range != null) {
        val start = range.getRangeStart(length)
        val end = range.getRangeEnd(length)
        val rangeLength = min(chunkSize, end - start + 1)
        ResourceRegion(video, start, rangeLength)
    } else {
        val rangeLength = min(chunkSize, length)
        ResourceRegion(video, 0, rangeLength)
    }
}

fun createThumb(file: File, size: Int) {
    val title = FilenameUtils.removeExtension(file.name)
    val image = ImageIO.read(file)
    val ratio = size / max(image.width, image.height).toDouble()
    val nw = (image.width * ratio).toInt()
    val nh = (image.height * ratio).toInt()
    val th32 = BufferedImage(nw, nh, BufferedImage.TYPE_3BYTE_BGR)
    th32.graphics.drawImage(image, 0, 0, nw, nh, null)
    ImageIO.write(th32, "png", File(file.parent, "${title}-${size}.png"))
}
