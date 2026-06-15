package com.inventory.templateservice.config;

import com.inventory.templateservice.repository.ITemplateRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class TemplateServiceHealthIndicator implements HealthIndicator {

    private final ITemplateRepository templateRepository;

    public TemplateServiceHealthIndicator(ITemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Override
    public Health health() {
        try {
            long totalTemplates = templateRepository.count();
            return Health.up()
                    .withDetail("totalTemplates", totalTemplates)
                    .withDetail("service", "template-service")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("service", "template-service")
                    .build();
        }
    }
}
