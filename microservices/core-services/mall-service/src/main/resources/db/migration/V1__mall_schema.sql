-- Mall Service Schema
-- Initial migration for mall service database

CREATE TABLE IF NOT EXISTS mall_order (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(19,4),
    discount_amount DECIMAL(19,4),
    pay_amount DECIMAL(19,4),
    freight_amount DECIMAL(19,4),
    status VARCHAR(20) NOT NULL,
    payment_status VARCHAR(20),
    delivery_status VARCHAR(20),
    payment_method VARCHAR(50),
    delivery_method VARCHAR(50),
    receiver_name VARCHAR(100),
    receiver_phone VARCHAR(50),
    receiver_address TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS mall_order_item (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200),
    product_sku VARCHAR(100),
    product_image VARCHAR(500),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(19,4),
    discount_amount DECIMAL(19,4),
    total_amount DECIMAL(19,4),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_mall_item_order FOREIGN KEY (order_id) REFERENCES mall_order(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS shopping_cart (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200),
    product_sku VARCHAR(100),
    product_image VARCHAR(500),
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(19,4),
    checked BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS coupon_template (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(19,4),
    min_purchase DECIMAL(19,4),
    max_discount DECIMAL(19,4),
    total_count INTEGER NOT NULL,
    remaining_count INTEGER NOT NULL,
    per_user_limit INTEGER DEFAULT 1,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS coupon (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'UNUSED',
    used_at TIMESTAMP,
    order_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_coupon_template FOREIGN KEY (template_id) REFERENCES coupon_template(id)
);

CREATE TABLE IF NOT EXISTS payment (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    payment_number VARCHAR(50) NOT NULL UNIQUE,
    payment_method VARCHAR(50) NOT NULL,
    payment_amount DECIMAL(19,4) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS shipment (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    shipment_number VARCHAR(50),
    carrier VARCHAR(100),
    delivery_method VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    shipped_at TIMESTAMP,
    received_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS refund_application (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    order_item_id BIGINT,
    refund_number VARCHAR(50) NOT NULL UNIQUE,
    reason TEXT,
    refund_amount DECIMAL(19,4),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS refund_audit (
    id BIGSERIAL PRIMARY KEY,
    refund_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    comment TEXT,
    operator VARCHAR(64),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_refund_audit_refund FOREIGN KEY (refund_id) REFERENCES refund_application(id)
);

CREATE TABLE IF NOT EXISTS order_status_history (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    old_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    comment TEXT,
    operator VARCHAR(64),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS flash_sale (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS flash_sale_product (
    id BIGSERIAL PRIMARY KEY,
    flash_sale_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    flash_price DECIMAL(19,4) NOT NULL,
    total_stock INTEGER NOT NULL,
    remaining_stock INTEGER NOT NULL,
    limit_per_user INTEGER DEFAULT 1,
    CONSTRAINT fk_flash_product_sale FOREIGN KEY (flash_sale_id) REFERENCES flash_sale(id)
);

CREATE TABLE IF NOT EXISTS group_buy (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    product_id BIGINT NOT NULL,
    group_price DECIMAL(19,4) NOT NULL,
    min_group_size INTEGER NOT NULL,
    max_group_size INTEGER,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS group_buy_detail (
    id BIGSERIAL PRIMARY KEY,
    group_buy_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_id BIGINT,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_group_detail_buy FOREIGN KEY (group_buy_id) REFERENCES group_buy(id)
);

CREATE TABLE IF NOT EXISTS bargain_activity (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    product_id BIGINT NOT NULL,
    original_price DECIMAL(19,4) NOT NULL,
    target_price DECIMAL(19,4) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bargain_record (
    id BIGSERIAL PRIMARY KEY,
    activity_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    current_price DECIMAL(19,4) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_bargain_record_activity FOREIGN KEY (activity_id) REFERENCES bargain_activity(id)
);

CREATE TABLE IF NOT EXISTS bargain_detail (
    id BIGSERIAL PRIMARY KEY,
    record_id BIGINT NOT NULL,
    helper_user_id BIGINT NOT NULL,
    bargain_amount DECIMAL(19,4) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_bargain_detail_record FOREIGN KEY (record_id) REFERENCES bargain_record(id)
);

CREATE TABLE IF NOT EXISTS full_discount (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    threshold_amount DECIMAL(19,4) NOT NULL,
    discount_value DECIMAL(19,4) NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS full_discount_rule (
    id BIGSERIAL PRIMARY KEY,
    full_discount_id BIGINT NOT NULL,
    min_amount DECIMAL(19,4) NOT NULL,
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(19,4) NOT NULL,
    CONSTRAINT fk_discount_rule_discount FOREIGN KEY (full_discount_id) REFERENCES full_discount(id)
);

CREATE TABLE IF NOT EXISTS distributor (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    parent_id BIGINT,
    level INTEGER DEFAULT 1,
    commission_rate DECIMAL(5,2) DEFAULT 0,
    total_sales DECIMAL(19,4) DEFAULT 0,
    total_commission DECIMAL(19,4) DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS commission_record (
    id BIGSERIAL PRIMARY KEY,
    distributor_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    order_amount DECIMAL(19,4) NOT NULL,
    commission_rate DECIMAL(5,2) NOT NULL,
    commission_amount DECIMAL(19,4) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_commission_distributor FOREIGN KEY (distributor_id) REFERENCES distributor(id)
);

CREATE INDEX IF NOT EXISTS idx_mall_order_number ON mall_order(order_number);
CREATE INDEX IF NOT EXISTS idx_mall_order_user ON mall_order(user_id);
CREATE INDEX IF NOT EXISTS idx_mall_order_status ON mall_order(status);
CREATE INDEX IF NOT EXISTS idx_shopping_cart_user ON shopping_cart(user_id);
CREATE INDEX IF NOT EXISTS idx_coupon_user ON coupon(user_id);
CREATE INDEX IF NOT EXISTS idx_payment_order ON payment(order_id);
