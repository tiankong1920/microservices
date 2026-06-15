package com.invoice.invoiceservice.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super("商品信息未找到，ID: " + id);
    }
}
