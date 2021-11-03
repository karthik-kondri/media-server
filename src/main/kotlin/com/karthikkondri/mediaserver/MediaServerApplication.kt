package com.karthikkondri.mediaserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MediaServerApplication

fun main(args: Array<String>) {
	runApplication<MediaServerApplication>(*args)
}
