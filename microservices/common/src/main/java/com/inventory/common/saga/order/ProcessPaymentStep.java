package com.inventory.common.saga.order;

import com.inventory.common.saga.SagaStep;
import com.inventory.common.saga.SagaStepException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProcessPaymentStep implements SagaStep<OrderSagaData> {

    @Override
    public String getName() {
        return "PROCESS_PAYMENT";
    }

    @Override
    public OrderSagaData execute(OrderSagaData data) throws SagaStepException {
        log.info("Step [{}]: Processing payment for order: {}, amount: {}",
                getName(), data.getOrderNumber(), data.getTotalAmount());

        if (data.getTotalAmount() == null || data.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new SagaStepException("Invalid payment amount", false);
        }

        String transactionId = "PAY-" + System.currentTimeMillis();

        log.info("Payment processing: transaction={}, amount={}, customer={}",
                transactionId, data.getTotalAmount(), data.getCustomerId());

        data.markPaymentProcessed(transactionId);
        log.info("Step [{}]: Payment processed successfully with transaction: {}", getName(), transactionId);

        return data;
    }

    @Override
    public void compensate(OrderSagaData data) throws SagaStepException {
        log.info("Step [{}]: Compensating - refunding payment for order: {}",
                getName(), data.getOrderNumber());

        if (data.getPaymentTransactionId() != null) {
            log.info("Processing refund for transaction: {}", data.getPaymentTransactionId());
            log.info("Refunding {} to customer {}", data.getTotalAmount(), data.getCustomerId());

            data.addCompensatedStep(getName());
        }

        log.info("Step [{}]: Refund completed", getName());
    }
}
