-- V2: Add Procurement Orders
-- Version: 2.0.0
-- Description: Add procurement order management tables

BEGIN;

CREATE TABLE IF NOT EXISTS procurement_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    supplier_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expected_delivery_date DATE,
    actual_delivery_date TIMESTAMP,
    subtotal DECIMAL(12, 2) DEFAULT 0,
    tax DECIMAL(12, 2) DEFAULT 0,
    shipping_cost DECIMAL(12, 2) DEFAULT 0,
    discount DECIMAL(12, 2) DEFAULT 0,
    total_amount DECIMAL(12, 2) DEFAULT 0,
    notes TEXT,
    tracking_number VARCHAR(100),
    carrier VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_proc_orders_number ON procurement_orders(order_number);
CREATE INDEX idx_proc_orders_supplier ON procurement_orders(supplier_id);
CREATE INDEX idx_proc_orders_warehouse ON procurement_orders(warehouse_id);
CREATE INDEX idx_proc_orders_status ON procurement_orders(status);
CREATE INDEX idx_proc_orders_date ON procurement_orders(order_date);

CREATE TABLE IF NOT EXISTS procurement_order_items (
    id BIGSERIAL PRIMARY KEY,
    procurement_order_id BIGINT NOT NULL REFERENCES procurement_orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255),
    quantity INTEGER NOT NULL,
    received_quantity INTEGER DEFAULT 0,
    unit_price DECIMAL(12, 2) NOT NULL,
    discount DECIMAL(12, 2) DEFAULT 0,
    total_price DECIMAL(12, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_proc_items_order ON procurement_order_items(procurement_order_id);
CREATE INDEX idx_proc_items_product ON procurement_order_items(product_id);

CREATE TABLE IF NOT EXISTS procurement_returns (
    id BIGSERIAL PRIMARY KEY,
    return_number VARCHAR(50) UNIQUE NOT NULL,
    procurement_order_id BIGINT NOT NULL REFERENCES procurement_orders(id),
    reason VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    return_tracking_number VARCHAR(100),
    refund_amount DECIMAL(12, 2),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

CREATE INDEX idx_proc_returns_order ON procurement_returns(procurement_order_id);
CREATE INDEX idx_proc_returns_status ON procurement_returns(status);

INSERT INTO schema_migrations (version, description) VALUES ('V2__add_procurement_orders', 'Added procurement order tables');

COMMIT;
