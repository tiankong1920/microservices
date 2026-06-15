package com.inventory.common.saga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.common.saga.entity.SagaLogEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JpaSagaLog implements SagaLog {

    @PersistenceContext
    private EntityManager entityManager;

    private final ObjectMapper objectMapper;

    public JpaSagaLog(EntityManager entityManager, ObjectMapper objectMapper) {
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
    }

    @Override
    public void log(SagaState<?> state) {
        SagaLogEntity entity = mapToEntity(state);
        entityManager.persist(entity);
        log.debug("Persisted saga state: {} - {}", state.getSagaId(), state.getStatus());
    }

    @Override
    public SagaState<?> findById(String sagaId) {
        SagaLogEntity entity = entityManager.find(SagaLogEntity.class, sagaId);
        if (entity == null) {
            return null;
        }
        return mapToState(entity);
    }

    @Override
    public void update(SagaState<?> state) {
        SagaLogEntity entity = mapToEntity(state);
        entityManager.merge(entity);
        log.debug("Updated saga state: {} - {}", state.getSagaId(), state.getStatus());
    }

    private SagaLogEntity mapToEntity(SagaState<?> state) {
        String dataSnapshot = null;
        if (state.getData() != null) {
            try {
                dataSnapshot = objectMapper.writeValueAsString(state.getData());
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize saga data", e);
            }
        }

        String stepHistoryJson = null;
        if (state.getStepHistory() != null) {
            try {
                stepHistoryJson = objectMapper.writeValueAsString(state.getStepHistory());
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize step history", e);
            }
        }

        return SagaLogEntity.builder()
                .sagaId(state.getSagaId())
                .sagaType(state.getSagaType())
                .status(state.getStatus() != null ? state.getStatus().name() : null)
                .currentStep(state.getCurrentStep())
                .stepHistory(stepHistoryJson)
                .errorMessage(state.getErrorMessage())
                .createdAt(state.getCreatedAt())
                .updatedAt(state.getUpdatedAt())
                .completedAt(state.getCompletedAt())
                .dataSnapshot(dataSnapshot)
                .build();
    }

    @SuppressWarnings("unchecked")
    private SagaState<?> mapToState(SagaLogEntity entity) {
        Object data = null;
        if (entity.getDataSnapshot() != null) {
            try {
                data = objectMapper.readValue(entity.getDataSnapshot(), Object.class);
            } catch (JsonProcessingException e) {
                log.warn("Failed to deserialize saga data", e);
            }
        }

        return SagaState.builder()
                .sagaId(entity.getSagaId())
                .sagaType(entity.getSagaType())
                .status(entity.getStatus() != null ? SagaState.Status.valueOf(entity.getStatus()) : null)
                .currentStep(entity.getCurrentStep())
                .errorMessage(entity.getErrorMessage())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .completedAt(entity.getCompletedAt())
                .data(data)
                .build();
    }
}
