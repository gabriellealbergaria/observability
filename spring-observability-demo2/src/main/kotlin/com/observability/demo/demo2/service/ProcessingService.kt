package com.observability.demo.demo2.service

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.metrics.DoubleHistogram
import io.opentelemetry.api.metrics.LongCounter
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import org.springframework.stereotype.Service
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.system.measureNanoTime

@Service
class ProcessingService(
    openTelemetry: OpenTelemetry
) {
    private val tracer: Tracer = openTelemetry.getTracer("demo2")
    private val meter = openTelemetry.getMeter("demo2")

    private val processedCounter: LongCounter = meter
        .counterBuilder("demo2_processed_total")
        .setDescription("Total processed requests (simulated)")
        .build()

    private val durationMs: DoubleHistogram = meter
        .histogramBuilder("demo2_processing_duration_ms")
        .setDescription("Processing duration in milliseconds")
        .setUnit("ms")
        .build()

    fun process(): String {
        val span = tracer.spanBuilder("demo2-processing")
            .setAttribute("workload.kind", "synthetic")
            .startSpan()

        try {
            span.addEvent("processing_started")

            val workload = pickWorkload()
            span.setAttribute("workload.variant", workload.name)

            var result = ""
            val elapsedNs = measureNanoTime {
                when (workload) {
                    Workload.CPU -> result = cpuBoundWork()
                    Workload.MEMORY -> result = memoryBoundWork()
                    Workload.IO_SIM -> result = ioLikeWork()
                }
            }

            durationMs.record(elapsedNs / 1_000_000.0) // ns -> ms
            processedCounter.add(1)

            span.addEvent("processing_finished", Attributes.empty())

            span.setStatus(StatusCode.OK)
            return "processed: $result"
        } catch (t: Throwable) {
            span.recordException(t)
            span.setStatus(StatusCode.ERROR)
            throw t
        } finally {
            span.end()
        }
    }

    private enum class Workload { CPU, MEMORY, IO_SIM }

    private fun pickWorkload(): Workload =
        when (Random.nextInt(3)) {
            0 -> Workload.CPU
            1 -> Workload.MEMORY
            else -> Workload.IO_SIM
        }

    // Simulate CPU-bound work (math loop)
    private fun cpuBoundWork(): String {
        var acc = 0.0
        for (i in 1..200_000) acc += sqrt(i.toDouble())
        return "cpu:${"%.2f".format(acc)}"
    }

    // Simulate memory pressure by allocating and touching arrays
    private fun memoryBoundWork(): String {
        val size = 250_000
        val data = IntArray(size) { it }
        var sum = 0L
        for (i in 0 until size step 3) sum += data[i]
        return "mem:$sum"
    }

    // Simulate I/O-like latency with sleep (no real external call)
    private fun ioLikeWork(): String {
        val delayMs = 50L + Random.nextLong(0, 150)
        Thread.sleep(delayMs)
        return "io:${delayMs}ms"
    }
}
