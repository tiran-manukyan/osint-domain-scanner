package com.osint.backend.config

import com.osint.backend.util.CustomInstantSerializer
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Instant

@Configuration
class JacksonConfig {

    @Bean
    fun customizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder
                .serializerByType(Instant::class.java, CustomInstantSerializer())
        }
    }
}
