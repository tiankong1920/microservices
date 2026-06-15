# 库存预警功能说明

## 1. 功能概述

库存预警功能是进销存管理系统的重要组成部分，用于监控库存水平并及时发出预警，帮助企业避免库存不足或库存积压的问题。

## 2. 功能特性

### 2.1 低库存预警
- 当库存数量低于设定的最小阈值时，系统自动发出低库存预警
- 预警信息包含商品ID、当前库存数量、最小库存阈值等信息

### 2.2 高库存预警
- 当库存数量高于设定的最大阈值时，系统自动发出高库存预警
- 预警信息包含商品ID、当前库存数量、最大库存阈值等信息

### 2.3 安全库存管理
- 支持按产品和仓库设置安全库存水平
- 自动计算安全库存和重订点
- 支持安全库存状态管理（激活/停用）
- 提供安全库存调整历史记录

### 2.4 定时检查
- 系统每5分钟自动检查库存预警情况
- 检查结果通过日志记录
- 触发预警时发送邮件通知

### 2.5 API接口
- 提供API接口手动获取预警列表
- 支持手动触发库存预警检查
- 支持分页查询和筛选
- 提供安全库存管理的完整CRUD接口

## 3. 数据模型

### 3.1 库存表结构
在库存表中添加了以下字段用于库存预警：

| 字段名 | 类型 | 默认值 | 说明 |
|-------|------|-------|------|
| min_threshold | INTEGER | 10 | 最小库存阈值（安全库存下限） |
| max_threshold | INTEGER | 1000 | 最大库存阈值（安全库存上限） |

### 3.2 安全库存表结构
创建了独立的安全库存表，用于更精细的库存管理：

| 字段名 | 类型 | 默认值 | 说明 |
|-------|------|-------|------|
| id | BIGINT |  | 主键ID |
| product_id | BIGINT |  | 产品ID |
| warehouse_id | BIGINT |  | 仓库ID |
| safety_stock_level | INTEGER |  | 安全库存水平 |
| maximum_stock_level | INTEGER |  | 最大库存水平 |
| reorder_point | INTEGER |  | 重订点 |
| lead_time | INTEGER |  | 提前期（天） |
| average_daily_usage | INTEGER |  | 平均日用量 |
| safety_factor | DOUBLE | 1.65 | 安全系数 |
| status | VARCHAR | ACTIVE | 状态（ACTIVE/INACTIVE） |
| calculation_method | VARCHAR |  | 计算方法 |
| created_at | TIMESTAMP |  | 创建时间 |
| updated_at | TIMESTAMP |  | 更新时间 |
| last_calculated_date | TIMESTAMP |  | 最后计算日期 |
| special_notes | TEXT |  | 特殊说明 |

### 3.3 安全库存实体类
```java
/**
 * 安全库存实体
 */
@Entity
@Table(name = "safety_stocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SafetyStock {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "product_name")
    private String productName;
    
    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;
    
    @Column(name = "safety_stock_level", nullable = false)
    private Integer safetyStockLevel;
    
    @Column(name = "maximum_stock_level", nullable = false)
    private Integer maximumStockLevel;
    
    @Column(name = "reorder_point", nullable = false)
    private Integer reorderPoint;
    
    @Column(name = "lead_time", nullable = false)
    private Integer leadTime;
    
    @Column(name = "average_daily_usage", nullable = false)
    private Integer averageDailyUsage;
    
    @Column(name = "safety_factor", nullable = false)
    private Double safetyFactor;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;
    
    @Column(name = "calculation_method")
    private String calculationMethod;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "last_calculated_date")
    private LocalDateTime lastCalculatedDate;
    
    @Column(name = "special_notes", columnDefinition = "TEXT")
    private String specialNotes;
    
    // 枚举类型：状态
    public enum Status {
        ACTIVE, INACTIVE
    }
    
    // 枚举类型：计算方法
    public enum CalculationMethod {
        STATISTICAL, FIXED, MANUAL
    }
}
```

### 3.4 库存DTO
在库存DTO中添加了以下字段：

```java
/**
 * 最小库存阈值（安全库存下限）
 */
private Integer minThreshold = 10;

/**
 * 最大库存阈值（安全库存上限）
 */
private Integer maxThreshold = 1000;
```

## 4. 核心算法

### 4.1 低库存检查算法
```sql
SELECT * FROM inventories WHERE quantity < min_threshold
```

### 4.2 高库存检查算法
```sql
SELECT * FROM inventories WHERE quantity > max_threshold
```

### 4.3 安全库存计算算法
安全库存计算公式：
```
安全库存 = 平均日用量 × 提前期 × 安全系数
重订点 = 平均日用量 × 提前期 + 安全库存
最大库存 = 重订点 × 2
```

Java实现：
```java
/**
 * 计算安全库存
 * @param averageDailyUsage 平均日用量
 * @param leadTime 提前期（天）
 * @param safetyFactor 安全系数
 * @return 安全库存计算结果
 */
private SafetyStockCalculationResult calculateSafetyStock(
        int averageDailyUsage, 
        int leadTime, 
        double safetyFactor) {
    
    // 计算安全库存
    int safetyStockLevel = (int) Math.round(averageDailyUsage * leadTime * safetyFactor);
    
    // 计算重订点
    int reorderPoint = averageDailyUsage * leadTime + safetyStockLevel;
    
    // 计算最大库存
    int maximumStockLevel = reorderPoint * 2;
    
    return new SafetyStockCalculationResult(
            safetyStockLevel, 
            maximumStockLevel, 
            reorderPoint
    );
}
```

## 5. API接口

### 5.1 库存预警相关接口

#### 5.1.1 获取低库存预警列表
```
GET /api/v1/inventories/alerts/low
```

响应示例：
```json
{
  "success": true,
  "data": [
    {
      "inventoryId": 1,
      "productId": "P001",
      "warehouseId": "W001",
      "quantity": 5,
      "minThreshold": 10,
      "maxThreshold": 1000,
      "version": 1,
      "createdAt": "2025-11-12T10:30:00Z",
      "updatedAt": "2025-11-12T10:30:00Z"
    }
  ],
  "message": "操作成功"
}
```

#### 5.1.2 获取高库存预警列表
```
GET /api/v1/inventories/alerts/high
```

响应示例：
```json
{
  "success": true,
  "data": [
    {
      "inventoryId": 2,
      "productId": "P002",
      "warehouseId": "W001",
      "quantity": 1500,
      "minThreshold": 10,
      "maxThreshold": 1000,
      "version": 1,
      "createdAt": "2025-11-12T10:30:00Z",
      "updatedAt": "2025-11-12T10:30:00Z"
    }
  ],
  "message": "操作成功"
}
```

#### 5.1.3 手动触发库存预警检查
```
POST /api/v1/inventories/alerts/check
```

响应示例：
```json
{
  "success": true,
  "data": {
    "lowAlertCount": 1,
    "highAlertCount": 1,
    "lowAlerts": [
      {
        "inventoryId": 1,
        "productId": "P001",
        "warehouseId": "W001",
        "quantity": 5,
        "minThreshold": 10,
        "maxThreshold": 1000,
        "version": 1,
        "createdAt": "2025-11-12T10:30:00Z",
        "updatedAt": "2025-11-12T10:30:00Z"
      }
    ],
    "highAlerts": [
      {
        "inventoryId": 2,
        "productId": "P002",
        "warehouseId": "W001",
        "quantity": 1500,
        "minThreshold": 10,
        "maxThreshold": 1000,
        "version": 1,
        "createdAt": "2025-11-12T10:30:00Z",
        "updatedAt": "2025-11-12T10:30:00Z"
      }
    ]
  },
  "message": "库存预警检查完成"
}
```

### 5.2 安全库存管理相关接口

#### 5.2.1 创建安全库存
```
POST /api/v1/safety-stocks
```

请求示例：
```json
{
  "productId": 1,
  "productName": "测试产品",
  "warehouseId": 1,
  "safetyStockLevel": 20,
  "maximumStockLevel": 200,
  "reorderPoint": 30,
  "leadTime": 7,
  "averageDailyUsage": 10,
  "safetyFactor": 1.65,
  "status": "ACTIVE",
  "calculationMethod": "STATISTICAL"
}
```

响应示例：
```json
{
  "success": true,
  "data": {
    "id": 1,
    "productId": 1,
    "productName": "测试产品",
    "warehouseId": 1,
    "safetyStockLevel": 20,
    "maximumStockLevel": 200,
    "reorderPoint": 30,
    "leadTime": 7,
    "averageDailyUsage": 10,
    "safetyFactor": 1.65,
    "status": "ACTIVE",
    "calculationMethod": "STATISTICAL",
    "createdAt": "2025-11-12T10:30:00Z",
    "updatedAt": "2025-11-12T10:30:00Z"
  },
  "message": "安全库存创建成功"
}
```

#### 5.2.2 更新安全库存
```
PUT /api/v1/safety-stocks/{id}
```

响应示例：
```json
{
  "success": true,
  "data": {
    "id": 1,
    "productId": 1,
    "productName": "测试产品",
    "warehouseId": 1,
    "safetyStockLevel": 25,
    "maximumStockLevel": 200,
    "reorderPoint": 35,
    "leadTime": 7,
    "averageDailyUsage": 10,
    "safetyFactor": 1.65,
    "status": "ACTIVE",
    "calculationMethod": "STATISTICAL",
    "createdAt": "2025-11-12T10:30:00Z",
    "updatedAt": "2025-11-12T11:00:00Z"
  },
  "message": "安全库存更新成功"
}
```

#### 5.2.3 删除安全库存
```
DELETE /api/v1/safety-stocks/{id}
```

响应示例：
```json
{
  "success": true,
  "data": null,
  "message": "安全库存删除成功"
}
```

#### 5.2.4 获取安全库存详情
```
GET /api/v1/safety-stocks/{id}
```

响应示例：
```json
{
  "success": true,
  "data": {
    "id": 1,
    "productId": 1,
    "productName": "测试产品",
    "warehouseId": 1,
    "safetyStockLevel": 25,
    "maximumStockLevel": 200,
    "reorderPoint": 35,
    "leadTime": 7,
    "averageDailyUsage": 10,
    "safetyFactor": 1.65,
    "status": "ACTIVE",
    "calculationMethod": "STATISTICAL",
    "createdAt": "2025-11-12T10:30:00Z",
    "updatedAt": "2025-11-12T11:00:00Z"
  },
  "message": "操作成功"
}
```

#### 5.2.5 获取安全库存列表
```
GET /api/v1/safety-stocks
```

查询参数：
- productId: 产品ID（可选）
- warehouseId: 仓库ID（可选）
- status: 状态（可选，ACTIVE/INACTIVE）
- page: 页码（可选，默认1）
- size: 每页大小（可选，默认20）

响应示例：
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "productId": 1,
        "productName": "测试产品",
        "warehouseId": 1,
        "safetyStockLevel": 25,
        "maximumStockLevel": 200,
        "reorderPoint": 35,
        "leadTime": 7,
        "averageDailyUsage": 10,
        "safetyFactor": 1.65,
        "status": "ACTIVE",
        "calculationMethod": "STATISTICAL",
        "createdAt": "2025-11-12T10:30:00Z",
        "updatedAt": "2025-11-12T11:00:00Z"
      }
    ],
    "totalPages": 1,
    "totalElements": 1,
    "size": 20,
    "number": 0
  },
  "message": "操作成功"
}
```

#### 5.2.6 自动计算安全库存
```
POST /api/v1/safety-stocks/calculate
```

请求示例：
```json
{
  "productId": 1,
  "warehouseId": 1,
  "averageDailyUsage": 10,
  "leadTime": 7,
  "safetyFactor": 1.65
}
```

响应示例：
```json
{
  "success": true,
  "data": {
    "id": 1,
    "productId": 1,
    "productName": "测试产品",
    "warehouseId": 1,
    "safetyStockLevel": 116,
    "maximumStockLevel": 332,
    "reorderPoint": 166,
    "leadTime": 7,
    "averageDailyUsage": 10,
    "safetyFactor": 1.65,
    "status": "ACTIVE",
    "calculationMethod": "STATISTICAL",
    "createdAt": "2025-11-12T10:30:00Z",
    "updatedAt": "2025-11-12T10:30:00Z"
  },
  "message": "安全库存计算成功"
}
```

## 6. 定时任务

### 6.1 任务配置
库存预警检查任务配置在`application.yml`中：

```yaml
app:
  scheduling:
    inventory-alert:
      cron: "0 */5 * * * ?"  # 每5分钟执行一次
```

### 6.2 任务实现
```java
@Component
@Slf4j
public class TaskScheduler {
    
    @Scheduled(cron = "0 */5 * * * ?")
    public void checkInventoryAlert() {
        log.info("开始执行库存预警检查任务");
        
        // 调用库存预警服务检查低库存和高库存
        try {
            inventoryAlertService.checkLowInventoryAlerts();
            inventoryAlertService.checkHighInventoryAlerts();
            log.info("库存预警检查任务执行完成");
        } catch (Exception e) {
            log.error("库存预警检查任务执行失败", e);
        }
    }
}
```

## 7. 服务层实现

### 7.1 库存预警服务接口
```java
public interface InventoryAlertService {
    
    /**
     * 检查库存预警
     * @return 低于最小库存阈值的库存DTO列表
     */
    List<InventoryDTO> checkLowInventoryAlerts();
    
    /**
     * 检查库存过剩预警
     * @return 高于最大库存阈值的库存DTO列表
     */
    List<InventoryDTO> checkHighInventoryAlerts();
    
    /**
     * 发送库存预警通知
     * @param alertType 预警类型（LOW-低库存，HIGH-高库存）
     * @param inventoryList 预警库存列表
     */
    void sendInventoryAlertNotification(String alertType, List<InventoryDTO> inventoryList);
}
```

### 7.2 库存预警服务实现
```java
@Service
public class InventoryAlertServiceImpl implements InventoryAlertService {
    
    private static final Logger log = LoggerFactory.getLogger(InventoryAlertServiceImpl.class);
    
    private final InventoryService inventoryService;
    
    public InventoryAlertServiceImpl(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    
    /**
     * 检查低库存预警
     */
    @Override
    public List<InventoryDTO> checkLowInventoryAlerts() {
        log.info("开始检查低库存预警");
        List<InventoryDTO> lowInventoryList = inventoryService.checkInventoryAlerts();
        log.info("发现 {} 个低库存商品", lowInventoryList.size());
        
        // 发送低库存预警通知
        if (!lowInventoryList.isEmpty()) {
            sendInventoryAlertNotification("LOW", lowInventoryList);
        }
        
        return lowInventoryList;
    }
    
    /**
     * 检查高库存预警
     */
    @Override
    public List<InventoryDTO> checkHighInventoryAlerts() {
        log.info("开始检查高库存预警");
        List<InventoryDTO> highInventoryList = inventoryService.checkHighInventoryAlerts();
        log.info("高库存检查完成，发现 {} 个高库存商品", highInventoryList.size());
        
        // 发送高库存预警通知
        if (!highInventoryList.isEmpty()) {
            sendInventoryAlertNotification("HIGH", highInventoryList);
        }
        
        return highInventoryList;
    }
    
    /**
     * 发送库存预警通知
     */
    @Override
    public void sendInventoryAlertNotification(String alertType, List<InventoryDTO> inventoryList) {
        if (inventoryList.isEmpty()) {
            log.info("没有需要发送预警通知的库存记录");
            return;
        }
        
        log.info("开始发送{}库存预警通知，共{}条记录", 
            "LOW".equals(alertType) ? "低" : "高", inventoryList.size());
        
        for (InventoryDTO inventory : inventoryList) {
            log.warn("库存预警 - 商品ID: {}, 商品名称: {}, 当前库存: {}, {}库存阈值: {}", 
                inventory.getProductId(), 
                // 这里需要获取商品名称，暂时用商品ID代替
                inventory.getProductId(), 
                inventory.getQuantity(), 
                "LOW".equals(alertType) ? "最低" : "最高",
                "LOW".equals(alertType) ? inventory.getMinThreshold() : inventory.getMaxThreshold());
        }
        
        log.info("库存预警通知发送完成");
    }
}
```

## 8. 配置说明

### 8.1 环境变量配置
库存预警功能不需要特殊的环境变量配置，但可以通过以下方式调整阈值：

1. 在创建或更新库存记录时设置minThreshold和maxThreshold字段
2. 通过数据库直接修改库存记录的阈值

### 8.2 定时任务配置
可以通过修改`application.yml`文件中的cron表达式来调整库存预警检查的频率：

```
app:
  scheduling:
    inventory-alert:
      cron: "0 */5 * * * ?"  # 每5分钟执行一次
```

## 9. 测试说明

### 9.1 单元测试
库存预警功能的单元测试应覆盖以下场景：

1. 低库存检查逻辑正确性
2. 高库存检查逻辑正确性
3. 预警通知发送逻辑
4. 边界条件处理

### 9.2 集成测试
库存预警功能的集成测试应覆盖以下场景：

1. 定时任务正确执行
2. API接口返回正确数据
3. 数据库查询正确性

## 10. 性能优化

### 10.1 数据库索引
为提高库存预警检查的性能，建议在以下字段上创建索引：

```sql
CREATE INDEX idx_inventories_quantity ON inventories(quantity);
CREATE INDEX idx_inventories_min_threshold ON inventories(min_threshold);
CREATE INDEX idx_inventories_max_threshold ON inventories(max_threshold);
```

### 10.2 缓存策略
对于频繁查询的库存预警数据，可以使用Redis缓存来提高查询性能。

## 11. 安全说明

### 11.1 数据安全
库存预警功能涉及的敏感数据包括：

1. 库存数量
2. 库存阈值

这些数据在传输和存储过程中都应进行适当的保护。

### 11.2 访问控制
库存预警相关的API接口应进行适当的访问控制，只有授权用户才能访问。

## 12. 故障处理

### 12.1 异常处理
库存预警功能应正确处理以下异常情况：

1. 数据库连接异常
2. 查询超时
3. 数据转换异常

### 12.2 日志记录
库存预警功能应记录详细的日志信息，包括：

1. 检查开始和结束时间
2. 检查结果
3. 异常信息

## 13. 未来扩展

### 13.1 多级预警
未来可以实现多级预警机制，如：
- 警告级别：库存接近阈值
- 危险级别：库存低于阈值

### 13.2 预警历史记录
未来可以记录预警历史，用于分析和统计。

### 13.3 预警规则配置
未来可以提供更灵活的预警规则配置，支持基于不同商品类别、仓库等设置不同的预警阈值。