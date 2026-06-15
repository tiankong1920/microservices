package com.inventory.common.saga.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "saga_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaLogEntity {

    @Id
    @Column(name = "saga_id")
    private String sagaId;

    @Column(name = "saga_type")
    private String sagaType;

    @Column(name = "status")
    private String status;

    @Column(name = "current_step")
    private String currentStep;

    @Column(name = "step_history", columnDefinition = "TEXT")
    private String stepHistory;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "data_snapshot", columnDefinition = "TEXT")
    private String dataSnapshot;
}
