package com.inventory.reportservice.entity;

public enum ReportStatus {
    DRAFT("草稿"),
    GENERATING("生成中"),
    COMPLETED("已完成"),
    FAILED("生成失败"),
    CANCELLED("已取消");

    private final String description;

    ReportStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
