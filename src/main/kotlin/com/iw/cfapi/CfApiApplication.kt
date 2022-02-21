package com.iw.cfapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.Banner

@SpringBootApplication
class CfApiApplication

fun main(args: Array<String>) {
	runApplication<CfApiApplication>(*args) {
		setBannerMode(Banner.Mode.OFF)
	}
}
