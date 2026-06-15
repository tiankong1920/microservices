-- Product Service Schema
-- Initial migration for product service database

CREATE TABLE IF NOT EXISTS product_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    parent_id BIGINT,
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES product_categories(id)
);

CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    product_code VARCHAR(255) NOT NULL UNIQUE,
    sku VARCHAR(255) UNIQUE,
    name VARCHAR(255) NOT NULL,
    brand VARCHAR(255),
    category_id BIGINT,
    price DECIMAL(19,4) NOT NULL,
    cost_price DECIMAL(19,4),
    stock_quantity INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    description TEXT,
    main_image VARCHAR(255),
    video_url VARCHAR(255),
    weight DECIMAL(8,2),
    dimensions VARCHAR(255),
    created_by BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES product_categories(id)
);

CREATE TABLE IF NOT EXISTS product_skus (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    sku_code VARCHAR(255) NOT NULL UNIQUE,
    barcode VARCHAR(255),
    price DECIMAL(19,4) NOT NULL,
    cost_price DECIMAL(19,4),
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    attributes JSONB,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_sku_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS product_specs (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    sort_order INTEGER DEFAULT 0,
    CONSTRAINT fk_spec_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS product_spec_values (
    id BIGSERIAL PRIMARY KEY,
    spec_id BIGINT NOT NULL,
    value VARCHAR(255) NOT NULL,
    sort_order INTEGER DEFAULT 0,
    CONSTRAINT fk_spec_value_spec FOREIGN KEY (spec_id) REFERENCES product_specs(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS product_images (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    sort_order INTEGER DEFAULT 0,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_image_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS product_audits (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    comment TEXT,
    operated_by BIGINT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_audit_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS boms (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    version INTEGER DEFAULT 1,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_bom_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bom_components (
    id BIGSERIAL PRIMARY KEY,
    bom_id BIGINT NOT NULL,
    component_product_id BIGINT,
    component_name VARCHAR(255) NOT NULL,
    quantity DECIMAL(19,4) NOT NULL,
    unit VARCHAR(50),
    sort_order INTEGER DEFAULT 0,
    CONSTRAINT fk_component_bom FOREIGN KEY (bom_id) REFERENCES boms(id) ON DELETE CASCADE,
    CONSTRAINT fk_component_product FOREIGN KEY (component_product_id) REFERENCES products(id)
);

CREATE INDEX IF NOT EXISTS idx_products_category ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);
CREATE INDEX IF NOT EXISTS idx_products_code ON products(product_code);
CREATE INDEX IF NOT EXISTS idx_product_skus_product ON product_skus(product_id);
CREATE INDEX IF NOT EXISTS idx_boms_product ON boms(product_id);
