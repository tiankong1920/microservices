CREATE TABLE invoice_customer_info (
    id BIGSERIAL PRIMARY KEY,
    customer_name VARCHAR(200) NOT NULL,
    tax_number VARCHAR(100) NOT NULL,
    registered_address VARCHAR(500),
    contact_phone VARCHAR(50),
    mobile_phone VARCHAR(20),
    bank_name VARCHAR(200),
    bank_account VARCHAR(100),
    email VARCHAR(100),
    contact_person VARCHAR(100),
    remark TEXT,
    pinyin_initials VARCHAR(50),
    pinyin_full VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
    usage_count INTEGER DEFAULT 0,
    last_used_at TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    tenant_id VARCHAR(50),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_customer_name ON invoice_customer_info (customer_name);
CREATE INDEX idx_customer_tax_no ON invoice_customer_info (tax_number);
CREATE INDEX idx_customer_status ON invoice_customer_info (status);
CREATE INDEX idx_customer_pinyin ON invoice_customer_info (pinyin_initials);
CREATE INDEX idx_customer_usage ON invoice_customer_info (usage_count);

CREATE TABLE invoice_product (
    id BIGSERIAL PRIMARY KEY,
    product_name VARCHAR(200) NOT NULL,
    specification VARCHAR(200),
    unit_name VARCHAR(50) NOT NULL,
    unit_price DECIMAL(18,4) NOT NULL,
    tax_rate DECIMAL(5,2) NOT NULL,
    product_category VARCHAR(100),
    tax_category_code VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
    usage_count INTEGER DEFAULT 0,
    last_used_at TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    tenant_id VARCHAR(50),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_product_name ON invoice_product (product_name);
CREATE INDEX idx_product_spec ON invoice_product (specification);
CREATE INDEX idx_product_usage ON invoice_product (usage_count);

CREATE TABLE invoice_unit_info (
    id BIGSERIAL PRIMARY KEY,
    unit_name VARCHAR(50) NOT NULL UNIQUE,
    aliases VARCHAR(500),
    usage_count INTEGER DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    tenant_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_unit_name ON invoice_unit_info (unit_name);
CREATE INDEX idx_unit_usage ON invoice_unit_info (usage_count);

CREATE TABLE invoice_operation_log (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    operation_type VARCHAR(30) NOT NULL,
    operator VARCHAR(100),
    ip_address VARCHAR(50),
    old_value TEXT,
    new_value TEXT,
    description VARCHAR(500),
    tenant_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_oplog_entity ON invoice_operation_log (entity_type, entity_id);
CREATE INDEX idx_oplog_user ON invoice_operation_log (operator);
CREATE INDEX idx_oplog_time ON invoice_operation_log (created_at);

CREATE TABLE invoice_pinyin_index (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    entity_name VARCHAR(200) NOT NULL,
    pinyin_initials VARCHAR(50) NOT NULL,
    pinyin_full VARCHAR(500),
    tenant_id VARCHAR(50)
);

CREATE INDEX idx_pinyin_initials ON invoice_pinyin_index (pinyin_initials);
CREATE INDEX idx_pinyin_entity ON invoice_pinyin_index (entity_type, entity_id);
