package com.karthikkondri.mediaserver.repository

import com.karthikkondri.mediaserver.entity.VideoFileInfo
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface VideoFileInfoRepository: MongoRepository<VideoFileInfo, String> {
    @Query("{ title: { \$regex: ?0, \$options: 'i' } }")
    fun findVideoFiles(q: String): List<VideoFileInfo>
}