package ru.radeflex.substest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class SubsTestApplication

fun main(args: Array<String>) {
    runApplication<SubsTestApplication>(*args)
}
