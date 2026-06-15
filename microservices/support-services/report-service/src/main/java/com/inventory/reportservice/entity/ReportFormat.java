package com.inventory.reportservice.entity;

public enum ReportFormat {
    PDF("PDF"),
    EXCEL("Excel"),
    CSV("CSV"),
    JSON("JSON");

    private final String description;

    ReportFormat(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
