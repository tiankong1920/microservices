package com.inventory.common.saga.order;

import com.inventory.common.saga.SagaStep;
import com.inventory.common.saga.SagaStepException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReserveInventoryStep implements SagaStep<OrderSagaData> {

    @Override
    public String getName() {
        return "RESERVE_INVENTORY";
    }

    @Override
    public OrderSagaData execute(OrderSagaData data) throws SagaStepException {
        log.info("Step [{}]: Reserving inventory for order: {}", getName(), data.getOrderNumber());

        if (data.getItems() == null || data.getItems().isEmpty()) {
            throw new SagaStepException("No items to reserve", false);
        }

        String transactionId = "INV-" + System.currentTimeMillis();

        for (OrderSagaData.OrderItemData item : data.getItems()) {
            log.info("Reserving {} units of product {} for order {}",
                    item.getQuantity(), item.getProductId(), data.getOrderNumber());
        }

        data.markInventoryReserved(transactionId);
        log.info("Step [{}]: Inventory reserved successfully with transaction: {}", getName(), transactionId);

        return data;
    }

    @Override
    public void compensate(OrderSagaData data) throws SagaStepException {
        log.info("Step [{}]: Compensating - releasing inventory reservation for order: {}",
                getName(), data.getOrderNumber());

        if (data.getInventoryTransactionId() != null) {
            log.info("Releasing inventory reservation with transaction: {}", data.getInventoryTransactionId());

            for (OrderSagaData.OrderItemData item : data.getItems()) {
                log.info("Releasing {} units of product {}", item.getQuantity(), item.getProductId());
            }

            data.addCompensatedStep(getName());
        }

        log.info("Step [{}]: Compensation completed", getName());
    }
}
