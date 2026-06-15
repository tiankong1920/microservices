# 备份与恢复指南

## 版本
- 版本：1.0.0
- 创建日期：2025-12-29
- 最后更新：2025-12-29

## 1. 文档概述

### 1.1 目的

本指南提供了库存管理系统的详细备份与恢复流程，包括备份策略、备份类型、备份计划、备份存储、恢复流程、备份验证和灾难恢复演练，旨在确保数据安全和系统可恢复性。

### 1.2 适用范围

- 数据库备份与恢复
- 配置文件备份与恢复
- 日志文件备份与归档
- 代码备份与恢复
- 灾难恢复演练

### 1.3 目标读者

- 系统运维工程师
- DevOps工程师
- 系统管理员
- 数据库管理员

### 1.4 术语定义

| 术语 | 定义 |
|------|------|
| **全量备份**：备份所有数据，包括数据、结构、索引等 |
| **增量备份**：仅备份自上次备份以来发生变化的数据 |
| **差异备份**：备份自上次全量备份以来发生变化的数据 |
| **RTO（Recovery Time Objective）**：恢复时间目标，从故障发生到系统恢复的最大可接受时间 |
| **RPO（Recovery Point Objective）**：恢复点目标，可接受的最大数据丢失量 |
| **异地备份**：将备份存储到异地，避免单点故障 |
| **冷备**：系统停机状态下进行的备份 |
| **热备**：系统运行状态下进行的备份 |

## 2. 备份策略

### 2.1 备份类型

#### 2.1.1 数据库备份

| 备份类型 | 频率 | 保留期限 | 存储位置 |
|---------|------|---------|---------|
| 全量备份 | 每日 | 30天 | 本地 + 异地 |
| 增量备份 | 每小时 | 7天 | 本地 |
| 差异备份 | 每周 | 4周 | 本地 + 异地 |

#### 2.1.2 配置文件备份

| 备份类型 | 频率 | 保留期限 | 存储位置 |
|---------|------|---------|---------|
| 全量备份 | 每日 | 90天 | 本地 + 异地 |
| 变更备份 | 每次变更 | 30天 | 本地 |
| 版本备份 | 每次发布 | 永久 | Git仓库 |

#### 2.1.3 日志文件备份

| 备份类型 | 频率 | 保留期限 | 存储位置 |
|---------|------|---------|---------|
| 日志归档 | 每日 | 90天 | 本地 |
| 日志压缩 | 每周 | 1年 | 本地 + 异地 |
| 日志删除 | 每月 | 2年 | 异地 |

#### 2.1.4 代码备份

| 备份类型 | 频率 | 保留期限 | 存储位置 |
|---------|------|---------|---------|
| Git提交 | 每次提交 | 永久 | Git仓库 |
| 代码快照 | 每次发布 | 永久 | Git仓库 |
| 代码归档 | 每月 | 1年 | 本地 + 异地 |

### 2.2 备份计划

#### 2.2.1 每日备份计划

```powershell
# 每日凌晨2点执行全量备份
$backupScript = @"
# 数据库全量备份
docker exec postgres-1 pg_dump -U postgres -d inventory > backup-inventory-$(Get-Date -Format "yyyyMMdd").sql

# 配置文件备份
Copy-Item -Path "config\*" -Destination "backups\config\$(Get-Date -Format "yyyyMMdd")\" -Recurse -Force

# 日志文件备份
Copy-Item -Path "logs\*.log" -Destination "backups\logs\$(Get-Date -Format "yyyyMMdd")\" -Force

# 压缩备份
Compress-Archive -Path "backups\$(Get-Date -Format "yyyyMMdd")" -DestinationPath "backups\daily-backup-$(Get-Date -Format "yyyyMMdd").zip" -Force

# 上传到异地
# 使用rsync、scp或云存储API上传备份文件
"@

$backupScript | Out-File -FilePath "scripts\daily-backup.ps1" -Encoding UTF8
```

#### 2.2.2 每周备份计划

```powershell
# 每周日凌晨3点执行差异备份
$weeklyBackupScript = @"
# 数据库差异备份
docker exec postgres-1 pg_dump -U postgres -d inventory --data-only > backup-inventory-diff-$(Get-Date -Format "yyyyMMdd").sql

# 配置文件差异备份
Copy-Item -Path "config\*" -Destination "backups\config\weekly-$(Get-Date -Format "yyyyMMdd")\" -Recurse -Force

# 压缩备份
Compress-Archive -Path "backups\weekly-$(Get-Date -Format "yyyyMMdd")" -DestinationPath "backups\weekly-backup-$(Get-Date -Format "yyyyMMdd").zip" -Force

# 上传到异地
# 使用rsync、scp或云存储API上传备份文件
"@

$weeklyBackupScript | Out-File -FilePath "scripts\weekly-backup.ps1" -Encoding UTF8
```

#### 2.2.3 每月备份计划

```powershell
# 每月1日凌晨4点执行归档备份
$monthlyBackupScript = @"
# 数据库归档备份
docker exec postgres-1 pg_dump -U postgres -d inventory > backup-inventory-archive-$(Get-Date -Format "yyyyMM").sql

# 配置文件归档备份
Copy-Item -Path "config\*" -Destination "backups\config\monthly-$(Get-Date -Format "yyyyMM")\" -Recurse -Force

# 日志文件归档备份
Copy-Item -Path "logs\archive\*" -Destination "backups\logs\monthly-$(Get-Date -Format "yyyyMM")\" -Recurse -Force

# 压缩备份
Compress-Archive -Path "backups\monthly-$(Get-Date -Format "yyyyMM")" -DestinationPath "backups\monthly-backup-$(Get-Date -Format "yyyyMM").zip" -Force

# 上传到异地
# 使用rsync、scp或云存储API上传备份文件
"@

$monthlyBackupScript | Out-File -FilePath "scripts\monthly-backup.ps1" -Encoding UTF8
```

### 2.3 备份存储

#### 2.3.1 本地存储

```powershell
# 创建本地备份目录
$localBackupDir = "E:\backups"
if (-not (Test-Path $localBackupDir)) {
    New-Item -Path $localBackupDir -ItemType Directory -Force
}

# 创建子目录
$subDirs = @("database", "config", "logs", "code")
foreach ($dir in $subDirs) {
    if (-not (Test-Path "$localBackupDir\$dir")) {
        New-Item -Path "$localBackupDir\$dir" -ItemType Directory -Force
    }
}

Write-Host "本地备份目录已创建：$localBackupDir" -ForegroundColor Green
```

#### 2.3.2 异地存储

```powershell
# 配置异地备份服务器
$remoteBackupServer = "backup-server.example.com"
$remoteBackupDir = "/backups/inventory-system"

# 使用rsync同步备份
$rsyncCommand = "rsync -avz --delete E:\backups\ $remoteBackupServer`:$remoteBackupDir"

# 或使用scp上传备份
$scpCommand = "scp -r E:\backups\* $remoteBackupServer`:$remoteBackupDir"

# 或使用云存储API上传备份
# 根据云存储提供商（AWS S3、Azure Blob、阿里云OSS）使用相应的API
```

#### 2.3.3 云存储

```powershell
# 使用AWS S3存储备份
$s3Bucket = "inventory-system-backups"
$s3Key = "backup-$(Get-Date -Format "yyyyMMdd").zip"

# 上传备份到S3
aws s3 cp backups\daily-backup-$(Get-Date -Format "yyyyMMdd").zip s3://$s3Bucket/$s3Key

# 设置生命周期策略
aws s3api put-bucket-lifecycle-configuration `
    --bucket $s3Bucket `
    --lifecycle-configuration file://lifecycle.json
```

## 3. 备份类型

### 3.1 数据库备份

#### 3.1.1 PostgreSQL全量备份

```powershell
# 全量备份
docker exec postgres-1 pg_dump -U postgres -d inventory > backup-inventory-full-$(Get-Date -Format "yyyyMMdd_HHmmss").sql

# 带压缩的全量备份
docker exec postgres-1 pg_dump -U postgres -d inventory -F c -f backup-inventory-full-$(Get-Date -Format "yyyyMMdd_HHmmss").dump

# 仅备份数据
docker exec postgres-1 pg_dump -U postgres -d inventory --data-only > backup-inventory-data-$(Get-Date -Format "yyyyMMdd_HHmmss").sql

# 仅备份结构
docker exec postgres-1 pg_dump -U postgres -d inventory --schema-only > backup-inventory-schema-$(Get-Date -Format "yyyyMMdd_HHmmss").sql
```

#### 3.1.2 PostgreSQL增量备份

```powershell
# 使用pgBackRest进行增量备份
docker exec postgres-1 pgbackrest --stanza=inventory --type=incr backup

# 查看备份信息
docker exec postgres-1 pgbackrest --stanza=inventory info

# 验证备份
docker exec postgres-1 pgbackrest --stanza=inventory check
```

#### 3.1.3 PostgreSQL差异备份

```powershell
# 差异备份
docker exec postgres-1 pg_dump -U postgres -d inventory --data-only --exclude-table-data='*_history' > backup-inventory-diff-$(Get-Date -Format "yyyyMMdd_HHmmss").sql

# 查看备份大小
Get-Item backup-inventory-diff-*.sql | Select-Object Name, Length
```

### 3.2 配置文件备份

#### 3.2.1 Nacos配置备份

```powershell
# 备份Nacos配置
curl -X GET "http://localhost:8848/nacos/v1/cs/configs?tenant=&dataId=&group=" > nacos-config-backup-$(Get-Date -Format "yyyyMMdd_HHmmss").json

# 备份Nacos命名空间
curl -X GET "http://localhost:8848/nacos/v1/console/namespaces" > nacos-namespaces-backup-$(Get-Date -Format "yyyyMMdd_HHmmss").json

# 备份Nacos服务列表
curl -X GET "http://localhost:8848/nacos/v1/ns/service/list?pageNo=1&pageSize=100" > nacos-services-backup-$(Get-Date -Format "yyyyMMdd_HHmmss").json
```

#### 3.2.2 应用配置备份

```powershell
# 备份应用配置文件
$configBackupDir = "backups\config\$(Get-Date -Format "yyyyMMdd_HHmmss")"
if (-not (Test-Path $configBackupDir)) {
    New-Item -Path $configBackupDir -ItemType Directory -Force
}

Copy-Item -Path "config\*" -Destination "$configBackupDir\" -Recurse -Force

Write-Host "配置文件已备份到：$configBackupDir" -ForegroundColor Green
```

### 3.3 日志文件备份

#### 3.3.1 日志归档

```powershell
# 归档30天前的日志
$cutoffDate = (Get-Date).AddDays(-30)
$archiveDir = "logs\archive"

if (-not (Test-Path $archiveDir)) {
    New-Item -Path $archiveDir -ItemType Directory -Force
}

Get-ChildItem -Path "logs" -Filter "*.log" | Where-Object { $_.LastWriteTime -lt $cutoffDate } | ForEach-Object {
    $archiveFile = "$archiveDir\$($_.BaseName)_$($_.LastWriteTime.ToString('yyyyMMdd')).log"
    Move-Item -Path $_.FullName -Destination $archiveFile
    Write-Host "已归档：$($_.Name）" -ForegroundColor Green
}
```

#### 3.3.2 日志压缩

```powershell
# 压缩归档日志
Get-ChildItem -Path "logs\archive" -Filter "*.log" | ForEach-Object {
    $zipFile = "$($_.FullName).zip"
    Compress-Archive -Path $_.FullName -DestinationPath $zipFile -Force
    Remove-Item -Path $_.FullName
    Write-Host "已压缩：$($_.Name）" -ForegroundColor Green
}
```

### 3.4 代码备份

#### 3.4.1 Git提交

```powershell
# 提交代码到Git仓库
git add .
git commit -m "Backup - $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")"
git push origin main

Write-Host "代码已提交到Git仓库" -ForegroundColor Green
```

#### 3.4.2 代码快照

```powershell
# 创建代码快照
$snapshotDir = "backups\code\snapshot-$(Get-Date -Format "yyyyMMdd_HHmmss")"
if (-not (Test-Path $snapshotDir)) {
    New-Item -Path $snapshotDir -ItemType Directory -Force
}

Copy-Item -Path ".\*" -Destination "$snapshotDir\" -Recurse -Force -Exclude @(".git", "target", "node_modules")

Write-Host "代码快照已创建：$snapshotDir" -ForegroundColor Green
```

## 4. 恢复流程

### 4.1 数据库恢复

#### 4.1.1 全量恢复

```powershell
# 恢复数据库
cat backup-inventory-full-20251229.sql | docker exec -i postgres-1 psql -U postgres -d inventory

# 恢复到新数据库
cat backup-inventory-full-20251229.sql | docker exec -i postgres-1 psql -U postgres -d inventory_new

# 验证恢复
docker exec postgres-1 psql -U postgres -d inventory -c "SELECT COUNT(*) FROM orders;"
```

#### 4.1.2 增量恢复

```powershell
# 恢复基础备份
docker exec postgres-1 pgbackrest --stanza=inventory --delta restore

# 恢复增量备份
docker exec postgres-1 pgbackrest --stanza=inventory --type=incr restore

# 验证恢复
docker exec postgres-1 pgbackrest --stanza=inventory check
```

#### 4.1.3 差异恢复

```powershell
# 恢复全量备份
cat backup-inventory-full-20251228.sql | docker exec -i postgres-1 psql -U postgres -d inventory

# 恢复差异备份
cat backup-inventory-diff-20251229.sql | docker exec -i postgres-1 psql -U postgres -d inventory

# 验证恢复
docker exec postgres-1 psql -U postgres -d inventory -c "SELECT COUNT(*) FROM orders;"
```

### 4.2 配置文件恢复

#### 4.2.1 Nacos配置恢复

```powershell
# 恢复Nacos配置
$nacosConfig = Get-Content "nacos-config-backup-20251229.json" -Raw | ConvertFrom-Json

foreach ($config in $nacosConfig) {
    curl -X POST "http://localhost:8848/nacos/v1/cs/configs" `
        -d "dataId=$($config.dataId）&group=$($config.group）&content=$($config.content）"
    
    Write-Host "已恢复配置：$($config.dataId）" -ForegroundColor Green
}
```

#### 4.2.2 应用配置恢复

```powershell
# 恢复应用配置文件
$backupPath = "backups\config\backup_20251229_120000"
Copy-Item -Path "$backupPath\*" -Destination "config\" -Recurse -Force

Write-Host "配置文件已从备份恢复：$backupPath" -ForegroundColor Green

# 重启服务
docker restart inventory-service
```

### 4.3 服务恢复

#### 4.3.1 单个服务恢复

```powershell
# 恢复单个服务
$backupPath = "backups\services\inventory-service-backup-20251229.jar"
Copy-Item -Path $backupPath -Destination "deploy\inventory-service.jar" -Force

# 重启服务
docker restart inventory-service

# 验证服务
curl http://localhost:8080/actuator/health
```

#### 4.3.2 所有服务恢复

```powershell
# 恢复所有服务
$backupPath = "backups\services\backup_20251229_120000"
Get-ChildItem -Path $backupPath -Filter "*.jar" | ForEach-Object {
    Copy-Item -Path $_.FullName -Destination "deploy\$($_.Name）" -Force
    Write-Host "已恢复：$($_.Name）" -ForegroundColor Green
}

# 重启所有服务
docker-compose restart

# 验证服务
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

## 5. 备份验证

### 5.1 完整性检查

#### 5.1.1 数据库备份完整性检查

```powershell
# 检查备份文件大小
$backupFile = "backup-inventory-full-20251229.sql"
$fileSize = (Get-Item $backupFile).Length

if ($fileSize -gt 1MB) {
    Write-Host "✅ 备份文件大小正常：$fileSize bytes" -ForegroundColor Green
} else {
    Write-Host "❌ 备份文件大小异常：$fileSize bytes" -ForegroundColor Red
}

# 检查备份文件内容
$backupContent = Get-Content $backupFile
if ($backupContent -match "CREATE TABLE") {
    Write-Host "✅ 备份文件内容正常" -ForegroundColor Green
} else {
    Write-Host "❌ 备份文件内容异常" -ForegroundColor Red
}
```

#### 5.1.2 配置文件完整性检查

```powershell
# 验证YAML格式
try {
    $config = Get-Content "config/application.yml" -Raw | ConvertFrom-Yaml
    Write-Host "✅ YAML格式有效" -ForegroundColor Green
} catch {
    Write-Host "❌ YAML格式无效：$($_.Exception.Message）" -ForegroundColor Red
}

# 检查必需配置项
$requiredConfigs = @("spring.application.name", "spring.profiles.active", "server.port")
$missingConfigs = @()
foreach ($configKey in $requiredConfigs) {
    if (-not $config.ContainsKey($configKey)) {
        $missingConfigs += $configKey
    }
}

if ($missingConfigs.Count -gt 0) {
    Write-Host "❌ 缺失配置项：$($missingConfigs -join ', '）" -ForegroundColor Red
} else {
    Write-Host "✅ 所有必需配置项都存在" -ForegroundColor Green
}
```

### 5.2 恢复测试

#### 5.2.1 数据库恢复测试

```powershell
# 创建测试数据库
docker exec postgres-1 psql -U postgres -c "CREATE DATABASE inventory_test;"

# 恢复到测试数据库
cat backup-inventory-full-20251229.sql | docker exec -i postgres-1 psql -U postgres -d inventory_test

# 验证数据
$result = docker exec postgres-1 psql -U postgres -d inventory_test -c "SELECT COUNT(*) FROM orders;" -t

if ($result -gt 0) {
    Write-Host "✅ 数据库恢复测试成功：$result 条记录" -ForegroundColor Green
} else {
    Write-Host "❌ 数据库恢复测试失败" -ForegroundColor Red
}

# 删除测试数据库
docker exec postgres-1 psql -U postgres -c "DROP DATABASE inventory_test;"
```

#### 5.2.2 配置文件恢复测试

```powershell
# 备份当前配置
Copy-Item -Path "config\application.yml" -Destination "config\application.yml.backup" -Force

# 恢复备份配置
Copy-Item -Path "backups\config\backup_20251229_120000\application.yml" -Destination "config\application.yml" -Force

# 验证配置
try {
    $config = Get-Content "config\application.yml" -Raw | ConvertFrom-Yaml
    Write-Host "✅ 配置文件恢复测试成功" -ForegroundColor Green
} catch {
    Write-Host "❌ 配置文件恢复测试失败：$($_.Exception.Message）" -ForegroundColor Red
    # 恢复原配置
    Copy-Item -Path "config\application.yml.backup" -Destination "config\application.yml" -Force
}
```

## 6. 灾难恢复演练

### 6.1 演练计划

#### 6.1.1 演练目标

- 验证备份的完整性和可恢复性
- 测试恢复流程的有效性
- 评估恢复时间（RTO）
- 评估数据丢失量（RPO）
- 识别恢复流程中的问题
- 改进恢复流程

#### 6.1.2 演练频率

| 演练类型 | 频率 | 演练时间 |
|---------|------|---------|
| 数据库恢复演练 | 每季度 | 非工作时间 |
| 配置恢复演练 | 每半年 | 非工作时间 |
| 完整系统恢复演练 | 每年 | 非工作时间 |

### 6.2 演练执行

#### 6.2.1 数据库恢复演练

```powershell
# 步骤1：创建演练环境
docker exec postgres-1 psql -U postgres -c "CREATE DATABASE inventory_dr;"

# 步骤2：恢复备份到演练环境
cat backup-inventory-full-20251229.sql | docker exec -i postgres-1 psql -U postgres -d inventory_dr

# 步骤3：验证数据
$result = docker exec postgres-1 psql -U postgres -d inventory_dr -c "SELECT COUNT(*) FROM orders;" -t
Write-Host "演练环境数据量：$result 条记录" -ForegroundColor Green

# 步骤4：清理演练环境
docker exec postgres-1 psql -U postgres -c "DROP DATABASE inventory_dr;"

Write-Host "数据库恢复演练完成" -ForegroundColor Green
```

#### 6.2.2 完整系统恢复演练

```powershell
# 步骤1：停止所有服务
docker-compose stop

# 步骤2：恢复数据库
cat backup-inventory-full-20251229.sql | docker exec -i postgres-1 psql -U postgres -d inventory

# 步骤3：恢复配置
Copy-Item -Path "backups\config\backup_20251229_120000\*" -Destination "config\" -Recurse -Force

# 步骤4：启动所有服务
docker-compose start

# 步骤5：验证服务
Start-Sleep -Seconds 60
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health

Write-Host "完整系统恢复演练完成" -ForegroundColor Green
```

### 6.3 演练评估

#### 6.3.1 演练报告

```powershell
$drReport = @"
灾难恢复演练报告
================

演练日期：$(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
演练类型：数据库恢复演练

演练目标：
- 验证备份的完整性和可恢复性
- 测试恢复流程的有效性
- 评估恢复时间（RTO）
- 评估数据丢失量（RPO）

演练过程：
1. 创建演练环境
2. 恢复备份到演练环境
3. 验证数据完整性
4. 清理演练环境

演练结果：
- 备份完整性：✅ 通过
- 恢复流程：✅ 有效
- 恢复时间（RTO）：30分钟
- 数据丢失量（RPO）：0

改进建议：
- 优化恢复流程，减少恢复时间
- 增加备份频率，减少数据丢失量
- 自动化恢复流程，减少人工干预
"@

$drReport | Out-File -FilePath "disaster-recovery-drill-report-$(Get-Date -Format 'yyyyMMdd').txt" -Encoding UTF8
```

## 7. RTO/RPO目标

### 7.1 RTO目标

| 服务类型 | RTO目标 | 说明 |
|---------|---------|------|
| 数据库 | 4小时 | 从故障发生到数据库恢复的最大时间 |
| 配置中心 | 2小时 | 从故障发生到配置中心恢复的最大时间 |
| 核心服务 | 4小时 | 从故障发生到核心服务恢复的最大时间 |
| 支持服务 | 2小时 | 从故障发生到支持服务恢复的最大时间 |
| 完整系统 | 8小时 | 从故障发生到完整系统恢复的最大时间 |

### 7.2 RPO目标

| 数据类型 | RPO目标 | 说明 |
|---------|---------|------|
| 数据库数据 | 1小时 | 可接受的最大数据丢失量 |
| 配置数据 | 4小时 | 可接受的最大配置丢失量 |
| 日志数据 | 24小时 | 可接受的最大日志丢失量 |
| 代码数据 | 1天 | 可接受的最大代码丢失量 |

## 8. 联系方式

### 8.1 技术支持

如果遇到问题，请联系：

- **技术支持邮箱**：support@example.com
- **技术支持热线**：+86-XXX-XXXX
- **在线文档**：[备份与恢复指南](https://example.com/backup-recovery-guide)

### 8.2 紧急联系

对于紧急故障，请联系：

- **紧急联系热线**：+86-XXX-XXXX（24小时）
- **紧急联系邮箱**：emergency@example.com

---

**免责声明**：本指南仅供参考，具体实施以实际环境为准。如有疑问，请联系support@example.com。