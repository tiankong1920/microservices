-- Supplier Service Schema
-- Initial migration for supplier service database

CREATE TABLE IF NOT EXISTS suppliers (
    id BIGSERIAL PRIMARY KEY,
    supplier_code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    address TEXT,
    city VARCHAR(100),
    country VARCHAR(100),
    product_category VARCHAR(255),
    payment_terms VARCHAR(100),
    lead_time_days INTEGER,
    rating DECIMAL(3,2) DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_suppliers_code ON suppliers(supplier_code);
CREATE INDEX IF NOT EXISTS idx_suppliers_category ON suppliers(product_category);
CREATE INDEX IF NOT EXISTS idx_suppliers_status ON suppliers(status);
