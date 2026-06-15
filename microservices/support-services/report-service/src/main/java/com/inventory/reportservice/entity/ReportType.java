package com.inventory.reportservice.entity;

public enum ReportType {
    SALES_REPORT("销售报表"),
    PURCHASE_REPORT("采购报表"),
    INVENTORY_REPORT("库存报表"),
    FINANCIAL_REPORT("财务报表"),
    CUSTOMER_REPORT("客户报表"),
    SUPPLIER_REPORT("供应商报表");

    private final String description;

    ReportType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
