-- Security Schema Initialization Script
-- Creates tables for RBAC (Role-Based Access Control) system

-- Create security audit log table
CREATE TABLE IF NOT EXISTS security_audit_log (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    username VARCHAR(100),
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    description VARCHAR(1000),
    additional_info TEXT,
    timestamp TIMESTAMP NOT NULL,
    success BOOLEAN,
    session_id VARCHAR(100),
    request_id VARCHAR(100)
);

-- Create indexes for audit log
CREATE INDEX IF NOT EXISTS idx_audit_username ON security_audit_log(username);
CREATE INDEX IF NOT EXISTS idx_audit_event_type ON security_audit_log(event_type);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON security_audit_log(timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_ip_address ON security_audit_log(ip_address);

-- Create remember-me token table
CREATE TABLE IF NOT EXISTS persistent_logins (
    username VARCHAR(100) NOT NULL,
    series VARCHAR(64) PRIMARY KEY,
    token VARCHAR(64) NOT NULL,
    last_used TIMESTAMP NOT NULL
);

-- Create password history table
CREATE TABLE IF NOT EXISTS password_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_password_history_user FOREIGN KEY (user_id) REFERENCES oauth2_users(id)
);

-- Create index for password history
CREATE INDEX IF NOT EXISTS idx_password_history_user ON password_history(user_id);

-- Insert default permissions
INSERT INTO oauth2_permissions (permission_name, resource, action, description) VALUES
-- User Management Permissions
('USER_CREATE', '/api/users', 'CREATE', 'Create new users'),
('USER_READ', '/api/users', 'READ', 'View user information'),
('USER_UPDATE', '/api/users', 'UPDATE', 'Update user information'),
('USER_DELETE', '/api/users', 'DELETE', 'Delete users'),
('USER_LIST', '/api/users', 'LIST', 'List all users'),

-- Role Management Permissions
('ROLE_CREATE', '/api/roles', 'CREATE', 'Create new roles'),
('ROLE_READ', '/api/roles', 'READ', 'View role information'),
('ROLE_UPDATE', '/api/roles', 'UPDATE', 'Update role information'),
('ROLE_DELETE', '/api/roles', 'DELETE', 'Delete roles'),
('ROLE_LIST', '/api/roles', 'LIST', 'List all roles'),

-- Permission Management Permissions
('PERMISSION_CREATE', '/api/permissions', 'CREATE', 'Create new permissions'),
('PERMISSION_READ', '/api/permissions', 'READ', 'View permission information'),
('PERMISSION_UPDATE', '/api/permissions', 'UPDATE', 'Update permission information'),
('PERMISSION_DELETE', '/api/permissions', 'DELETE', 'Delete permissions'),
('PERMISSION_LIST', '/api/permissions', 'LIST', 'List all permissions'),

-- Inventory Management Permissions
('INVENTORY_CREATE', '/api/inventory', 'CREATE', 'Create inventory items'),
('INVENTORY_READ', '/api/inventory', 'READ', 'View inventory items'),
('INVENTORY_UPDATE', '/api/inventory', 'UPDATE', 'Update inventory items'),
('INVENTORY_DELETE', '/api/inventory', 'DELETE', 'Delete inventory items'),
('INVENTORY_LIST', '/api/inventory', 'LIST', 'List all inventory items'),

-- Order Management Permissions
('ORDER_CREATE', '/api/orders', 'CREATE', 'Create orders'),
('ORDER_READ', '/api/orders', 'READ', 'View orders'),
('ORDER_UPDATE', '/api/orders', 'UPDATE', 'Update orders'),
('ORDER_DELETE', '/api/orders', 'DELETE', 'Delete orders'),
('ORDER_LIST', '/api/orders', 'LIST', 'List all orders'),
('ORDER_APPROVE', '/api/orders', 'APPROVE', 'Approve orders'),

-- Product Management Permissions
('PRODUCT_CREATE', '/api/products', 'CREATE', 'Create products'),
('PRODUCT_READ', '/api/products', 'READ', 'View products'),
('PRODUCT_UPDATE', '/api/products', 'UPDATE', 'Update products'),
('PRODUCT_DELETE', '/api/products', 'DELETE', 'Delete products'),
('PRODUCT_LIST', '/api/products', 'LIST', 'List all products'),

-- Sales Management Permissions
('SALES_CREATE', '/api/sales', 'CREATE', 'Create sales records'),
('SALES_READ', '/api/sales', 'READ', 'View sales records'),
('SALES_UPDATE', '/api/sales', 'UPDATE', 'Update sales records'),
('SALES_DELETE', '/api/sales', 'DELETE', 'Delete sales records'),
('SALES_LIST', '/api/sales', 'LIST', 'List all sales records'),

-- Report Permissions
('REPORT_VIEW', '/api/reports', 'READ', 'View reports'),
('REPORT_EXPORT', '/api/reports', 'EXPORT', 'Export reports'),
('REPORT_GENERATE', '/api/reports', 'CREATE', 'Generate reports'),

-- System Administration Permissions
('SYSTEM_CONFIG', '/api/admin/config', 'MANAGE', 'Manage system configuration'),
('SYSTEM_MONITOR', '/api/admin/monitor', 'READ', 'Monitor system status'),
('SYSTEM_BACKUP', '/api/admin/backup', 'MANAGE', 'Manage system backups'),
('SYSTEM_AUDIT', '/api/admin/audit', 'READ', 'View audit logs'),

-- Customer Management Permissions
('CUSTOMER_CREATE', '/api/customers', 'CREATE', 'Create customers'),
('CUSTOMER_READ', '/api/customers', 'READ', 'View customers'),
('CUSTOMER_UPDATE', '/api/customers', 'UPDATE', 'Update customers'),
('CUSTOMER_DELETE', '/api/customers', 'DELETE', 'Delete customers'),
('CUSTOMER_LIST', '/api/customers', 'LIST', 'List all customers'),

-- Supplier Management Permissions
('SUPPLIER_CREATE', '/api/suppliers', 'CREATE', 'Create suppliers'),
('SUPPLIER_READ', '/api/suppliers', 'READ', 'View suppliers'),
('SUPPLIER_UPDATE', '/api/suppliers', 'UPDATE', 'Update suppliers'),
('SUPPLIER_DELETE', '/api/suppliers', 'DELETE', 'Delete suppliers'),
('SUPPLIER_LIST', '/api/suppliers', 'LIST', 'List all suppliers')
ON CONFLICT (permission_name) DO NOTHING;

-- Insert default roles
INSERT INTO oauth2_roles (role_name, description) VALUES
('ADMIN', 'System Administrator with full access'),
('MANAGER', 'Manager with limited administrative access'),
('OPERATOR', 'Operator with operational access'),
('USER', 'Regular user with basic access'),
('AUDITOR', 'Auditor with read-only access to audit logs')
ON CONFLICT (role_name) DO NOTHING;

-- Assign permissions to ADMIN role
INSERT INTO oauth2_role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM oauth2_roles r, oauth2_permissions p
WHERE r.role_name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- Assign permissions to MANAGER role
INSERT INTO oauth2_role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM oauth2_roles r, oauth2_permissions p
WHERE r.role_name = 'MANAGER'
AND p.permission_name IN (
    'USER_READ', 'USER_LIST',
    'INVENTORY_CREATE', 'INVENTORY_READ', 'INVENTORY_UPDATE', 'INVENTORY_LIST',
    'ORDER_CREATE', 'ORDER_READ', 'ORDER_UPDATE', 'ORDER_LIST', 'ORDER_APPROVE',
    'PRODUCT_CREATE', 'PRODUCT_READ', 'PRODUCT_UPDATE', 'PRODUCT_LIST',
    'SALES_CREATE', 'SALES_READ', 'SALES_UPDATE', 'SALES_LIST',
    'CUSTOMER_CREATE', 'CUSTOMER_READ', 'CUSTOMER_UPDATE', 'CUSTOMER_LIST',
    'SUPPLIER_CREATE', 'SUPPLIER_READ', 'SUPPLIER_UPDATE', 'SUPPLIER_LIST',
    'REPORT_VIEW', 'REPORT_EXPORT', 'REPORT_GENERATE'
)
ON CONFLICT DO NOTHING;

-- Assign permissions to OPERATOR role
INSERT INTO oauth2_role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM oauth2_roles r, oauth2_permissions p
WHERE r.role_name = 'OPERATOR'
AND p.permission_name IN (
    'INVENTORY_READ', 'INVENTORY_UPDATE', 'INVENTORY_LIST',
    'ORDER_CREATE', 'ORDER_READ', 'ORDER_UPDATE', 'ORDER_LIST',
    'PRODUCT_READ', 'PRODUCT_LIST',
    'SALES_CREATE', 'SALES_READ', 'SALES_LIST',
    'CUSTOMER_READ', 'CUSTOMER_LIST',
    'SUPPLIER_READ', 'SUPPLIER_LIST',
    'REPORT_VIEW'
)
ON CONFLICT DO NOTHING;

-- Assign permissions to USER role
INSERT INTO oauth2_role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM oauth2_roles r, oauth2_permissions p
WHERE r.role_name = 'USER'
AND p.permission_name IN (
    'INVENTORY_READ', 'INVENTORY_LIST',
    'ORDER_READ', 'ORDER_LIST',
    'PRODUCT_READ', 'PRODUCT_LIST',
    'SALES_READ', 'SALES_LIST',
    'CUSTOMER_READ', 'CUSTOMER_LIST',
    'REPORT_VIEW'
)
ON CONFLICT DO NOTHING;

-- Assign permissions to AUDITOR role
INSERT INTO oauth2_role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM oauth2_roles r, oauth2_permissions p
WHERE r.role_name = 'AUDITOR'
AND p.permission_name IN (
    'USER_READ', 'USER_LIST',
    'ROLE_READ', 'ROLE_LIST',
    'PERMISSION_READ', 'PERMISSION_LIST',
    'SYSTEM_AUDIT', 'SYSTEM_MONITOR',
    'REPORT_VIEW'
)
ON CONFLICT DO NOTHING;

-- Create default admin user (password: Admin@123)
-- BCrypt hash with strength 12
INSERT INTO oauth2_users (username, password, email, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at)
VALUES ('admin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.qVh.N9NJ.KQXCK', 'admin@inventory.com', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- Assign ADMIN role to admin user
INSERT INTO oauth2_user_roles (user_id, role_id)
SELECT u.id, r.id FROM oauth2_users u, oauth2_roles r
WHERE u.username = 'admin' AND r.role_name = 'ADMIN'
ON CONFLICT DO NOTHING;
