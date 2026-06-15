package com.inventory.common.saga.order;

import com.inventory.common.saga.InMemorySagaLog;
import com.inventory.common.saga.SagaBuilder;
import com.inventory.common.saga.SagaLog;
import com.inventory.common.saga.SagaOrchestrator;
import com.inventory.common.saga.SagaState;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderSagaFactory {

    public static SagaOrchestrator<OrderSagaData> createOrderCreationSaga() {
        return createOrderCreationSaga(new InMemorySagaLog());
    }

    public static SagaOrchestrator<OrderSagaData> createOrderCreationSaga(SagaLog sagaLog) {
        return new SagaBuilder<OrderSagaData>()
                .withSagaLog(sagaLog)
                .withMaxRetries(3)
                .addSteps(
                        new CreateOrderStep(),
                        new ReserveInventoryStep(),
                        new ProcessPaymentStep()
                )
                .build();
    }

    public static OrderSagaData createSampleOrderData() {
        return OrderSagaData.builder()
                .orderNumber("ORD-" + System.currentTimeMillis())
                .customerId(1L)
                .totalAmount(new java.math.BigDecimal("999.99"))
                .items(java.util.List.of(
                        OrderSagaData.OrderItemData.builder()
                                .productId(100L)
                                .productName("Sample Product")
                                .quantity(2)
                                .unitPrice(new java.math.BigDecimal("499.99"))
                                .subtotal(new java.math.BigDecimal("999.98"))
                                .build()
                ))
                .build();
    }

    public static void main(String[] args) {
        log.info("=== Order Creation Saga Demo ===");

        SagaOrchestrator<OrderSagaData> saga = OrderSagaFactory.createOrderCreationSaga();
        OrderSagaData initialData = createSampleOrderData();

        log.info("Initial Order Data: OrderNumber={}, CustomerId={}, TotalAmount={}, Items={}",
                initialData.getOrderNumber(), initialData.getCustomerId(),
                initialData.getTotalAmount(), initialData.getItems().size());

        SagaState<OrderSagaData> result = saga.execute("ORDER_CREATION", initialData);

        log.info("Saga Execution Result: sagaId={}, status={}, orderStatus={}, orderId={}, "
                + "inventoryReserved={}, paymentProcessed={}, executedSteps={}",
                result.getSagaId(), result.getStatus(), result.getData().getStatus(),
                result.getData().getOrderId(), result.getData().isInventoryReserved(),
                result.getData().isPaymentProcessed(), result.getData().getExecutedSteps());

        if (result.getStatus() == SagaState.Status.COMPENSATED) {
            log.info("Compensated Steps: {}", result.getData().getCompensatedSteps());
        }

        log.info("Step History:");
        if (result.getStepHistory() != null) {
            for (SagaState.SagaStepRecord record : result.getStepHistory()) {
                log.info("[{}] {} - {}: {}",
                        record.getAction(),
                        record.getStepName(),
                        record.getStepStatus(),
                        record.getResult() != null ? record.getResult() : record.getErrorMessage());
            }
        }
    }
}
