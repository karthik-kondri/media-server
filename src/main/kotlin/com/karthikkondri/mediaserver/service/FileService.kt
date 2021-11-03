package com.karthikkondri.mediaserver.service

import com.karthikkondri.mediaserver.entity.VideoFileInfo
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Mono

interface FileService {
    fun storePart(filePartMono: Mono<FilePart>): Mono<VideoFileInfo>
}
