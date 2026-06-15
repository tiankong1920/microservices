-- V3: Add Business Partner Module
-- Version: 3.0.0
-- Description: Add business partner management tables

BEGIN;

CREATE TABLE IF NOT EXISTS business_partners (
    id BIGSERIAL PRIMARY KEY,
    partner_code VARCHAR(50) UNIQUE NOT NULL,
    partner_type VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    address TEXT,
    city VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    tax_id VARCHAR(50),
    business_license VARCHAR(100),
    credit_rating VARCHAR(10),
    credit_limit DECIMAL(12, 2) DEFAULT 0,
    payment_terms VARCHAR(50) DEFAULT 'NET30',
    bank_name VARCHAR(100),
    bank_account VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    tags TEXT[],
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bp_code ON business_partners(partner_code);
CREATE INDEX idx_bp_type ON business_partners(partner_type);
CREATE INDEX idx_bp_city ON business_partners(city);
CREATE INDEX idx_bp_country ON business_partners(country);
CREATE INDEX idx_bp_active ON business_partners(is_active);

CREATE TABLE IF NOT EXISTS partner_contacts (
    id BIGSERIAL PRIMARY KEY,
    partner_id BIGINT NOT NULL REFERENCES business_partners(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    department VARCHAR(100),
    position VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(50),
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_partner_contacts_partner ON partner_contacts(partner_id);

CREATE TABLE IF NOT EXISTS partner_relationships (
    id BIGSERIAL PRIMARY KEY,
    partner_id BIGINT NOT NULL REFERENCES business_partners(id) ON DELETE CASCADE,
    related_partner_id BIGINT NOT NULL,
    relationship_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(partner_id, related_partner_id, relationship_type)
);

CREATE INDEX idx_partner_rel_partner ON partner_relationships(partner_id);
CREATE INDEX idx_partner_rel_related ON partner_relationships(related_partner_id);

CREATE TABLE IF NOT EXISTS partner_activities (
    id BIGSERIAL PRIMARY KEY,
    partner_id BIGINT NOT NULL REFERENCES business_partners(id) ON DELETE CASCADE,
    activity_type VARCHAR(50) NOT NULL,
    description TEXT,
    performed_by VARCHAR(255),
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_partner_activity_partner ON partner_activities(partner_id);
CREATE INDEX idx_partner_activity_type ON partner_activities(activity_type);

CREATE TABLE IF NOT EXISTS partner_ratings (
    id BIGSERIAL PRIMARY KEY,
    partner_id BIGINT NOT NULL REFERENCES business_partners(id) ON DELETE CASCADE,
    rating DECIMAL(3, 2) NOT NULL,
    max_rating DECIMAL(3, 2) DEFAULT 5.00,
    rating_type VARCHAR(50) NOT NULL,
    comments TEXT,
    rated_by VARCHAR(255),
    rated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_partner_rating_partner ON partner_ratings(partner_id);

INSERT INTO schema_migrations (version, description) VALUES ('V3__add_business_partner', 'Added business partner tables');

COMMIT;
