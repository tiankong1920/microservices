package com.inventory.common.saga;

public interface SagaStep<T> {
    String getName();
    T execute(T data) throws SagaStepException;
    void compensate(T data) throws SagaStepException;
}
