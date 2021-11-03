package com.karthikkondri.mediaserver.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "videoFileInfo")
data class VideoFileInfo(
    @Id val id: String?,
    val title: String,
    val mediaType: String,
    val size: Long,
    val duration: Long,
    val path: String,
    val thumbnail: String?,
    val uploadDate: LocalDateTime?
)
