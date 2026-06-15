package com.inventory.datasourceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {

    private long totalDatasources;

    private long activeDatasources;

    private long connectedDatasources;

    private long disconnectedDatasources;

    private long errorDatasources;

    private double averageResponseTime;

    private double connectionSuccessRate;

    private long totalTestCount;

    private long successTestCount;

    private long failedTestCount;

    private Map<String, Long> datasourceTypeDistribution;

    private Map<String, Long> connectionStatusDistribution;

    private LocalDateTime lastUpdated;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatasourceTypeStats {
        private String type;
        private long count;
        private long connected;
        private long disconnected;
        private double avgResponseTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSeriesData {
        private LocalDateTime timestamp;
        private Double value;
    }
}
