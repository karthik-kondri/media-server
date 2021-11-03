package com.karthikkondri.mediaserver.service

import com.karthikkondri.mediaserver.entity.VideoFileInfo

interface VideoService {
    fun saveVideo(videoFileInfo: VideoFileInfo): VideoFileInfo
    fun findVideos(q: String): List<VideoFileInfo>
    fun getVideoFile(id: String): VideoFileInfo?
}
