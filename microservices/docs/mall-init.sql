-- 商城系统数据库初始化脚本
-- Database: mall_db

-- 创建数据库
CREATE DATABASE IF NOT EXISTS mall_db;
\c mall_db;

-- ============================================
-- 1. 商品管理模块
-- ============================================

CREATE TABLE IF NOT EXISTS product_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT REFERENCES product_categories(id),
    level INTEGER NOT NULL DEFAULT 1,
    sort_order INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    product_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    brand VARCHAR(100),
    category_id BIGINT REFERENCES product_categories(id),
    price DECIMAL(19,4) NOT NULL,
    cost_price DECIMAL(19,4),
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    description TEXT,
    main_image VARCHAR(255),
    video_url VARCHAR(255),
    weight DECIMAL(8,2),
    dimensions VARCHAR(100),
    created_by BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_specs (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    spec_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_spec_values (
    id BIGSERIAL PRIMARY KEY,
    spec_id BIGINT NOT NULL REFERENCES product_specs(id) ON DELETE CASCADE,
    value VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_images (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url VARCHAR(255) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_audits (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    old_status VARCHAR(20) NOT NULL,
    new_status VARCHAR(20) NOT NULL,
    auditor_id BIGINT,
    audit_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 2. 订单管理模块
-- ============================================

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    customer_address VARCHAR(255) NOT NULL,
    total_amount DECIMAL(19,4) NOT NULL,
    actual_amount DECIMAL(19,4) NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    order_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    payment_time TIMESTAMP,
    shipping_fee DECIMAL(19,4) DEFAULT 0,
    coupon_id BIGINT,
    discount_amount DECIMAL(19,4) DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    sku_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    sku_attributes JSONB NOT NULL DEFAULT '{}',
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(19,4) NOT NULL,
    subtotal DECIMAL(19,4) NOT NULL,
    product_image VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS shopping_carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    checked BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    payment_no VARCHAR(50) NOT NULL UNIQUE,
    amount DECIMAL(19,4) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'INIT',
    transaction_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 3. 营销活动模块
-- ============================================

CREATE TABLE IF NOT EXISTS coupon_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    value DECIMAL(19,4) NOT NULL,
    min_spend DECIMAL(19,4) DEFAULT 0,
    max_discount DECIMAL(19,4),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    usage_limit INTEGER,
    per_user_limit INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    applicable_categories JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS coupons (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES coupon_templates(id),
    coupon_code VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'UNUSED',
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at TIMESTAMP,
    order_id BIGINT
);

CREATE TABLE IF NOT EXISTS flash_sales (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS flash_sale_products (
    id BIGSERIAL PRIMARY KEY,
    flash_sale_id BIGINT NOT NULL REFERENCES flash_sales(id),
    sku_id BIGINT NOT NULL,
    flash_price DECIMAL(19,4) NOT NULL,
    stock_limit INTEGER NOT NULL,
    sold_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS group_buys (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    sku_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    group_size INTEGER NOT NULL,
    group_price DECIMAL(19,4) NOT NULL,
    single_price DECIMAL(19,4) NOT NULL,
    leader_discount DECIMAL(19,4),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bargain_activities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    sku_id BIGINT NOT NULL,
    original_price DECIMAL(19,4) NOT NULL,
    min_price DECIMAL(19,4) NOT NULL,
    max_bargain_count INTEGER NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS full_discounts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS full_discount_rules (
    id BIGSERIAL PRIMARY KEY,
    discount_id BIGINT NOT NULL REFERENCES full_discounts(id),
    min_amount DECIMAL(19,4) NOT NULL,
    discount_amount DECIMAL(19,4) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 4. 售后退款模块
-- ============================================

CREATE TABLE IF NOT EXISTS refund_applications (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    refund_type VARCHAR(20) NOT NULL,
    refund_amount DECIMAL(19,4) NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    evidence_images JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 5. 分销裂变模块
-- ============================================

CREATE TABLE IF NOT EXISTS distributors (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    distributor_code VARCHAR(50) NOT NULL UNIQUE,
    parent_id BIGINT,
    level INTEGER NOT NULL DEFAULT 1,
    commission_rate DECIMAL(5,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    total_commission DECIMAL(19,4) DEFAULT 0,
    available_commission DECIMAL(19,4) DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 索引
-- ============================================

CREATE INDEX IF NOT EXISTS idx_products_code ON products(product_code);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);
CREATE INDEX IF NOT EXISTS idx_categories_parent ON product_categories(parent_id);
CREATE INDEX IF NOT EXISTS idx_orders_user ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(order_status);
CREATE INDEX IF NOT EXISTS idx_orders_created ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_coupons_user_status ON coupons(user_id, status);
CREATE INDEX IF NOT EXISTS idx_cart_user ON shopping_carts(user_id);
CREATE INDEX IF NOT EXISTS idx_distributors_user ON distributors(user_id);
CREATE INDEX IF NOT EXISTS idx_distributors_parent ON distributors(parent_id);

-- ============================================
-- 初始数据
-- ============================================

INSERT INTO product_categories (name, level, sort_order, status) VALUES
('电子产品', 1, 1, 'ACTIVE'),
('服装', 1, 2, 'ACTIVE'),
('食品', 1, 3, 'ACTIVE'),
('家居', 1, 4, 'ACTIVE');

INSERT INTO product_categories (name, parent_id, level, sort_order, status) VALUES
('手机', 1, 2, 1, 'ACTIVE'),
('电脑', 1, 2, 2, 'ACTIVE'),
('男装', 2, 2, 1, 'ACTIVE'),
('女装', 2, 2, 2, 'ACTIVE');

INSERT INTO coupon_templates (name, type, value, min_spend, start_time, end_time, usage_limit, per_user_limit, status) VALUES
('新人专享券', 'FIXED', 30.00, 0.00, '2026-01-01', '2026-12-31', 10000, 1, 'ACTIVE'),
('满100减20', 'FIXED', 20.00, 100.00, '2026-01-01', '2026-12-31', 5000, 5, 'ACTIVE'),
('8折优惠券', 'PERCENTAGE', 20.00, 200.00, '2026-01-01', '2026-06-30', 1000, 2, 'ACTIVE');

INSERT INTO full_discounts (name, start_time, end_time, status) VALUES
('春季满减', '2026-03-01', '2026-05-31', 'ACTIVE');

INSERT INTO full_discount_rules (discount_id, min_amount, discount_amount, sort_order) VALUES
(1, 100.00, 10.00, 1),
(1, 200.00, 30.00, 2),
(1, 500.00, 80.00, 3);