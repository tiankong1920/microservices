package com.inventory.common.saga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaState<T> {

    private String sagaId;
    private String sagaType;
    private Status status;
    private T data;
    private List<SagaStepRecord> stepHistory;
    private String currentStep;
    private String errorMessage;
    private int retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    public enum Status {
        STARTING,
        RUNNING,
        COMPLETED,
        FAILED,
        COMPENSATING,
        COMPENSATED,
        CANCELLED
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SagaStepRecord {
        private String stepName;
        private String action;
        private Status stepStatus;
        private String result;
        private LocalDateTime executedAt;
        private String errorMessage;
    }

    public void addStepRecord(String stepName, String action, Status stepStatus, String result) {
        if (stepHistory == null) {
            stepHistory = new ArrayList<>();
        }
        stepHistory.add(SagaStepRecord.builder()
                .stepName(stepName)
                .action(action)
                .stepStatus(stepStatus)
                .result(result)
                .executedAt(LocalDateTime.now())
                .build());
    }

    public void addStepError(String stepName, String action, String errorMessage) {
        if (stepHistory == null) {
            stepHistory = new ArrayList<>();
        }
        stepHistory.add(SagaStepRecord.builder()
                .stepName(stepName)
                .action(action)
                .stepStatus(Status.FAILED)
                .errorMessage(errorMessage)
                .executedAt(LocalDateTime.now())
                .build());
    }
}
