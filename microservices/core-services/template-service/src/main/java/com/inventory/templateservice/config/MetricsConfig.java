package com.inventory.templateservice.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter templateCreatedCounter(MeterRegistry registry) {
        return Counter.builder("template.created.count")
                .description("Number of templates created")
                .tag("service", "template-service")
                .register(registry);
    }

    @Bean
    public Counter templateUpdatedCounter(MeterRegistry registry) {
        return Counter.builder("template.updated.count")
                .description("Number of templates updated")
                .tag("service", "template-service")
                .register(registry);
    }

    @Bean
    public Counter templateDeletedCounter(MeterRegistry registry) {
        return Counter.builder("template.deleted.count")
                .description("Number of templates deleted")
                .tag("service", "template-service")
                .register(registry);
    }

    @Bean
    public Counter versionCreatedCounter(MeterRegistry registry) {
        return Counter.builder("template.version.created.count")
                .description("Number of template versions created")
                .tag("service", "template-service")
                .register(registry);
    }

    @Bean
    public Counter importCounter(MeterRegistry registry) {
        return Counter.builder("template.import.count")
                .description("Number of template imports")
                .tag("service", "template-service")
                .register(registry);
    }

    @Bean
    public Counter exportCounter(MeterRegistry registry) {
        return Counter.builder("template.export.count")
                .description("Number of template exports")
                .tag("service", "template-service")
                .register(registry);
    }

    @Bean
    public Counter errorCounter(MeterRegistry registry) {
        return Counter.builder("template.error.count")
                .description("Number of errors in template service")
                .tag("service", "template-service")
                .register(registry);
    }

    @Bean
    public Timer templateQueryTimer(MeterRegistry registry) {
        return Timer.builder("template.query.duration")
                .description("Time taken to query templates")
                .tag("service", "template-service")
                .register(registry);
    }

    @Bean
    public Timer templateCreateTimer(MeterRegistry registry) {
        return Timer.builder("template.create.duration")
                .description("Time taken to create a template")
                .tag("service", "template-service")
                .register(registry);
    }

    @Bean
    public Timer templateUpdateTimer(MeterRegistry registry) {
        return Timer.builder("template.update.duration")
                .description("Time taken to update a template")
                .tag("service", "template-service")
                .register(registry);
    }
}
