package com.observability.demo.demo1.controller

import com.observability.demo.demo1.service.Demo2Service
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Tracer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SampleController(
    private val demo2Service: Demo2Service,
    openTelemetry: OpenTelemetry
) {
    private val tracer: Tracer = openTelemetry.getTracer("demo1")

    @GetMapping("/start")
    fun start(): String {
        val span = tracer.spanBuilder("start-span").startSpan()
        span.makeCurrent().use {
            span.addEvent("calling_demo2")
            val response = demo2Service.callDemo2()
            span.addEvent("demo2_called")
        }
        span.end()
        return "Requisição iniciada!"
    }
}
