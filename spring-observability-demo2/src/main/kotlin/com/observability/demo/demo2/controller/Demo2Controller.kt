package com.observability.demo.demo2.controller

import com.observability.demo.demo2.service.ProcessingService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class Demo2Controller(
    private val processingService: ProcessingService
) {
    @GetMapping("/process")
    fun process(): String = processingService.process()
}
