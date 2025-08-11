# 📊 Observability

Local environment for **metrics, logs, and traces** with **OpenTelemetry Collector** as a single hub and **Prometheus**, **Loki**, **Tempo**, and **Grafana** backends. **Vendor-agnostic** model: applications speak **OTLP** to the Collector, which performs fan-out to the destinations.

---

## 🚀 Services (equivalents and justifications)

### 1) otel-collector
- **Image:** `otel/opentelemetry-collector-contrib:0.97.0`
- **Ports:** `4317` (OTLP gRPC), `4318` (OTLP HTTP), `8889` (Collector metrics)
- **Function:** Single OTLP entry point for metrics/logs/traces; exports to Tempo/Prometheus/Loki.
- **Equivalents:** Datadog Agent, New Relic Infra Agent, Elastic APM Agent.
- **Why:** Neutral standard, unified pipelines, easy to switch destinations without changing applications.

### 2) Prometheus
- **Image:** `prom/prometheus:v2.52.0`
- **Port:** `9090`
- **Function:** TSDB and metrics queries; receives **remote_write** from the Collector.
- **Equivalents:** Datadog Metrics, AWS CloudWatch Metrics, Azure Monitor Metrics.
- **Why:** 100% local/offline, zero cost in development, mature ecosystem.

### 3) Loki
- **Image:** `grafana/loki:3.0.0`
- **Port:** `3100`
- **Function:** Efficient log storage/querying, integrated with Grafana.
- **Equivalents:** Elasticsearch (ELK/EFK), Splunk, Datadog Logs.
- **Why:** Lightweight (indexes labels), simple to operate in a local environment.

### 4) Tempo
- **Image:** `grafana/tempo:2.4.1`
- **Port:** `3200` (query API)
- **Function:** Trace storage/querying; ingestion **via Collector** (does not expose 4317/4318 on host).
- **Equivalents:** Jaeger, Zipkin, Datadog APM, New Relic APM.
- **Why:** No relational DB, simple configuration, native Grafana/OTLP integration.

### 5) Grafana
- **Image:** `grafana/grafana:10.4.2`
- **Port:** `3000`
- **Function:** Unified visualization (dashboards/Explore/alerts), file-based provisioning.
- **Equivalents:** Kibana, Datadog Dashboards, New Relic One.
- **Why:** Natively integrates Prometheus/Loki/Tempo and easy to version-control.

### Stack extras
- **obs-health**: service that checks the health of endpoints (Collector/Prometheus/Tempo/Loki/Grafana).
- **obs-gen-traces | obs-gen-metrics | obs-gen-logs**: OTLP traffic generators for end-to-end smoke tests.

---

## 🗂 Profiles
- `observability` (full) • `observ-tracing` • `observ-metrics` • `observ-logs`

---

## 🔌 Flow

```
Apps (OTLP) → OTel Collector → metrics → Prometheus → Grafana
                              → logs   → Loki       → Grafana
                              → traces → Tempo      → Grafana
```

---

## ⚙️ Quick usage

```bash
# validate the compose
docker compose config

# start the full stack
docker compose --profile observability up -d

# health checker
docker compose run --rm obs-health

# generate traffic (optional)
docker compose run --rm obs-gen-traces
docker compose run --rm obs-gen-metrics
docker compose run --rm obs-gen-logs
```

**Access:**
- Grafana: `http://localhost:3000` (admin/admin)
- Prometheus: `http://localhost:9090`
- Loki API: `http://localhost:3100`
- Tempo (readiness): `http://localhost:3200/ready`
- Collector metrics: `http://localhost:8889/metrics`

---

## ☕ Example (Java agent v2 OTEL, HTTP/OTLP)

```bash
JAVA_TOOL_OPTIONS=-javaagent:opentelemetry-javaagent.jar

export OTEL_SERVICE_NAME=spring-observability-demo1
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318
export OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf

export OTEL_TRACES_EXPORTER=otlp
export OTEL_METRICS_EXPORTER=otlp
export OTEL_LOGS_EXPORTER=otlp

export OTEL_RESOURCE_ATTRIBUTES=env=local,team=eng,version=1.0.0
# Disable Micrometer auto-bridge if preferred
export OTEL_INSTRUMENTATION_MICROMETER_ENABLED=false
```

> Note: this is a **local vendor-agnostic lab**. It does not assume production details or proprietary exporters.

---

## 📁 Grafana provisioning

- **Datasources:** `stubs/observabilidade/grafana/provisioning/datasources/datasource.yaml`
- **Dashboards:** `stubs/observabilidade/grafana/provisioning/dashboards/observability-dashboard.json` (auto-imported)  