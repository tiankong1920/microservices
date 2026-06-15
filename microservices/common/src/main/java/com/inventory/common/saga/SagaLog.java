package com.inventory.common.saga;

public interface SagaLog {
    void log(SagaState<?> state);
    SagaState<?> findById(String sagaId);
    void update(SagaState<?> state);
}
