-- Datasource Service Schema
-- Initial migration for datasource service database

CREATE TABLE IF NOT EXISTS datasource_config (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    type VARCHAR(32) NOT NULL,
    version VARCHAR(32),
    host VARCHAR(255) NOT NULL,
    port INTEGER NOT NULL,
    database_name VARCHAR(128),
    username VARCHAR(128),
    password TEXT,
    extra_config TEXT,
    status VARCHAR(16) DEFAULT 'ACTIVE',
    template_id BIGINT,
    created_by VARCHAR(64),
    updated_by VARCHAR(64),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS datasource_operation_log (
    id BIGSERIAL PRIMARY KEY,
    datasource_id BIGINT NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
    operator VARCHAR(64),
    result VARCHAR(20),
    detail TEXT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_operation_log_datasource FOREIGN KEY (datasource_id) REFERENCES datasource_config(id)
);

CREATE TABLE IF NOT EXISTS datasource_health_check (
    id BIGSERIAL PRIMARY KEY,
    datasource_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    response_time BIGINT,
    error_message TEXT,
    checked_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_health_check_datasource FOREIGN KEY (datasource_id) REFERENCES datasource_config(id)
);

CREATE TABLE IF NOT EXISTS datasource_query_history (
    id BIGSERIAL PRIMARY KEY,
    datasource_id BIGINT NOT NULL,
    query_text TEXT NOT NULL,
    query_type VARCHAR(50),
    execution_time BIGINT,
    row_count BIGINT,
    status VARCHAR(20),
    error_message TEXT,
    executed_by VARCHAR(64),
    executed_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_query_history_datasource FOREIGN KEY (datasource_id) REFERENCES datasource_config(id)
);

CREATE TABLE IF NOT EXISTS datasource_metadata (
    id BIGSERIAL PRIMARY KEY,
    datasource_id BIGINT NOT NULL,
    metadata_type VARCHAR(50) NOT NULL,
    metadata_key VARCHAR(255) NOT NULL,
    metadata_value TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_metadata_datasource FOREIGN KEY (datasource_id) REFERENCES datasource_config(id)
);

CREATE TABLE IF NOT EXISTS datasource_permission (
    id BIGSERIAL PRIMARY KEY,
    datasource_id BIGINT NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    permission_type VARCHAR(50) NOT NULL,
    granted_by VARCHAR(64),
    granted_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_permission_datasource FOREIGN KEY (datasource_id) REFERENCES datasource_config(id)
);

CREATE TABLE IF NOT EXISTS datasource_sync_task (
    id BIGSERIAL PRIMARY KEY,
    datasource_id BIGINT NOT NULL,
    task_type VARCHAR(50) NOT NULL,
    cron_expression VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    last_executed_at TIMESTAMP,
    next_execution_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_sync_task_datasource FOREIGN KEY (datasource_id) REFERENCES datasource_config(id)
);

CREATE TABLE IF NOT EXISTS datasource_backup (
    id BIGSERIAL PRIMARY KEY,
    datasource_id BIGINT NOT NULL,
    backup_type VARCHAR(50) NOT NULL,
    file_path VARCHAR(500),
    file_size BIGINT,
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_backup_datasource FOREIGN KEY (datasource_id) REFERENCES datasource_config(id)
);

CREATE TABLE IF NOT EXISTS datasource_monitor (
    id BIGSERIAL PRIMARY KEY,
    datasource_id BIGINT NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(19,4),
    unit VARCHAR(20),
    recorded_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_monitor_datasource FOREIGN KEY (datasource_id) REFERENCES datasource_config(id)
);

CREATE INDEX IF NOT EXISTS idx_datasource_tenant ON datasource_config(tenant_id);
CREATE INDEX IF NOT EXISTS idx_datasource_type ON datasource_config(type);
CREATE INDEX IF NOT EXISTS idx_datasource_status ON datasource_config(status);
