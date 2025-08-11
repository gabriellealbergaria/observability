package com.observability.demo.demo1.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class HttpConfig {

    @Bean
    fun restClient(builder: RestClient.Builder): RestClient =
        builder.build()
}
