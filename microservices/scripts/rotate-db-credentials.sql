-- ============================================================================
-- 数据库凭证轮换脚本 (LS-003)
-- ============================================================================
-- 用途: 生产环境数据库凭证轮换
-- 执行者: DBA团队
-- 执行时间: 维护窗口期
-- ============================================================================

-- 步骤1: 创建新的数据库用户 (替换为你需要的新用户名)
-- CREATE USER 'inventory_app_new'@'%' IDENTIFIED BY 'NEW_STRONG_PASSWORD';

-- 步骤2: 授予新用户与旧用户相同的权限
-- GRANT SELECT, INSERT, UPDATE, DELETE ON inventory.* TO 'inventory_app_new'@'%';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON inventory_saas.* TO 'inventory_app_new'@'%';
-- GRANT EXECUTE ON PROCEDURE inventory.* TO 'inventory_app_new'@'%';

-- 步骤3: 验证新用户权限
-- SHOW GRANTS FOR 'inventory_app_new'@'%';

-- 步骤4: 更新应用程序配置 (application-prod.yml)
-- 将 spring.datasource.username 和 spring.datasource.password 更新为新凭证
-- spring:
--   datasource:
--     username: inventory_app_new
--     password: NEW_STRONG_PASSWORD

-- 步骤5: 重启应用程序服务使配置生效
-- ./scripts/rolling-restart.sh

-- 步骤6: 验证应用程序连接
-- SELECT * FROM information_schema.processlist WHERE user = 'inventory_app_new';

-- 步骤7: 确认无误后删除旧用户 (谨慎操作!)
-- DROP USER 'inventory_app_old'@'%';

-- ============================================================================
-- 紧急回滚步骤 (如果新凭证不工作)
-- ============================================================================
-- 1. 将 application-prod.yml 恢复为旧凭证
-- 2. 重启应用程序
-- 3. 验证应用正常连接
-- 4. 分析问题原因后重新尝试

-- ============================================================================
-- 凭证轮换检查清单
-- ============================================================================
-- [ ] 新密码符合密码复杂度要求 (长度≥12, 包含大小写字母、数字、特殊字符)
-- [ ] 新用户权限与旧用户一致
-- [ ] Vault/配置中心已更新新凭证
-- [ ] 应用重启后数据库连接正常
-- [ ] 监控告警无异常
-- [ ] 旧用户已删除
-- ============================================================================
