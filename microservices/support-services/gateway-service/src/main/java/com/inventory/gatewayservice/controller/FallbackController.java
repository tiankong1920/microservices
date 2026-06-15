package com.inventory.gatewayservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.inventory.common.core.ApiResponse;

@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    @GetMapping("/{service}")
    public ResponseEntity<ApiResponse<?>> fallbackService(@PathVariable String service) {
        log.warn("Fallback triggered for service: {}", service);
        return ResponseEntity
                .status(503)
                .body(ApiResponse.error("GATEWAY-FALLBACK",
                        "Service " + service + " is temporarily unavailable. Please try again later."));
    }
}
