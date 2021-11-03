package com.karthikkondri.mediaserver.controller

import com.karthikkondri.mediaserver.entity.VideoFileInfo
import com.karthikkondri.mediaserver.service.FileService
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.nio.file.Paths

@CrossOrigin
@RestController
@RequestMapping("/f")
class FileController(val fileService: FileService) {
    @PostMapping("/upload")
    fun upload(@RequestPart("file") filePartMono: Mono<FilePart>): Mono<VideoFileInfo> {
        return fileService.storePart(filePartMono)
    }
}
