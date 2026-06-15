-- V4: Add Inventory Alert System
-- Version: 4.0.0
-- Description: Add inventory alert thresholds and alert logs

BEGIN;

CREATE TABLE IF NOT EXISTS inventory_alert_thresholds (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    low_stock_threshold INTEGER DEFAULT 20,
    critical_stock_threshold INTEGER DEFAULT 5,
    reorder_point INTEGER DEFAULT 10,
    alert_email VARCHAR(255),
    is_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_id, warehouse_id)
);

CREATE INDEX idx_alert_threshold_product ON inventory_alert_thresholds(product_id);
CREATE INDEX idx_alert_threshold_warehouse ON inventory_alert_thresholds(warehouse_id);
CREATE INDEX idx_alert_threshold_enabled ON inventory_alert_thresholds(is_enabled);

CREATE TABLE IF NOT EXISTS inventory_alert_logs (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    current_quantity INTEGER NOT NULL,
    threshold_value INTEGER NOT NULL,
    email_status VARCHAR(50) DEFAULT 'PENDING',
    email_sent_to VARCHAR(255),
    email_sent_at TIMESTAMP,
    is_acknowledged BOOLEAN DEFAULT FALSE,
    acknowledged_by VARCHAR(255),
    acknowledged_at TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_alert_log_product ON inventory_alert_logs(product_id);
CREATE INDEX idx_alert_log_warehouse ON inventory_alert_logs(warehouse_id);
CREATE INDEX idx_alert_log_type ON inventory_alert_logs(alert_type);
CREATE INDEX idx_alert_log_status ON inventory_alert_logs(email_status);
CREATE INDEX idx_alert_log_acknowledged ON inventory_alert_logs(is_acknowledged);
CREATE INDEX idx_alert_log_created ON inventory_alert_logs(created_at);

CREATE TABLE IF NOT EXISTS alert_notification_settings (
    id BIGSERIAL PRIMARY KEY,
    notification_type VARCHAR(50) NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    is_enabled BOOLEAN DEFAULT TRUE,
    notify_on_low_stock BOOLEAN DEFAULT TRUE,
    notify_on_critical_stock BOOLEAN DEFAULT TRUE,
    notify_on_out_of_stock BOOLEAN DEFAULT TRUE,
    notify_on_reorder_point BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notification_settings_type ON alert_notification_settings(notification_type);

ALTER TABLE inventory ADD COLUMN IF NOT EXISTS low_stock_threshold INTEGER DEFAULT 10;
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS critical_stock_threshold INTEGER DEFAULT 5;

INSERT INTO schema_migrations (version, description) VALUES ('V4__add_inventory_alerts', 'Added inventory alert system tables');

COMMIT;
