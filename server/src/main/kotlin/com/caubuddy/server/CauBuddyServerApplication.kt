package com.caubuddy.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CauBuddyServerApplication

fun main(args: Array<String>) {
    runApplication<CauBuddyServerApplication>(*args)
}
