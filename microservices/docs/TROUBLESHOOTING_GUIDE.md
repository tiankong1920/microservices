# 故障排查指南

## 版本
- 版本：1.0.0
- 创建日期：2025-12-29
- 最后更新：2025-12-29

## 1. 文档概述

### 1.1 目的

本指南提供了库存管理系统的详细故障排查流程，包括常见问题分类、问题诊断流程、典型问题解决方案、故障排查工具使用、性能问题排查、安全问题排查和紧急故障处理流程，旨在帮助运维人员快速定位和解决问题。

### 1.2 适用范围

- 服务启动问题
- 网络连接问题
- 数据库问题
- 配置问题
- 性能问题
- 安全问题
- 紧急故障处理

### 1.3 目标读者

- 系统运维工程师
- DevOps工程师
- 系统管理员
- 技术支持人员

### 1.4 术语定义

| 术语 | 定义 |
|------|------|
| **服务不可用**：服务无法正常响应请求 |
| **响应时间过长**：服务响应时间超过预期阈值 |
| **错误率过高**：服务错误率超过预期阈值 |
| **资源耗尽**：系统资源（CPU、内存、磁盘）使用率接近100% |
| **连接超时**：客户端与服务端连接超时 |
| **死锁**：多个进程互相等待对方释放资源，导致系统停滞 |

## 2. 常见问题分类

### 2.1 服务启动问题

#### 2.1.1 问题现象

- 服务启动失败
- 服务启动后立即退出
- 服务启动超时
- 服务启动后无法访问

#### 2.1.2 可能原因

- 端口被占用
- 配置文件错误
- 依赖服务未启动
- JVM内存不足
- 类路径错误
- 权限不足

#### 2.1.3 诊断步骤

```powershell
# 步骤1：检查服务状态
docker ps -a | Select-String "inventory-service"

# 步骤2：查看服务日志
docker logs inventory-service

# 步骤3：检查端口占用
netstat -ano | Select-String "8080"

# 步骤4：检查配置文件
Get-Content "config/application.yml"

# 步骤5：检查依赖服务
docker ps | Select-String "postgres|redis|nacos"
```

#### 2.1.4 解决方案

**方案1：端口被占用**

```powershell
# 查找占用端口的进程
netstat -ano | Select-String "8080"

# 终止占用进程
Stop-Process -Id <pid> -Force

# 或修改服务端口
# 编辑 config/application.yml
# server.port: 8081
```

**方案2：配置文件错误**

```powershell
# 验证YAML格式
try {
    $config = Get-Content "config/application.yml" -Raw | ConvertFrom-Yaml
    Write-Host "✅ YAML格式有效" -ForegroundColor Green
} catch {
    Write-Host "❌ YAML格式无效：$($_.Exception.Message）" -ForegroundColor Red
    # 修复YAML格式错误
}
```

**方案3：依赖服务未启动**

```powershell
# 启动依赖服务
cd nacos-cluster
docker-compose up -d

cd ../monitoring
docker-compose up -d

# 等待依赖服务启动
Start-Sleep -Seconds 30
```

**方案4：JVM内存不足**

```powershell
# 增加JVM内存
$JAVA_OPTS = "-Xms2g -Xmx2g"

# 启动服务
java $JAVA_OPTS -jar inventory-service.jar
```

### 2.2 网络连接问题

#### 2.2.1 问题现象

- 服务间无法通信
- 客户端无法访问服务
- 连接超时
- 连接被拒绝

#### 2.2.2 可能原因

- 防火墙阻止
- 网络配置错误
- DNS解析失败
- 负载均衡器配置错误
- 网络分区

#### 2.2.3 诊断步骤

```powershell
# 步骤1：检查网络连通性
Test-NetConnection -ComputerName localhost -Port 8080

# 步骤2：检查DNS解析
Resolve-DnsName -Name localhost

# 步骤3：检查防火墙规则
Get-NetFirewallRule | Select-Object DisplayName, Direction, Action

# 步骤4：检查路由表
Get-NetRoute

# 步骤5：检查网络接口
Get-NetIPConfiguration
```

#### 2.2.4 解决方案

**方案1：防火墙阻止**

```powershell
# 添加防火墙规则
New-NetFirewallRule -DisplayName "Allow Inventory Service" `
    -Direction Inbound -LocalPort 8080 -Protocol TCP -Action Allow

# 查看防火墙规则
Get-NetFirewallRule | Select-Object DisplayName, Direction, Action
```

**方案2：DNS解析失败**

```powershell
# 检查hosts文件
Get-Content "C:\Windows\System32\drivers\etc\hosts"

# 添加DNS记录
Add-Content -Path "C:\Windows\System32\drivers\etc\hosts" -Value "127.0.0.1 inventory-service"

# 刷新DNS缓存
Clear-DnsClientCache
```

**方案3：负载均衡器配置错误**

```powershell
# 检查Nginx配置
Get-Content "nacos-cluster/nginx.conf"

# 重新加载Nginx配置
docker exec nginx nginx -s reload

# 检查Nginx状态
docker exec nginx nginx -t
```

### 2.3 数据库问题

#### 2.3.1 问题现象

- 数据库连接失败
- 查询超时
- 数据库锁等待
- 数据库死锁
- 数据库性能下降

#### 2.3.2 可能原因

- 数据库未启动
- 连接池耗尽
- 慢查询
- 索引缺失
- 表锁
- 数据库配置不当

#### 2.3.3 诊断步骤

```powershell
# 步骤1：检查数据库状态
docker ps | Select-String "postgres"

# 步骤2：检查数据库连接
docker exec postgres-1 pg_isready -U postgres

# 步骤3：查看数据库日志
docker logs postgres-1

# 步骤4：检查数据库连接数
docker exec postgres-1 psql -U postgres -c "SELECT count(*) FROM pg_stat_activity;"

# 步骤5：检查慢查询
docker exec postgres-1 psql -U postgres -c "SELECT * FROM pg_stat_statements ORDER BY total_time DESC LIMIT 10;"
```

#### 2.3.4 解决方案

**方案1：数据库连接失败**

```powershell
# 检查数据库是否启动
docker ps | Select-String "postgres"

# 启动数据库
docker start postgres-1

# 检查数据库连接
docker exec postgres-1 pg_isready -U postgres
```

**方案2：连接池耗尽**

```powershell
# 增加连接池大小
# 编辑 config/application.yml
# spring.datasource.hikari.maximum-pool-size: 20

# 重启服务
docker restart inventory-service
```

**方案3：慢查询**

```sql
-- 查看慢查询
SELECT query, calls, total_time, mean_time
FROM pg_stat_statements
ORDER BY total_time DESC
LIMIT 10;

-- 查看执行计划
EXPLAIN ANALYZE SELECT * FROM orders WHERE customer_id = 1;

-- 创建索引
CREATE INDEX idx_order_customer_id ON orders(customer_id);

-- 分析表
ANALYZE orders;
```

**方案4：数据库锁等待**

```sql
-- 查看锁等待
SELECT 
    pid,
    usename,
    pg_blocking_pids(pid) AS blocked_by,
    query AS blocked_query
FROM pg_stat_activity
WHERE cardinality(pg_blocking_pids(pid)) > 0;

-- 终止阻塞进程
SELECT pg_terminate_backend(<pid>);
```

### 2.4 配置问题

#### 2.4.1 问题现象

- 配置不生效
- 配置加载失败
- 配置冲突
- 配置格式错误

#### 2.4.2 可能原因

- 配置文件路径错误
- 配置文件格式错误
- 配置文件权限不足
- 配置缓存未清除
- 配置覆盖顺序错误

#### 2.4.3 诊断步骤

```powershell
# 步骤1：检查配置文件路径
Test-Path "config/application.yml"

# 步骤2：检查配置文件格式
Get-Content "config/application.yml" -Raw | ConvertFrom-Yaml

# 步骤3：检查配置文件权限
Get-Acl "config/application.yml"

# 步骤4：检查配置加载日志
docker logs inventory-service | Select-String "config"

# 步骤5：检查Nacos配置
curl http://localhost:8848/nacos/v1/cs/configs?dataId=application.yml&group=DEFAULT_GROUP
```

#### 2.4.4 解决方案

**方案1：配置文件路径错误**

```powershell
# 检查配置文件路径
Test-Path "config/application.yml"

# 创建配置文件
if (-not (Test-Path "config/application.yml")) {
    New-Item -Path "config/application.yml" -ItemType File -Force
}

# 复制配置文件
Copy-Item -Path "config/application-dev.yml" -Destination "config/application.yml" -Force
```

**方案2：配置文件格式错误**

```powershell
# 验证YAML格式
try {
    $config = Get-Content "config/application.yml" -Raw | ConvertFrom-Yaml
    Write-Host "✅ YAML格式有效" -ForegroundColor Green
} catch {
    Write-Host "❌ YAML格式无效：$($_.Exception.Message）" -ForegroundColor Red
    # 修复YAML格式错误
}
```

**方案3：配置缓存未清除**

```powershell
# 清除配置缓存
docker exec inventory-service rm -rf /tmp/config-cache

# 重启服务
docker restart inventory-service

# 或重新加载配置
curl -X POST "http://localhost:8848/nacos/v1/cs/configs" `
    -d "dataId=application.yml&group=DEFAULT_GROUP&content=$(Get-Content config/application.yml -Raw)"
```

## 3. 问题诊断流程

### 3.1 问题分类

根据问题严重程度，将问题分为以下几类：

| 问题级别 | 定义 | 响应时间 | 解决时间 |
|---------|------|---------|---------|
| **P1（紧急）**：系统完全不可用，影响所有用户 | 15分钟 | 4小时 |
| **P2（高）**：主要功能不可用，影响部分用户 | 1小时 | 8小时 |
| **P3（中）**：次要功能不可用，影响少数用户 | 4小时 | 24小时 |
| **P4（低）**：性能下降或小问题，不影响主要功能 | 24小时 | 72小时 |

### 3.2 诊断流程

#### 3.2.1 问题收集

```powershell
# 收集系统信息
$systemInfo = @{
    "时间" = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    "操作系统" = [System.Environment]::OSVersion.VersionString
    "CPU使用率" = (Get-Counter "\Processor(_Total)\% Processor Time").CounterSamples.CookedValue
    "内存使用率" = (Get-Counter "\Memory\Available MBytes").CounterSamples.CookedValue
    "磁盘使用率" = (Get-PSDrive C).Used / (Get-PSDrive C).Used + (Get-PSDrive C).Free
}

# 收集服务状态
$serviceStatus = docker ps -a

# 收集日志
$logs = docker logs inventory-service --tail 100

# 保存诊断信息
$diagnosticInfo = $systemInfo + $serviceStatus + $logs
$diagnosticInfo | Out-File -FilePath "diagnostic-info-$(Get-Date -Format 'yyyyMMdd_HHmmss').txt" -Encoding UTF8
```

#### 3.2.2 问题分析

```powershell
# 分析错误日志
$errors = Select-String -Path "logs/app.log" -Pattern "ERROR"

# 分析警告日志
$warnings = Select-String -Path "logs/app.log" -Pattern "WARN"

# 分析性能指标
$performance = Get-Content "logs/performance.log" | Select-Object -Last 100

# 分析访问日志
$access = Get-Content "logs/access.log" | Select-Object -Last 100

# 生成分析报告
$analysisReport = @"
问题分析报告
================

错误数量：$($errors.Count）
警告数量：$($warnings.Count）
性能指标：$performance
访问日志：$access
"@

$analysisReport | Out-File -FilePath "analysis-report-$(Get-Date -Format 'yyyyMMdd_HHmmss').txt" -Encoding UTF8
```

### 3.3 根因分析

#### 3.3.1 鱼骨图分析

使用鱼骨图（因果图）分析问题的根本原因：

```
问题：服务响应时间过长
├── 人员
│   ├── 运维人员经验不足
│   ├── 培训不到位
│   └── 人员配置不足
├── 设备
│   ├── 服务器性能不足
│   ├── 网络带宽不足
│   └── 存储性能不足
├── 环境
│   ├── 系统负载过高
│   ├── 网络延迟过高
│   └── 数据库连接池耗尽
├── 方法
│   ├── 代码性能问题
│   ├── 数据库查询优化不足
│   └── 缓存策略不当
└── 材料
    ├── 配置参数不当
    ├── 资源分配不足
    └── 依赖服务性能问题
```

#### 3.3.2 5Why分析

使用5Why方法深入分析问题根本原因：

```
问题：服务响应时间过长

Why 1：为什么服务响应时间过长？
答：数据库查询时间过长

Why 2：为什么数据库查询时间过长？
答：缺少索引

Why 3：为什么缺少索引？
答：没有进行索引优化

Why 4：为什么没有进行索引优化？
答：没有性能优化流程

Why 5：为什么没有性能优化流程？
答：缺乏性能监控和优化机制

根本原因：缺乏性能监控和优化机制
```

## 4. 典型问题解决方案

### 4.1 服务启动失败

#### 4.1.1 端口被占用

**问题**：服务启动失败，提示端口已被占用

**解决方案**：

```powershell
# 查找占用端口的进程
netstat -ano | Select-String "8080"

# 终止占用进程
Stop-Process -Id <pid> -Force

# 或修改服务端口
# 编辑 config/application.yml
# server.port: 8081
```

#### 4.1.2 配置文件错误

**问题**：服务启动失败，提示配置文件格式错误

**解决方案**：

```powershell
# 验证YAML格式
try {
    $config = Get-Content "config/application.yml" -Raw | ConvertFrom-Yaml
    Write-Host "✅ YAML格式有效" -ForegroundColor Green
} catch {
    Write-Host "❌ YAML格式无效：$($_.Exception.Message）" -ForegroundColor Red
    # 修复YAML格式错误
}
```

### 4.2 数据库连接失败

#### 4.2.1 数据库未启动

**问题**：服务无法连接到数据库

**解决方案**：

```powershell
# 检查数据库是否启动
docker ps | Select-String "postgres"

# 启动数据库
docker start postgres-1

# 检查数据库连接
docker exec postgres-1 pg_isready -U postgres
```

#### 4.2.2 连接池耗尽

**问题**：数据库连接池耗尽，服务无法获取连接

**解决方案**：

```powershell
# 增加连接池大小
# 编辑 config/application.yml
# spring.datasource.hikari.maximum-pool-size: 20

# 重启服务
docker restart inventory-service
```

### 4.3 性能问题

#### 4.3.1 响应时间过长

**问题**：服务响应时间超过预期阈值

**解决方案**：

```powershell
# 查看慢查询日志
Select-String -Path "logs/app.log" -Pattern "slow query"

# 优化慢查询
# 1. 创建索引
# 2. 优化SQL语句
# 3. 使用缓存

# 增加JVM内存
$JAVA_OPTS = "-Xms2g -Xmx2g"

# 重启服务
docker restart inventory-service
```

#### 4.3.2 内存溢出

**问题**：服务因内存溢出而崩溃

**解决方案**：

```powershell
# 增加JVM内存
$JAVA_OPTS = "-Xms2g -Xmx2g -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/logs/heapdump.hprof"

# 启动服务
java $JAVA_OPTS -jar inventory-service.jar

# 分析堆转储文件
jhat -port 7000 /logs/heapdump.hprof
```

## 5. 故障排查工具使用

### 5.1 日志分析工具

#### 5.1.1 grep

```powershell
# 查找错误日志
Select-String -Path "logs/app.log" -Pattern "ERROR"

# 查找特定错误
Select-String -Path "logs/app.log" -Pattern "NullPointerException"

# 统计错误数量
(Select-String -Path "logs/app.log" -Pattern "ERROR").Count

# 查找时间范围内的日志
Select-String -Path "logs/app.log" -Pattern "2025-12-29"
```

#### 5.1.2 tail

```powershell
# 查看实时日志
docker logs -f inventory-service

# 查看最后100行日志
docker logs --tail 100 inventory-service

# 查看最近1小时的日志
docker logs --since 1h inventory-service
```

### 5.2 性能分析工具

#### 5.2.1 jstat

```powershell
# 查看JVM内存使用
jstat -gcutil <pid> 1000

# 查看JVM堆内存
jmap -heap <pid>

# 查看JVM线程
jstack <pid>

# 查看JVM类加载
jstat -class <pid> 1000
```

#### 5.2.2 top

```powershell
# 查看进程资源使用
Get-Process | Sort-Object CPU -Descending | Select-Object -First 10

# 查看内存使用
Get-Process | Sort-Object WorkingSet -Descending | Select-Object -First 10

# 查看特定进程
Get-Process -Name java
```

### 5.3 网络诊断工具

#### 5.3.1 ping

```powershell
# 测试网络连通性
Test-Connection -ComputerName localhost -Count 4

# 测试特定主机
Test-Connection -ComputerName 192.168.1.1 -Count 4
```

#### 5.3.2 telnet

```powershell
# 测试端口连通性
Test-NetConnection -ComputerName localhost -Port 8080

# 测试远程端口
Test-NetConnection -ComputerName 192.168.1.1 -Port 5432
```

## 6. 性能问题排查

### 6.1 CPU使用率过高

#### 6.1.1 诊断步骤

```powershell
# 查看CPU使用率
Get-Counter "\Processor(_Total)\% Processor Time" -SampleInterval 1 -MaxSamples 10

# 查看进程CPU使用
Get-Process | Sort-Object CPU -Descending | Select-Object -First 10

# 查看线程CPU使用
Get-Process -Name java | Select-Object Id, CPU, Threads
```

#### 6.1.2 解决方案

```powershell
# 优化代码
# 1. 减少循环次数
# 2. 使用缓存
# 3. 异步处理

# 增加服务器资源
# 1. 增加CPU核心数
# 2. 使用负载均衡
# 3. 水平扩展
```

### 6.2 内存使用率过高

#### 6.2.1 诊断步骤

```powershell
# 查看内存使用率
Get-Counter "\Memory\Available MBytes" -SampleInterval 1 -MaxSamples 10

# 查看进程内存使用
Get-Process | Sort-Object WorkingSet -Descending | Select-Object -First 10

# 查看JVM堆内存
jmap -heap <pid>
```

#### 6.2.2 解决方案

```powershell
# 增加JVM内存
$JAVA_OPTS = "-Xms2g -Xmx2g"

# 优化内存使用
# 1. 减少对象创建
# 2. 使用对象池
# 3. 及时释放资源

# 增加服务器内存
# 1. 增加物理内存
# 2. 使用交换空间
# 3. 水平扩展
```

### 6.3 磁盘I/O过高

#### 6.3.1 诊断步骤

```powershell
# 查看磁盘I/O
Get-Counter "\PhysicalDisk(_Total)\% Disk Time" -SampleInterval 1 -MaxSamples 10

# 查看磁盘使用率
Get-PSDrive C | Select-Object Used, Free

# 查看磁盘队列长度
Get-Counter "\PhysicalDisk(_Total)\Avg. Disk Queue Length" -SampleInterval 1 -MaxSamples 10
```

#### 6.3.2 解决方案

```powershell
# 优化磁盘I/O
# 1. 使用SSD
# 2. 分离数据和日志
# 3. 使用RAID

# 清理磁盘空间
# 1. 删除临时文件
# 2. 压缩日志文件
# 3. 归档旧数据
```

## 7. 安全问题排查

### 7.1 未授权访问

#### 7.1.1 诊断步骤

```powershell
# 查看访问日志
Get-Content "logs/access.log" | Select-String "401|403"

# 查看认证日志
Get-Content "logs/auth.log" | Select-String "failed|unauthorized"

# 查看异常IP
Get-Content "logs/access.log" | Select-String "192.168.1.100"
```

#### 7.1.2 解决方案

```powershell
# 封禁异常IP
New-NetFirewallRule -DisplayName "Block IP" `
    -Direction Inbound -RemoteAddress "192.168.1.100" -Action Block

# 加强认证
# 1. 使用强密码
# 2. 启用多因素认证
# 3. 定期轮换密钥
```

### 7.2 数据泄露

#### 7.2.1 诊断步骤

```powershell
# 查看数据访问日志
Get-Content "logs/audit.log" | Select-String "SELECT|DELETE|UPDATE"

# 查看敏感数据访问
Get-Content "logs/audit.log" | Select-String "password|secret|token"

# 查看数据导出日志
Get-Content "logs/audit.log" | Select-String "export|download"
```

#### 7.2.2 解决方案

```powershell
# 加密敏感数据
# 1. 使用AES-256加密
# 2. 使用TLS传输
# 3. 使用密钥管理

# 加强访问控制
# 1. 最小权限原则
# 2. 定期审计权限
# 3. 及时撤销权限
```

## 8. 紧急故障处理流程

### 8.1 P1故障处理流程

#### 8.1.1 故障发现

- 监控告警触发
- 用户反馈问题
- 运维人员发现问题

#### 8.1.2 故障响应（15分钟内）

1. **确认故障**：确认故障范围和影响
2. **通知相关人员**：通知运维团队、开发团队、管理层
3. **启动应急响应**：启动应急响应流程

#### 8.1.3 故障定位（30分钟内）

1. **收集信息**：收集系统日志、监控数据、用户反馈
2. **分析问题**：分析问题原因和影响范围
3. **确定方案**：确定故障恢复方案

#### 8.1.4 故障恢复（4小时内）

1. **快速恢复**：优先恢复服务，再进行故障分析
2. **验证恢复**：验证服务恢复正常
3. **通知用户**：通知用户服务已恢复

#### 8.1.5 故障复盘（24小时内）

1. **故障分析**：分析故障原因和影响
2. **总结经验**：总结经验教训
3. **改进措施**：制定改进措施

### 8.2 紧急联系

#### 8.2.1 技术支持

- **技术支持热线**：+86-XXX-XXXX（24小时）
- **技术支持邮箱**：support@example.com
- **紧急联系邮箱**：emergency@example.com

#### 8.2.2 升级流程

| 故障级别 | 升级时间 | 升级对象 |
|---------|---------|---------|
| P1（紧急） | 立即 | CTO、技术总监 |
| P2（高） | 1小时内 | 技术经理、运维经理 |
| P3（中） | 4小时内 | 技术主管、运维主管 |
| P4（低） | 24小时内 | 技术负责人、运维负责人 |

## 9. 联系方式

### 9.1 技术支持

如果遇到问题，请联系：

- **技术支持邮箱**：support@example.com
- **技术支持热线**：+86-XXX-XXXX
- **在线文档**：[故障排查指南](https://example.com/troubleshooting-guide)

### 9.2 紧急联系

对于紧急故障，请联系：

- **紧急联系热线**：+86-XXX-XXXX（24小时）
- **紧急联系邮箱**：emergency@example.com

---

**免责声明**：本指南仅供参考，具体实施以实际环境为准。如有疑问，请联系support@example.com。