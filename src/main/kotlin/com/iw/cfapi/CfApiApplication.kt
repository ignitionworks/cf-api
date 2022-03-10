package com.iw.cfapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(CfConfig::class)
class CfApiApplication

fun main(args: Array<String>) {
	runApplication<CfApiApplication>(*args) {
	}
}
