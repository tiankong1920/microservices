package com.inventory.common.saga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SagaBuilder<T> {

    private final List<SagaStep<T>> steps = new ArrayList<>();
    private SagaLog sagaLog = new InMemorySagaLog();
    private int maxRetries = 3;

    public SagaBuilder<T> addStep(SagaStep<T> step) {
        steps.add(step);
        return this;
    }

    public SagaBuilder<T> addSteps(SagaStep<T>... steps) {
        this.steps.addAll(Arrays.asList(steps));
        return this;
    }

    public SagaBuilder<T> withSagaLog(SagaLog sagaLog) {
        this.sagaLog = sagaLog;
        return this;
    }

    public SagaBuilder<T> withMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public SagaOrchestrator<T> build() {
        return new SagaOrchestrator<>(steps, sagaLog, maxRetries);
    }
}
