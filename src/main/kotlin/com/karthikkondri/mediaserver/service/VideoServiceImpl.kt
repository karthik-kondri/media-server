package com.karthikkondri.mediaserver.service

import com.karthikkondri.mediaserver.entity.VideoFileInfo
import com.karthikkondri.mediaserver.repository.VideoFileInfoRepository
import org.springframework.stereotype.Service

@Service
class VideoServiceImpl(private val videoFileInfoRepository: VideoFileInfoRepository): VideoService {
    override fun saveVideo(videoFileInfo: VideoFileInfo): VideoFileInfo = videoFileInfoRepository.save(videoFileInfo)
    override fun findVideos(q: String) = videoFileInfoRepository.findVideoFiles(q)
    override fun getVideoFile(id: String): VideoFileInfo? = videoFileInfoRepository.findById(id).orElse(null)
}
