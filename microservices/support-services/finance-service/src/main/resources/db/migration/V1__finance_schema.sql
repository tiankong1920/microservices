-- Finance Service Schema
-- Initial migration for finance service database

CREATE TABLE IF NOT EXISTS finance_account (
    id BIGSERIAL PRIMARY KEY,
    account_code VARCHAR(50) NOT NULL UNIQUE,
    account_name VARCHAR(200) NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    balance DECIMAL(19,4) DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    description TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS payment (
    id BIGSERIAL PRIMARY KEY,
    payment_number VARCHAR(50) NOT NULL UNIQUE,
    supplier_id BIGINT,
    supplier_name VARCHAR(200),
    payment_date TIMESTAMP NOT NULL,
    payment_amount DECIMAL(19,4),
    payment_method VARCHAR(50),
    related_document_type VARCHAR(50),
    related_document_number VARCHAR(50),
    related_document_id BIGINT,
    payment_status VARCHAR(20) NOT NULL,
    payer VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS receipt (
    id BIGSERIAL PRIMARY KEY,
    receipt_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT,
    customer_name VARCHAR(200),
    receipt_date TIMESTAMP NOT NULL,
    receipt_amount DECIMAL(19,4),
    receipt_method VARCHAR(50),
    related_document_type VARCHAR(50),
    related_document_number VARCHAR(50),
    related_document_id BIGINT,
    receipt_status VARCHAR(20) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS income (
    id BIGSERIAL PRIMARY KEY,
    income_number VARCHAR(50) NOT NULL UNIQUE,
    income_date TIMESTAMP NOT NULL,
    income_type VARCHAR(50) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    account_id BIGINT,
    customer_id BIGINT,
    customer_name VARCHAR(200),
    related_order_number VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS expense (
    id BIGSERIAL PRIMARY KEY,
    expense_number VARCHAR(50) NOT NULL UNIQUE,
    expense_date TIMESTAMP NOT NULL,
    expense_type VARCHAR(50) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    account_id BIGINT,
    supplier_id BIGINT,
    supplier_name VARCHAR(200),
    related_order_number VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS finance_voucher (
    id BIGSERIAL PRIMARY KEY,
    voucher_number VARCHAR(50) NOT NULL UNIQUE,
    voucher_date TIMESTAMP NOT NULL,
    voucher_type VARCHAR(50) NOT NULL,
    debit_account_id BIGINT,
    credit_account_id BIGINT,
    amount DECIMAL(19,4) NOT NULL,
    summary TEXT,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS settlement_account (
    id BIGSERIAL PRIMARY KEY,
    account_name VARCHAR(200) NOT NULL,
    account_number VARCHAR(100),
    bank_name VARCHAR(200),
    bank_branch VARCHAR(200),
    account_type VARCHAR(50) NOT NULL,
    balance DECIMAL(19,4) DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_payment_number ON payment(payment_number);
CREATE INDEX IF NOT EXISTS idx_payment_supplier ON payment(supplier_id);
CREATE INDEX IF NOT EXISTS idx_payment_status ON payment(payment_status);
CREATE INDEX IF NOT EXISTS idx_receipt_number ON receipt(receipt_number);
CREATE INDEX IF NOT EXISTS idx_receipt_customer ON receipt(customer_id);
CREATE INDEX IF NOT EXISTS idx_finance_account_code ON finance_account(account_code);
CREATE INDEX IF NOT EXISTS idx_voucher_number ON finance_voucher(voucher_number);
