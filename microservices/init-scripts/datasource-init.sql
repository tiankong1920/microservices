-- =============================================
-- 数据源管理系统数据库初始化脚本
-- 版本: 1.0.0
-- 创建时间: 2024-01-01
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS datasource_management 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE datasource_management;

-- =============================================
-- 1. 数据源配置表
-- =============================================
CREATE TABLE IF NOT EXISTS datasource_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    name VARCHAR(128) NOT NULL COMMENT '数据源名称',
    type VARCHAR(32) NOT NULL COMMENT '数据源类型: MYSQL/POSTGRESQL/ELASTICSEARCH/KUDU',
    version VARCHAR(32) COMMENT '版本号',
    host VARCHAR(255) NOT NULL COMMENT '主机地址',
    port INT NOT NULL COMMENT '端口',
    database_name VARCHAR(128) COMMENT '数据库名',
    username VARCHAR(128) COMMENT '用户名',
    password TEXT NOT NULL COMMENT '加密后的密码',
    extra_config JSON COMMENT '扩展配置(JSON格式)',
    status VARCHAR(16) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/INACTIVE/DELETED/ERROR',
    template_id BIGINT COMMENT '模板ID',
    created_by VARCHAR(64) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(64) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据源配置表';

-- =============================================
-- 2. 连接状态表
-- =============================================
CREATE TABLE IF NOT EXISTS connection_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    datasource_id BIGINT NOT NULL COMMENT '数据源ID',
    status VARCHAR(16) NOT NULL COMMENT '状态: CONNECTED/DISCONNECTED/ERROR/TESTING',
    response_time INT COMMENT '响应时间(毫秒)',
    error_message TEXT COMMENT '错误信息',
    checked_at DATETIME NOT NULL COMMENT '检测时间',
    INDEX idx_cs_datasource_id (datasource_id),
    INDEX idx_cs_checked_at (checked_at),
    INDEX idx_cs_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='连接状态表';

-- =============================================
-- 3. 连接测试日志表
-- =============================================
CREATE TABLE IF NOT EXISTS connection_test_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    datasource_id BIGINT NOT NULL COMMENT '数据源ID',
    test_type VARCHAR(16) NOT NULL COMMENT '测试类型: AUTO/MANUAL/BATCH/SCHEDULED',
    result VARCHAR(16) NOT NULL COMMENT '结果: SUCCESS/FAILURE/TIMEOUT',
    response_time INT COMMENT '响应时间(毫秒)',
    error_code VARCHAR(32) COMMENT '错误码',
    error_message TEXT COMMENT '错误详情',
    suggestions TEXT COMMENT '解决方案建议',
    tested_by VARCHAR(64) COMMENT '测试人',
    tested_at DATETIME NOT NULL COMMENT '测试时间',
    INDEX idx_ctl_datasource_id (datasource_id),
    INDEX idx_ctl_tested_at (tested_at),
    INDEX idx_ctl_result (result),
    INDEX idx_ctl_test_type (test_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='连接测试日志表';

-- =============================================
-- 4. 配置模板表
-- =============================================
CREATE TABLE IF NOT EXISTS config_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    name VARCHAR(128) NOT NULL COMMENT '模板名称',
    type VARCHAR(32) NOT NULL COMMENT '数据源类型',
    description TEXT COMMENT '模板描述',
    config_json JSON NOT NULL COMMENT '配置参数(JSON)',
    is_public TINYINT DEFAULT 0 COMMENT '是否公开: 0-否 1-是',
    created_by VARCHAR(64) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_ct_tenant_id (tenant_id),
    INDEX idx_ct_type (type),
    INDEX idx_ct_is_public (is_public)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配置模板表';

-- =============================================
-- 5. 审计日志表
-- =============================================
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    username VARCHAR(128) COMMENT '用户名',
    operation VARCHAR(32) NOT NULL COMMENT '操作类型',
    resource_type VARCHAR(32) NOT NULL COMMENT '资源类型',
    resource_id VARCHAR(64) COMMENT '资源ID',
    old_value JSON COMMENT '变更前值',
    new_value JSON COMMENT '变更后值',
    ip_address VARCHAR(64) COMMENT 'IP地址',
    user_agent VARCHAR(512) COMMENT '用户代理',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_al_tenant_id (tenant_id),
    INDEX idx_al_user_id (user_id),
    INDEX idx_al_created_at (created_at),
    INDEX idx_al_operation (operation),
    INDEX idx_al_resource_type (resource_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

-- =============================================
-- 6. 告警配置表
-- =============================================
CREATE TABLE IF NOT EXISTS alert_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    name VARCHAR(128) NOT NULL COMMENT '告警名称',
    datasource_ids JSON COMMENT '关联的数据源ID列表',
    alert_level VARCHAR(16) NOT NULL COMMENT '告警级别: CRITICAL/WARNING/INFO',
    alert_channels JSON NOT NULL COMMENT '告警渠道: ["EMAIL","SMS","DINGTALK","WECHAT"]',
    receivers JSON NOT NULL COMMENT '接收人配置',
    notify_frequency VARCHAR(16) DEFAULT 'IMMEDIATE' COMMENT '通知频率: IMMEDIATE/HOURLY/DAILY',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_ac_tenant_id (tenant_id),
    INDEX idx_ac_enabled (enabled),
    INDEX idx_ac_alert_level (alert_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警配置表';

-- =============================================
-- 7. 告警历史表
-- =============================================
CREATE TABLE IF NOT EXISTS alert_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    datasource_id BIGINT NOT NULL COMMENT '数据源ID',
    alert_config_id BIGINT COMMENT '告警配置ID',
    alert_level VARCHAR(16) NOT NULL COMMENT '告警级别',
    channel VARCHAR(32) NOT NULL COMMENT '告警渠道',
    message TEXT NOT NULL COMMENT '告警消息',
    status VARCHAR(16) DEFAULT 'PENDING' COMMENT '状态: PENDING/SENT/FAILED',
    sent_at DATETIME COMMENT '发送时间',
    error_message TEXT COMMENT '错误信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_ah_tenant_id (tenant_id),
    INDEX idx_ah_datasource_id (datasource_id),
    INDEX idx_ah_created_at (created_at),
    INDEX idx_ah_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警历史表';

-- =============================================
-- 8. 插入默认配置模板
-- =============================================
INSERT INTO config_template (tenant_id, name, type, description, config_json, is_public, created_by) VALUES
('default', 'MySQL默认模板', 'MYSQL', 'MySQL数据库默认配置模板', 
 '{"port": 3306, "charset": "utf8mb4", "timezone": "Asia/Shanghai", "useSSL": false}', 1, 'system'),

('default', 'PostgreSQL默认模板', 'POSTGRESQL', 'PostgreSQL数据库默认配置模板', 
 '{"port": 5432, "schema": "public", "sslMode": "disable"}', 1, 'system'),

('default', 'Elasticsearch默认模板', 'ELASTICSEARCH', 'Elasticsearch默认配置模板', 
 '{"port": 9200, "useSSL": false, "verifySSL": true}', 1, 'system'),

('default', 'Kudu默认模板', 'KUDU', 'Kudu默认配置模板', 
 '{"port": 7051, "operationTimeout": 30000, "connectionTimeout": 3000}', 1, 'system');

-- =============================================
-- 9. 插入默认告警配置
-- =============================================
INSERT INTO alert_config (tenant_id, name, datasource_ids, alert_level, alert_channels, receivers, notify_frequency, enabled) VALUES
('default', '默认连接失败告警', NULL, 'WARNING', 
 '["EMAIL"]', 
 '{"emails": ["admin@example.com"], "phones": [], "dingtalkWebhooks": [], "wechatWebhooks": []}', 
 'IMMEDIATE', 1);

-- =============================================
-- 10. 创建定时任务清理历史数据
-- =============================================
CREATE EVENT IF NOT EXISTS cleanup_old_connection_status
ON SCHEDULE EVERY 1 DAY
DO DELETE FROM connection_status WHERE checked_at < DATE_SUB(NOW(), INTERVAL 30 DAY);

CREATE EVENT IF NOT EXISTS cleanup_old_test_logs
ON SCHEDULE EVERY 1 DAY
DO DELETE FROM connection_test_log WHERE tested_at < DATE_SUB(NOW(), INTERVAL 90 DAY);

CREATE EVENT IF NOT EXISTS cleanup_old_alert_history
ON SCHEDULE EVERY 1 DAY
DO DELETE FROM alert_history WHERE created_at < DATE_SUB(NOW(), INTERVAL 180 DAY);

-- =============================================
-- 完成
-- =============================================
SELECT '数据源管理系统数据库初始化完成!' AS message;
