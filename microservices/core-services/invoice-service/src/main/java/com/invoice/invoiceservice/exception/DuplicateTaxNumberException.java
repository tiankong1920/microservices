package com.invoice.invoiceservice.exception;

public class DuplicateTaxNumberException extends RuntimeException {

    public DuplicateTaxNumberException(String taxNumber) {
        super("纳税人识别号已存在: " + taxNumber);
    }
}
