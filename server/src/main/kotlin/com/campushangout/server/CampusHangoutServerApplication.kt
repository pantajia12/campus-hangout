package com.campushangout.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CampusHangoutServerApplication

fun main(args: Array<String>) {
    runApplication<CampusHangoutServerApplication>(*args)
}
