-- Admin Service Schema
-- Initial migration for admin service database

CREATE TABLE IF NOT EXISTS admin_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(50),
    full_name VARCHAR(200),
    department VARCHAR(200),
    position VARCHAR(200),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admin_role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admin_user_role (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT fk_admin_user_role_user FOREIGN KEY (user_id) REFERENCES admin_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_admin_user_role_role FOREIGN KEY (role_id) REFERENCES admin_role(id) ON DELETE CASCADE,
    CONSTRAINT uk_admin_user_role UNIQUE (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS admin_permission (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    resource VARCHAR(200) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS admin_role_permission (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    CONSTRAINT fk_admin_rp_role FOREIGN KEY (role_id) REFERENCES admin_role(id) ON DELETE CASCADE,
    CONSTRAINT fk_admin_rp_perm FOREIGN KEY (permission_id) REFERENCES admin_permission(id) ON DELETE CASCADE,
    CONSTRAINT uk_admin_role_permission UNIQUE (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS password_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_password_history_user FOREIGN KEY (user_id) REFERENCES admin_user(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS backup_record (
    id BIGSERIAL PRIMARY KEY,
    backup_type VARCHAR(50) NOT NULL,
    file_path VARCHAR(500),
    file_size BIGINT,
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_admin_user_username ON admin_user(username);
CREATE INDEX IF NOT EXISTS idx_password_history_user ON password_history(user_id);
