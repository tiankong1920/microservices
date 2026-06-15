package com.inventory.common.saga;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class InMemorySagaLog implements SagaLog {

    private final Map<String, SagaState<?>> storage = new ConcurrentHashMap<>();

    @Override
    public void log(SagaState<?> state) {
        log.debug("Logging saga state: {} - {}", state.getSagaId(), state.getStatus());
        storage.put(state.getSagaId(), state);
    }

    @Override
    public SagaState<?> findById(String sagaId) {
        return storage.get(sagaId);
    }

    @Override
    public void update(SagaState<?> state) {
        storage.put(state.getSagaId(), state);
    }
}
