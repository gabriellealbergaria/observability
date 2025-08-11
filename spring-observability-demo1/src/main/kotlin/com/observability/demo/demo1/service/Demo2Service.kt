package com.observability.demo.demo1.service

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.metrics.DoubleHistogram
import io.opentelemetry.api.metrics.LongCounter
import io.opentelemetry.api.metrics.LongUpDownCounter
import io.opentelemetry.api.trace.Tracer
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class Demo2Service(
    private val restClient: RestClient
) {
    private val otel = GlobalOpenTelemetry.get()
    private val tracer: Tracer = otel.getTracer("demo1")
    private val meter = otel.getMeter("demo1")

    private val callsCounter: LongCounter = meter
        .counterBuilder("demo2_calls") // aparece como demo2_calls_total
        .setDescription("Total calls to demo2 service")
        .build()

    private val latency: DoubleHistogram = meter
        .histogramBuilder("demo2_call_duration_seconds")
        .setUnit("s")
        .setDescription("Duration of demo2 calls")
        .build()

    private val inflight: LongUpDownCounter = meter
        .upDownCounterBuilder("demo2_inflight_requests")
        .setDescription("In-flight requests to demo2")
        .build()

    fun callDemo2(): String {
        val base = Attributes.builder()
            .put("http.method", "GET")
            .put("http.route", "/process")
            .put("peer.service", "spring-observability-demo2")
            .build()

        val success = base.toBuilder().put("outcome", "success").build()
        val error   = base.toBuilder().put("outcome", "error").build()

        inflight.add(1, base)
        val start = System.nanoTime()

        val span = tracer.spanBuilder("demo2-call")
            .setAttribute("peer.service", "spring-observability-demo2")
            .startSpan()

        try {
            span.addEvent("about_to_call_demo2")
            val body = restClient.get()
                .uri("http://localhost:8081/process")
                .retrieve()
                .body(String::class.java) ?: ""
            span.addEvent("received_response_from_demo2")
            callsCounter.add(1, success)
            return body
        } catch (e: Exception) {
            callsCounter.add(1, error)
            span.recordException(e)
            throw e
        } finally {
            val seconds = (System.nanoTime() - start) / 1_000_000_000.0
            latency.record(seconds, base)
            inflight.add(-1, base)
            span.end()
        }
    }
}
