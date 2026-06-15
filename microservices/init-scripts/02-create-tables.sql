-- Inventory Management System - Complete Database Schema
-- PostgreSQL 18+

-- ============================================
-- 1. WAREHOUSE TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS warehouse (
    id BIGSERIAL PRIMARY KEY,
    warehouse_code VARCHAR(50) NOT NULL UNIQUE,
    warehouse_name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    city VARCHAR(50),
    province VARCHAR(50),
    country VARCHAR(50),
    postal_code VARCHAR(20),
    contact_person VARCHAR(100),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    capacity INTEGER,
    current_usage INTEGER DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- ============================================
-- 2. BATCH TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS batch (
    id BIGSERIAL PRIMARY KEY,
    batch_number VARCHAR(50) NOT NULL UNIQUE,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT,
    production_date DATE,
    expiration_date DATE,
    quantity INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- ============================================
-- 3. INVENTORY TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS inventory (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT DEFAULT 0,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    batch_id BIGINT,
    quantity INTEGER NOT NULL DEFAULT 0,
    available_quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    unit_cost NUMERIC(19,4) NOT NULL DEFAULT 0,
    total_value NUMERIC(19,4),
    location VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- ============================================
-- 4. STOCK TRANSFER ORDER TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS stock_transfer_order (
    id BIGSERIAL PRIMARY KEY,
    transfer_number VARCHAR(50) NOT NULL UNIQUE,
    source_warehouse_id BIGINT NOT NULL,
    target_warehouse_id BIGINT NOT NULL,
    transfer_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- ============================================
-- 5. STOCK TRANSFER ITEM TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS stock_transfer_item (
    id BIGSERIAL PRIMARY KEY,
    transfer_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    batch_id BIGINT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 6. OTHER STOCK IN ORDER TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS other_stock_in_order (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    warehouse_id BIGINT NOT NULL,
    order_date TIMESTAMP NOT NULL,
    stock_in_type VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- ============================================
-- 7. OTHER STOCK OUT ORDER TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS other_stock_out_order (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    warehouse_id BIGINT NOT NULL,
    order_date TIMESTAMP NOT NULL,
    stock_out_type VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- ============================================
-- 8. CUSTOMERS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    address VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- ============================================
-- 9. SUPPLIERS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS suppliers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    address VARCHAR(255),
    product_category VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- ============================================
-- 10. BUSINESS PARTNER TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS business_partner (
    id BIGSERIAL PRIMARY KEY,
    partner_code VARCHAR(50) NOT NULL UNIQUE,
    partner_name VARCHAR(100) NOT NULL,
    partner_type VARCHAR(20) NOT NULL,
    contact_person VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    address VARCHAR(255),
    city VARCHAR(50),
    province VARCHAR(50),
    country VARCHAR(50),
    postal_code VARCHAR(20),
    tax_number VARCHAR(50),
    bank_name VARCHAR(100),
    bank_account VARCHAR(50),
    credit_limit NUMERIC(19,4) DEFAULT 0,
    payment_terms VARCHAR(100),
    notes TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- ============================================
-- 11. PROCUREMENT ORDER TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS procurement_order (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    supplier_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    order_date TIMESTAMP NOT NULL,
    expected_delivery_date TIMESTAMP,
    actual_delivery_date TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    subtotal NUMERIC(19,4) DEFAULT 0,
    tax NUMERIC(19,4) DEFAULT 0,
    shipping_cost NUMERIC(19,4) DEFAULT 0,
    total_amount NUMERIC(19,4) DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- ============================================
-- 12. PROCUREMENT ORDER ITEM TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS procurement_order_item (
    id BIGSERIAL PRIMARY KEY,
    procurement_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100),
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(19,4) NOT NULL,
    subtotal NUMERIC(19,4) DEFAULT 0,
    received_quantity INTEGER DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 13. PROCUREMENT RETURN ORDER TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS procurement_return_order (
    id BIGSERIAL PRIMARY KEY,
    return_number VARCHAR(50) NOT NULL UNIQUE,
    original_order_id BIGINT,
    supplier_id BIGINT NOT NULL,
    return_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount NUMERIC(19,4) DEFAULT 0,
    reason TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- ============================================
-- 14. PROCUREMENT RETURN ITEM TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS procurement_return_item (
    id BIGSERIAL PRIMARY KEY,
    return_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100),
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(19,4) NOT NULL,
    subtotal NUMERIC(19,4) DEFAULT 0,
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 15. SALES ORDER TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS sales_order (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    order_date TIMESTAMP NOT NULL,
    delivery_date TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    subtotal NUMERIC(19,4) DEFAULT 0,
    tax NUMERIC(19,4) DEFAULT 0,
    discount NUMERIC(19,4) DEFAULT 0,
    total_amount NUMERIC(19,4) DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- ============================================
-- 16. SALES ORDER ITEM TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS sales_order_item (
    id BIGSERIAL PRIMARY KEY,
    sales_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100),
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(19,4) NOT NULL,
    discount NUMERIC(19,4) DEFAULT 0,
    subtotal NUMERIC(19,4) DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 17. RETAIL ORDER TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS retail_order (
    id BIGSERIAL PRIMARY KEY,
    retail_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT,
    warehouse_id BIGINT NOT NULL,
    retail_date TIMESTAMP NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    subtotal NUMERIC(19,4) DEFAULT 0,
    tax NUMERIC(19,4) DEFAULT 0,
    discount NUMERIC(19,4) DEFAULT 0,
    total_amount NUMERIC(19,4) DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- ============================================
-- 18. RETAIL ORDER ITEM TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS retail_order_item (
    id BIGSERIAL PRIMARY KEY,
    retail_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100),
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(19,4) NOT NULL,
    discount NUMERIC(19,4) DEFAULT 0,
    subtotal NUMERIC(19,4) DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 19. SALES RETURN ORDER TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS sales_return_order (
    id BIGSERIAL PRIMARY KEY,
    return_number VARCHAR(50) NOT NULL UNIQUE,
    original_order_id BIGINT,
    customer_id BIGINT NOT NULL,
    return_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount NUMERIC(19,4) DEFAULT 0,
    reason TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- ============================================
-- 20. SALES RETURN ITEM TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS sales_return_item (
    id BIGSERIAL PRIMARY KEY,
    return_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100),
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(19,4) NOT NULL,
    subtotal NUMERIC(19,4) DEFAULT 0,
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 21. FINANCE VOUCHERS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS finance_vouchers (
    id BIGSERIAL PRIMARY KEY,
    voucher_number VARCHAR(50) NOT NULL UNIQUE,
    voucher_date DATE NOT NULL,
    description TEXT,
    amount NUMERIC(19,4) NOT NULL DEFAULT 0,
    voucher_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    reference_number VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- ============================================
-- 22. FINANCE ACCOUNT TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS finance_account (
    id BIGSERIAL PRIMARY KEY,
    account_code VARCHAR(50) NOT NULL UNIQUE,
    account_name VARCHAR(100) NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    parent_id BIGINT,
    balance NUMERIC(19,4) DEFAULT 0,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- ============================================
-- 23. SETTLEMENT ACCOUNT TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS settlement_account (
    id BIGSERIAL PRIMARY KEY,
    account_name VARCHAR(100) NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    bank_name VARCHAR(100),
    bank_account VARCHAR(50),
    balance NUMERIC(19,4) DEFAULT 0,
    currency VARCHAR(10) DEFAULT 'CNY',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- ============================================
-- 24. PAYMENT TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS payment (
    id BIGSERIAL PRIMARY KEY,
    payment_number VARCHAR(50) NOT NULL UNIQUE,
    supplier_id BIGINT,
    business_partner_id BIGINT,
    payment_date TIMESTAMP NOT NULL,
    amount NUMERIC(19,4) NOT NULL,
    payment_method VARCHAR(50),
    settlement_account_id BIGINT,
    reference_number VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- ============================================
-- 25. RECEIPT TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS receipt (
    id BIGSERIAL PRIMARY KEY,
    receipt_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT,
    business_partner_id BIGINT,
    receipt_date TIMESTAMP NOT NULL,
    amount NUMERIC(19,4) NOT NULL,
    payment_method VARCHAR(50),
    settlement_account_id BIGINT,
    reference_number VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- ============================================
-- 26. INCOME TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS income (
    id BIGSERIAL PRIMARY KEY,
    income_number VARCHAR(50) NOT NULL UNIQUE,
    income_date TIMESTAMP NOT NULL,
    amount NUMERIC(19,4) NOT NULL,
    income_type VARCHAR(50),
    category VARCHAR(50),
    description TEXT,
    settlement_account_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- ============================================
-- 27. EXPENSE TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS expense (
    id BIGSERIAL PRIMARY KEY,
    expense_number VARCHAR(50) NOT NULL UNIQUE,
    expense_date TIMESTAMP NOT NULL,
    amount NUMERIC(19,4) NOT NULL,
    expense_type VARCHAR(50),
    category VARCHAR(50),
    description TEXT,
    settlement_account_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- ============================================
-- 28. OAUTH2 USERS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS oauth2_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    mfa_secret VARCHAR(255),
    mfa_recovery_code VARCHAR(255)
);

-- ============================================
-- 29. OAUTH2 ROLES TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS oauth2_roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 30. OAUTH2 PERMISSIONS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS oauth2_permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    resource VARCHAR(100),
    action VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 31. OAUTH2 USER ROLES TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS oauth2_user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

-- ============================================
-- 32. SECURITY AUDIT LOG TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS security_audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    action VARCHAR(50) NOT NULL,
    resource VARCHAR(100),
    resource_id VARCHAR(50),
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 33. BACKUP RECORD TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS backup_record (
    id BIGSERIAL PRIMARY KEY,
    backup_name VARCHAR(100) NOT NULL,
    backup_type VARCHAR(50) NOT NULL,
    backup_path VARCHAR(255),
    backup_size BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 34. PASSWORD HISTORY TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS password_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- CREATE INDEXES
-- ============================================
CREATE INDEX IF NOT EXISTS idx_warehouse_code ON warehouse(warehouse_code);
CREATE INDEX IF NOT EXISTS idx_warehouse_active ON warehouse(is_active);

CREATE INDEX IF NOT EXISTS idx_batch_number ON batch(batch_number);
CREATE INDEX IF NOT EXISTS idx_batch_product ON batch(product_id);
CREATE INDEX IF NOT EXISTS idx_batch_status ON batch(status);

CREATE INDEX IF NOT EXISTS idx_inventory_product ON inventory(product_id);
CREATE INDEX IF NOT EXISTS idx_inventory_warehouse ON inventory(warehouse_id);
CREATE INDEX IF NOT EXISTS idx_inventory_batch ON inventory(batch_id);
CREATE INDEX IF NOT EXISTS idx_inventory_status ON inventory(status);

CREATE INDEX IF NOT EXISTS idx_customer_email ON customers(email);
CREATE INDEX IF NOT EXISTS idx_customer_phone ON customers(phone);
CREATE INDEX IF NOT EXISTS idx_customer_active ON customers(active);

CREATE INDEX IF NOT EXISTS idx_supplier_active ON suppliers(active);

CREATE INDEX IF NOT EXISTS idx_procurement_status ON procurement_order(status);
CREATE INDEX IF NOT EXISTS idx_procurement_date ON procurement_order(order_date);
CREATE INDEX IF NOT EXISTS idx_procurement_supplier ON procurement_order(supplier_id);

CREATE INDEX IF NOT EXISTS idx_sales_status ON sales_order(status);
CREATE INDEX IF NOT EXISTS idx_sales_date ON sales_order(order_date);
CREATE INDEX IF NOT EXISTS idx_sales_customer ON sales_order(customer_id);

CREATE INDEX IF NOT EXISTS idx_retail_status ON retail_order(payment_status);
CREATE INDEX IF NOT EXISTS idx_retail_date ON retail_order(retail_date);

CREATE INDEX IF NOT EXISTS idx_finance_voucher_date ON finance_vouchers(voucher_date);
CREATE INDEX IF NOT EXISTS idx_finance_voucher_type ON finance_vouchers(voucher_type);

CREATE INDEX IF NOT EXISTS idx_security_audit_user ON security_audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_security_audit_action ON security_audit_log(action);
CREATE INDEX IF NOT EXISTS idx_security_audit_date ON security_audit_log(created_at);

-- ============================================
-- INSERT DEFAULT DATA
-- ============================================
-- Default warehouse
INSERT INTO warehouse (warehouse_code, warehouse_name, address, city, province, country, is_active, is_primary)
VALUES ('WH001', 'Main Warehouse', '123 Main Street', 'Shanghai', 'Shanghai', 'China', TRUE, TRUE)
ON CONFLICT (warehouse_code) DO NOTHING;

-- Default admin user
INSERT INTO oauth2_users (username, password, email, enabled)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@example.com', TRUE)
ON CONFLICT (username) DO NOTHING;

-- Default role
INSERT INTO oauth2_roles (name, description)
VALUES ('ADMIN', 'Administrator role'), ('USER', 'Regular user role')
ON CONFLICT (name) DO NOTHING;

-- Link admin user to admin role
INSERT INTO oauth2_user_roles (user_id, role_id)
SELECT u.id, r.id FROM oauth2_users u, oauth2_roles r 
WHERE u.username = 'admin' AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- Log completion
DO $$
BEGIN
    RAISE NOTICE 'Database schema creation completed successfully';
END $$;
