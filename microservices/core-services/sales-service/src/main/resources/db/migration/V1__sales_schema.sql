-- Sales Service Schema
-- Initial migration for sales service database

CREATE TABLE IF NOT EXISTS sales_order (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    order_date TIMESTAMP NOT NULL,
    delivery_date TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    subtotal DECIMAL(19,4),
    tax DECIMAL(19,4),
    discount DECIMAL(19,4),
    total_amount DECIMAL(19,4),
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS sales_order_item (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200),
    product_sku VARCHAR(100),
    quantity INTEGER NOT NULL,
    unit VARCHAR(20),
    unit_price DECIMAL(19,4),
    discount DECIMAL(19,4),
    tax_rate DECIMAL(5,2),
    subtotal DECIMAL(19,4),
    tax DECIMAL(19,4),
    total_amount DECIMAL(19,4),
    batch_number VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_sales_item_order FOREIGN KEY (order_id) REFERENCES sales_order(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sales_return_order (
    id BIGSERIAL PRIMARY KEY,
    return_number VARCHAR(50) NOT NULL UNIQUE,
    original_order_id BIGINT,
    customer_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    return_date TIMESTAMP NOT NULL,
    expected_refund_date TIMESTAMP,
    actual_refund_date TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    subtotal DECIMAL(19,4),
    tax DECIMAL(19,4),
    discount DECIMAL(19,4),
    total_amount DECIMAL(19,4),
    reason TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS sales_return_item (
    id BIGSERIAL PRIMARY KEY,
    return_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200),
    product_sku VARCHAR(100),
    quantity INTEGER NOT NULL,
    unit VARCHAR(20),
    unit_price DECIMAL(19,4),
    discount DECIMAL(19,4),
    tax_rate DECIMAL(5,2),
    subtotal DECIMAL(19,4),
    tax DECIMAL(19,4),
    total_amount DECIMAL(19,4),
    batch_number VARCHAR(50),
    reason TEXT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_sales_return_item_order FOREIGN KEY (return_order_id) REFERENCES sales_return_order(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS retail_order (
    id BIGSERIAL PRIMARY KEY,
    retail_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    retail_date TIMESTAMP NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    subtotal DECIMAL(19,4),
    tax DECIMAL(19,4),
    discount DECIMAL(19,4),
    total_amount DECIMAL(19,4),
    paid_amount DECIMAL(19,4),
    change_amount DECIMAL(19,4),
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS retail_order_item (
    id BIGSERIAL PRIMARY KEY,
    retail_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200),
    product_sku VARCHAR(100),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(19,4),
    discount DECIMAL(19,4),
    subtotal DECIMAL(19,4),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_retail_item_order FOREIGN KEY (retail_order_id) REFERENCES retail_order(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_sales_order_number ON sales_order(order_number);
CREATE INDEX IF NOT EXISTS idx_sales_order_customer ON sales_order(customer_id);
CREATE INDEX IF NOT EXISTS idx_sales_order_status ON sales_order(status);
CREATE INDEX IF NOT EXISTS idx_sales_return_number ON sales_return_order(return_number);
CREATE INDEX IF NOT EXISTS idx_retail_number ON retail_order(retail_number);
