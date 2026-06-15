package com.inventory.common.saga.order;

import com.inventory.common.saga.SagaStep;
import com.inventory.common.saga.SagaStepException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateOrderStep implements SagaStep<OrderSagaData> {

    @Override
    public String getName() {
        return "CREATE_ORDER";
    }

    @Override
    public OrderSagaData execute(OrderSagaData data) throws SagaStepException {
        log.info("Step [{}]: Creating order: {}", getName(), data.getOrderNumber());

        if (data.getOrderNumber() == null || data.getOrderNumber().isEmpty()) {
            throw new SagaStepException("Order number is required", false);
        }

        if (data.getCustomerId() == null) {
            throw new SagaStepException("Customer ID is required", false);
        }

        String orderId = "ORD-" + System.currentTimeMillis();
        data.setOrderId(orderId);
        data.setStatus("PENDING");

        log.info("Step [{}]: Order created successfully with ID: {}", getName(), orderId);

        return data;
    }

    @Override
    public void compensate(OrderSagaData data) throws SagaStepException {
        log.info("Step [{}]: Compensating - cancelling order: {}", getName(), data.getOrderId());

        if (data.getOrderId() != null) {
            log.info("Cancelling order: {}", data.getOrderId());
            data.addCompensatedStep(getName());
        }

        log.info("Step [{}]: Order cancelled", getName());
    }
}
