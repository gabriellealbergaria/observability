plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.0"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.observability.demo"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Spring Boot
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	// Kotlin
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	// OpenTelemetry API e SDK (uso manual de spans e métricas)
	implementation("io.opentelemetry:opentelemetry-api:1.39.0")
	implementation("io.opentelemetry:opentelemetry-sdk:1.39.0")
	implementation("io.opentelemetry:opentelemetry-exporter-otlp:1.39.0")
	implementation("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure:1.39.0")

	// Propagadores de trace (ex: B3, W3C)
	implementation("io.opentelemetry:opentelemetry-extension-trace-propagators:1.39.0")

	// Testes
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.add("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
