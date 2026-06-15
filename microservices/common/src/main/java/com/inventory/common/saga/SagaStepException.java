package com.inventory.common.saga;

public class SagaStepException extends RuntimeException {

    private final boolean retryable;

    public SagaStepException(String message) {
        super(message);
        this.retryable = false;
    }

    public SagaStepException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }

    public SagaStepException(String message, Throwable cause) {
        super(message, cause);
        this.retryable = false;
    }

    public SagaStepException(String message, Throwable cause, boolean retryable) {
        super(message, cause);
        this.retryable = retryable;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
