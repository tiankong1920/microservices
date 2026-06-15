-- Inventory Management System Database Initialization Script
-- PostgreSQL 15+

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create schemas
CREATE SCHEMA IF NOT EXISTS inventory;
CREATE SCHEMA IF NOT EXISTS audit;

-- Set default search path
SET search_path TO inventory, public;

-- Grant permissions
GRANT ALL PRIVILEGES ON SCHEMA inventory TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA audit TO postgres;

-- Create audit table for tracking changes
CREATE TABLE IF NOT EXISTS audit.audit_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    table_name VARCHAR(100) NOT NULL,
    operation VARCHAR(10) NOT NULL,
    old_values JSONB,
    new_values JSONB,
    changed_by VARCHAR(100),
    changed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45)
);

-- Create index on audit log
CREATE INDEX IF NOT EXISTS idx_audit_log_table_name ON audit.audit_log(table_name);
CREATE INDEX IF NOT EXISTS idx_audit_log_changed_at ON audit.audit_log(changed_at);

-- Log initialization
DO $$
BEGIN
    RAISE NOTICE 'Database initialization completed successfully';
END $$;
