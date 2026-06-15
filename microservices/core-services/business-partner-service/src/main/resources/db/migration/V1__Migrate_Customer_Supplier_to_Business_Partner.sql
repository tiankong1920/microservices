-- Migration script to move data from customers and suppliers tables to business_partners table
-- This script should be run after business-partner-service is deployed but before disabling old services

-- Create business_partners table if it doesn't exist
CREATE TABLE IF NOT EXISTS business_partners (
    partner_id BIGSERIAL PRIMARY KEY,
    partner_code VARCHAR(50) NOT NULL UNIQUE,
    partner_name VARCHAR(255) NOT NULL,
    partner_type VARCHAR(20) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(255),
    address VARCHAR(500),
    credit_rating VARCHAR(20),
    credit_limit DECIMAL(15,2),
    status VARCHAR(20) NOT NULL,
    remark TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Insert customers into business_partners table
INSERT INTO business_partners (
    partner_code,
    partner_name,
    partner_type,
    contact_person,
    phone,
    email,
    address,
    credit_rating,
    credit_limit,
    status,
    created_at,
    updated_at,
    deleted
) SELECT 
    customer_code,
    customer_name,
    'CUSTOMER' AS partner_type,
    contact_person,
    phone,
    email,
    address,
    credit_rating,
    credit_limit,
    status,
    created_at,
    updated_at,
    FALSE AS deleted
FROM customers
ON CONFLICT (partner_code) DO NOTHING;

-- Insert suppliers into business_partners table
INSERT INTO business_partners (
    partner_code,
    partner_name,
    partner_type,
    contact_person,
    phone,
    email,
    address,
    credit_rating,
    status,
    created_at,
    updated_at,
    deleted
) SELECT 
    supplier_code,
    supplier_name,
    'SUPPLIER' AS partner_type,
    contact_person,
    phone,
    email,
    address,
    credit_rating,
    status,
    created_at,
    updated_at,
    FALSE AS deleted
FROM suppliers
ON CONFLICT (partner_code) DO NOTHING;

-- Create index for better performance
CREATE INDEX IF NOT EXISTS idx_business_partners_type ON business_partners(partner_type);
CREATE INDEX IF NOT EXISTS idx_business_partners_status ON business_partners(status);
CREATE INDEX IF NOT EXISTS idx_business_partners_code ON business_partners(partner_code);

-- Verify migration
SELECT 
    partner_type,
    COUNT(*) AS count
FROM business_partners
WHERE deleted = FALSE
GROUP BY partner_type;

SELECT 'Migration completed successfully!' AS message;