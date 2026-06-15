package com.inventory.common.saga;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SagaOrchestrator<T> {

    private static final long MAX_RETRY_DELAY_MS = 5000;
    private static final long INITIAL_RETRY_DELAY_MS = 1000;

    private final List<SagaStep<T>> steps;
    private final SagaLog sagaLog;
    private final int maxRetries;

    public SagaOrchestrator(List<SagaStep<T>> steps, SagaLog sagaLog) {
        this(steps, sagaLog, 3);
    }

    public SagaOrchestrator(List<SagaStep<T>> steps, SagaLog sagaLog, int maxRetries) {
        this.steps = steps;
        this.sagaLog = sagaLog;
        this.maxRetries = maxRetries;
    }

    public SagaState<T> execute(String sagaType, T initialData) {
        String sagaId = UUID.randomUUID().toString();
        SagaState<T> state = SagaState.<T>builder()
                .sagaId(sagaId)
                .sagaType(sagaType)
                .status(SagaState.Status.STARTING)
                .data(initialData)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sagaLog.log(state);
        state.setStatus(SagaState.Status.RUNNING);
        sagaLog.update(state);

        log.info("Starting saga: {} of type: {}", sagaId, sagaType);

        for (SagaStep<T> step : steps) {
            state.setCurrentStep(step.getName());
            log.info("Executing step: {} for saga: {}", step.getName(), sagaId);

            boolean stepCompleted = false;
            int attempts = 0;

            while (!stepCompleted && attempts < maxRetries) {
                attempts++;
                try {
                    T result = step.execute(state.getData());
                    state.setData(result);
                    state.addStepRecord(step.getName(), "execute", SagaState.Status.RUNNING, "success");
                    sagaLog.update(state);
                    stepCompleted = true;
                    log.info("Step: {} completed successfully for saga: {}", step.getName(), sagaId);
                } catch (SagaStepException e) {
                    log.warn("Step: {} failed for saga: {}, attempt: {}, error: {}",
                            step.getName(), sagaId, attempts, e.getMessage());
                    state.addStepError(step.getName(), "execute", e.getMessage());
                    state.setRetryCount(attempts);
                    sagaLog.update(state);

                    if (!e.isRetryable() || attempts >= maxRetries) {
                        log.error("Step: {} failed permanently for saga: {}, starting compensation", step.getName(), sagaId);
                        compensate(state);
                        return state;
                    }

                    long delayMs = Math.min(INITIAL_RETRY_DELAY_MS * (1L << (attempts - 1)), MAX_RETRY_DELAY_MS);
                    try {
                        TimeUnit.MILLISECONDS.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        compensate(state);
                        state.setStatus(SagaState.Status.FAILED);
                        return state;
                    }
                }
            }
        }

        state.setStatus(SagaState.Status.COMPLETED);
        state.setCompletedAt(LocalDateTime.now());
        state.setUpdatedAt(LocalDateTime.now());
        sagaLog.update(state);
        log.info("Saga: {} completed successfully", sagaId);

        return state;
    }

    private void compensate(SagaState<T> state) {
        state.setStatus(SagaState.Status.COMPENSATING);
        sagaLog.update(state);

        log.info("Starting compensation for saga: {}", state.getSagaId());

        List<SagaState.SagaStepRecord> history = state.getStepHistory();
        if (history == null || history.isEmpty()) {
            log.info("No steps to compensate for saga: {}", state.getSagaId());
            state.setStatus(SagaState.Status.COMPENSATED);
            sagaLog.update(state);
            return;
        }

        for (int i = history.size() - 1; i >= 0; i--) {
            SagaState.SagaStepRecord record = history.get(i);
            String stepName = record.getStepName();

            SagaStep<T> step = steps.stream()
                    .filter(s -> s.getName().equals(stepName))
                    .findFirst()
                    .orElse(null);

            if (step == null) {
                log.warn("Step: {} not found for compensation", stepName);
                continue;
            }

            try {
                log.info("Compensating step: {} for saga: {}", stepName, state.getSagaId());
                step.compensate(state.getData());
                state.addStepRecord(stepName, "compensate", SagaState.Status.COMPENSATING, "compensated");
                sagaLog.update(state);
                log.info("Step: {} compensated successfully for saga: {}", stepName, state.getSagaId());
            } catch (SagaStepException e) {
                log.error("Compensation failed for step: {} of saga: {}, error: {}",
                        stepName, state.getSagaId(), e.getMessage());
                state.addStepError(stepName, "compensate", e.getMessage());
                sagaLog.update(state);
            }
        }

        state.setStatus(SagaState.Status.COMPENSATED);
        state.setUpdatedAt(LocalDateTime.now());
        sagaLog.update(state);
        log.info("Compensation completed for saga: {}", state.getSagaId());
    }

    public SagaState<T> getSagaState(String sagaId) {
        return (SagaState<T>) sagaLog.findById(sagaId);
    }
}
