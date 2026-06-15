package com.inventory.common.saga.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSagaData {

    private String orderId;
    private String orderNumber;
    private Long customerId;
    private List<OrderItemData> items;
    private BigDecimal totalAmount;
    private String status;

    private String inventoryTransactionId;
    private boolean inventoryReserved;
    private String paymentTransactionId;
    private boolean paymentProcessed;

    private List<String> executedSteps;
    private List<String> compensatedSteps;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemData {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }

    public void markInventoryReserved(String transactionId) {
        this.inventoryReserved = true;
        this.inventoryTransactionId = transactionId;
        if (this.executedSteps == null) {
            this.executedSteps = new java.util.ArrayList<>();
        }
        this.executedSteps.add("RESERVE_INVENTORY");
    }

    public void markPaymentProcessed(String transactionId) {
        this.paymentProcessed = true;
        this.paymentTransactionId = transactionId;
        if (this.executedSteps == null) {
            this.executedSteps = new java.util.ArrayList<>();
        }
        this.executedSteps.add("PROCESS_PAYMENT");
    }

    public void addCompensatedStep(String step) {
        if (this.compensatedSteps == null) {
            this.compensatedSteps = new java.util.ArrayList<>();
        }
        this.compensatedSteps.add(step);
    }
}
