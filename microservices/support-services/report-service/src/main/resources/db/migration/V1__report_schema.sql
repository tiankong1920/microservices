-- Report Service Schema
-- Initial migration for report service database

CREATE TABLE IF NOT EXISTS report (
    id BIGSERIAL PRIMARY KEY,
    report_name VARCHAR(255) NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    format VARCHAR(20) NOT NULL DEFAULT 'PDF',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    parameters TEXT,
    file_path VARCHAR(500),
    file_size BIGINT,
    generated_at TIMESTAMP,
    generated_by VARCHAR(64),
    schedule_cron VARCHAR(100),
    is_scheduled BOOLEAN DEFAULT FALSE,
    last_scheduled_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_report_type ON report(report_type);
CREATE INDEX IF NOT EXISTS idx_report_status ON report(status);
CREATE INDEX IF NOT EXISTS idx_report_generated_by ON report(generated_by);
