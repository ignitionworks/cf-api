package com.iw.cfapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CfApiApplication

fun main(args: Array<String>) {
	runApplication<CfApiApplication>(*args) {
	}
}
