# 进销存管理系统 — 功能覆盖检查表

> **项目名称**：进销存管理系统（采购-销售-库存）  
> **技术栈**：Java 21 + Spring Boot 3.4.4 微服务架构  
> **检查日期**：2026-06-08  
> **检查人员**：QA工程师 严过关  
> **文档版本**：v1.0  
> **文档密级**：内部 — 受限分发

---

## 1. 检查表概览

### 1.1 测试执行摘要

| 指标 | 数值 |
|------|------|
| **总测试用例** | 429 |
| **通过** | 423 ✅ |
| **失败** | 0 |
| **错误** | 0 |
| **跳过** | 6 |
| **通过率** | 98.6% |
| **编译失败模块** | 6 个 |

### 1.2 模块覆盖状态总览

| 分类 | 模块总数 | 已测试 ✅ | 部分测试 ⚠️ | 编译失败 ❌ | 无测试 🔧 |
|------|----------|----------|------------|------------|----------|
| 核心服务 | 12 | 5 | 2 | 5 | 0 |
| 支撑服务 | 6 | 5 | 0 | 0 | 1 |
| 公共模块 | 1 | 1 | 0 | 0 | 0 |
| 监控模块 | 1 | 1 | 0 | 0 | 0 |
| 跨服务测试 | 1 | 0 | 0 | 1 | 0 |
| **合计** | **21** | **12** | **2** | **6** | **1** |

### 1.3 未覆盖模块清单

| 模块 | 状态 | 风险等级 | 说明 |
|------|------|----------|------|
| customer-service | ❌ 编译失败 | 🔴 高 | 核心业务，客户信息管理无测试保障 |
| order-service | ❌ 编译失败 | 🔴 高 | 核心业务，订单生命周期无测试保障 |
| procurement-service | ❌ 编译失败 | 🔴 高 | 核心业务，采购流程无测试保障 |
| mall-service | ❌ 编译失败 | 🟡 中 | 商城模块，含11个Controller、10个Service |
| invoice-service | ❌ 编译失败 | 🟡 中 | 发票模块，含7个Controller、8个Service |
| cross-service-tests | ❌ 编译失败 | 🔴 高 | 跨服务集成测试完全缺失 |
| config-service | 🔧 无测试 | 🟢 低 | 配置中心，通常由Nacos托管 |
| product-service | ⚠️ 仅4个测试 | 🟡 中 | Feign Client降级+缓存测试，核心业务测试薄弱 |
| supplier-service | ⚠️ 仅1个测试(1跳过) | 🟡 中 | 供应商管理集成测试跳过 |
| finance-service | ⚠️ 仅1个测试(7个) | 🟡 中 | 11个Controller/14个Service，仅凭证Service有测试 |

---

## 2. 功能模块覆盖状态表

### 2.1 核心服务 — 库存管理（inventory-service）✅ 69个测试

| 功能点 | 测试文件 | 测试状态 | 覆盖等级 |
|--------|----------|----------|----------|
| 库存 CRUD | InventoryServiceImplTest | ✅ 已覆盖 | A |
| 库存缓存 | InventoryServiceImplCacheTest | ✅ 已覆盖 | A |
| 批次管理 | BatchServiceImplTest | ✅ 已覆盖 | A |
| 仓库管理 | WarehouseServiceImplTest | ✅ 已覆盖 | A |
| 库存预警 | InventoryAlertServiceTest | ✅ 已覆盖 | A |
| 库存调拨(StockTransfer) | — | ❌ 未覆盖 | — |
| 其他入库(OtherStockIn) | — | ❌ 未覆盖 | — |
| 其他出库(OtherStockOut) | — | ❌ 未覆盖 | — |
| 库存预警阈值管理 | — | ❌ 未覆盖 | — |
| Kafka消息监听 | — | ❌ 未覆盖 | — |

### 2.2 核心服务 — 订单管理（order-service）❌ 编译失败

| 功能点 | 源码文件 | 测试状态 | 覆盖等级 |
|--------|----------|----------|----------|
| 订单 CRUD | OrderController / OrderServiceImpl | ❌ 编译失败 | — |
| 订单状态机 | OrderServiceImpl | ❌ 编译失败 | — |
| 库存不足异常 | InsufficientInventoryException | ❌ 编译失败 | — |
| 订单不存在异常 | OrderNotFoundException | ❌ 编译失败 | — |
| 订单状态异常 | OrderStatusException | ❌ 编译失败 | — |
| 调用产品服务 | ProductClient | ❌ 编译失败 | — |
| 调用库存服务 | InventoryClient | ❌ 编译失败 | — |

### 2.3 核心服务 — 产品管理（product-service）⚠️ 仅5个测试

| 功能点 | 测试文件 | 测试状态 | 覆盖等级 |
|--------|----------|----------|----------|
| 产品 CRUD | ProductServiceImplTest | ✅ 已覆盖 | B |
| 产品缓存 | ProductServiceImplCacheTest | ✅ 已覆盖 | B |
| 分类管理 | — | ❌ 未覆盖 | — |
| 库存服务Client降级 | InventoryServiceClientFallbackTest | ✅ 已覆盖 | B |
| 订单服务Client降级 | OrderServiceClientFallbackTest | ✅ 已覆盖 | B |
| 产品Controller API | — | ❌ 未覆盖 | — |
| 分类Controller API | — | ❌ 未覆盖 | — |

### 2.4 核心服务 — 采购管理（procurement-service）❌ 编译失败

| 功能点 | 源码文件 | 测试状态 | 覆盖等级 |
|--------|----------|----------|----------|
| 采购单 CRUD | ProcurementController / ProcurementServiceImpl | ❌ 编译失败 | — |
| 采购退货 | ProcurementReturnController / ProcurementReturnServiceImpl | ❌ 编译失败 | — |
| 采购单状态流转 | ProcurementOrderStatusTransitionTest | ❌ 编译失败 | — |
| 调用供应商服务 | SupplierServiceClient | ❌ 编译失败 | — |
| 调用库存服务 | InventoryServiceClient | ❌ 编译失败 | — |
| 调用合作伙伴服务 | BusinessPartnerServiceClient | ❌ 编译失败 | — |

### 2.5 核心服务 — 销售管理（sales-service）❌ 编译失败

| 功能点 | 源码文件 | 测试状态 | 覆盖等级 |
|--------|----------|----------|----------|
| 销售订单 | SalesOrderController / SalesOrderServiceImpl | ❌ 编译失败 | — |
| 销售退货 | SalesReturnController / SalesReturnServiceImpl | ❌ 编译失败 | — |
| 零售管理 | RetailController / RetailServiceImpl | ❌ 编译失败 | — |
| 调用客户服务 | CustomerClient | ❌ 编译失败 | — |
| 调用库存服务 | InventoryClient | ❌ 编译失败 | — |
| 调用产品服务 | ProductClient | ❌ 编译失败 | — |

### 2.6 核心服务 — 客户管理（customer-service）❌ 编译失败

| 功能点 | 源码文件 | 测试状态 | 覆盖等级 |
|--------|----------|----------|----------|
| 客户 CRUD | CustomerController / CustomerServiceImpl | ❌ 编译失败 | — |
| 客户信息缓存 | CustomerServiceImplCacheTest | ❌ 编译失败 | — |
| 客户集成测试 | CustomerControllerIntegrationTest | ❌ 编译失败 | — |

### 2.7 核心服务 — 供应商管理（supplier-service）⚠️ 1个测试(1跳过)

| 功能点 | 测试文件 | 测试状态 | 覆盖等级 |
|--------|----------|----------|----------|
| 供应商 CRUD | SupplierControllerIntegrationTest | ⚠️ 跳过 | D |
| 供应商Service | — | ❌ 未覆盖 | — |

### 2.8 核心服务 — 发票管理（invoice-service）❌ 编译失败

| 功能点 | 源码文件 | 测试状态 | 覆盖等级 |
|--------|----------|----------|----------|
| 客户信息管理 | CustomerInfoController / CustomerInfoService | ❌ 编译失败 | — |
| 智能搜索 | IntelligentSearchController / IntelligentSearchServiceImpl | ❌ 编译失败 | — |
| 发票计算 | InvoiceCalculationController / InvoiceCalculationServiceImpl | ❌ 编译失败 | — |
| 发票产品 | InvoiceProductController / InvoiceProductServiceImpl | ❌ 编译失败 | — |
| 拼音搜索 | PinyinSearchController / PinyinSearchServiceImpl | ❌ 编译失败 | — |
| 单位信息 | UnitInfoController / UnitInfoServiceImpl | ❌ 编译失败 | — |
| 操作日志 | OperationLogController | ❌ 编译失败 | — |
| AES加密 | AesEncryptionUtil | ❌ 编译失败 | — |
| Levenshtein距离 | LevenshteinDistanceUtil | ❌ 编译失败 | — |
| 拼音工具 | PinyinUtil | ❌ 编译失败 | — |
| DTO验证 | CustomerInfoDTOValidationTest | ❌ 编译失败 | — |
| 发票集成 | InvoiceServiceIntegrationTest | ❌ 编译失败 | — |

### 2.9 核心服务 — 商城（mall-service）❌ 编译失败

| 功能点 | 源码文件 | 测试状态 | 覆盖等级 |
|--------|----------|----------|----------|
| 砍价 | BargainController / BargainServiceImpl | ❌ 编译失败 | — |
| 满减折扣 | FullDiscountController / FullDiscountServiceImpl | ❌ 编译失败 | — |
| 拼团 | GroupBuyController / GroupBuyServiceImpl | ❌ 编译失败 | — |
| 发货 | ShipmentController / ShipmentServiceImpl | ❌ 编译失败 | — |
| 优惠券 | CouponController / CouponServiceImpl | ❌ 未覆盖 | — |
| 分销商 | DistributorController / DistributorServiceImpl | ❌ 未覆盖 | — |
| 秒杀 | FlashSaleController / FlashSaleServiceImpl | ❌ 未覆盖 | — |
| 商城订单 | OrderController / OrderServiceImpl | ❌ 未覆盖 | — |
| 支付 | PaymentController / PaymentServiceImpl | ❌ 未覆盖 | — |
| 退款 | RefundController / RefundServiceImpl | ❌ 未覆盖 | — |
| 购物车 | ShoppingCartController / ShoppingCartServiceImpl | ❌ 未覆盖 | — |

### 2.10 核心服务 — 数据源（datasource-service）⚠️ 2个测试(2跳过)

| 功能点 | 测试文件 | 测试状态 | 覆盖等级 |
|--------|----------|----------|----------|
| 连接测试Controller | ConnectionTestControllerTest | ⚠️ 跳过 | D |
| 仪表盘Controller | DashboardControllerTest | ⚠️ 跳过 | D |
| 数据源配置Controller | DatasourceConfigControllerTest | ⚠️ 跳过 | D |
| API契约测试 | DatasourceApiContractTest | ⚠️ 跳过 | D |
| Elasticsearch插件 | ElasticsearchDataSourcePluginTest | ⚠️ 跳过 | D |
| Kudu插件 | KuduDataSourcePluginTest | ⚠️ 跳过 | D |
| MySQL插件 | MySQLDataSourcePluginTest | ⚠️ 跳过 | D |
| 插件注册 | PluginRegistryTest | ⚠️ 跳过 | D |
| PostgreSQL插件 | PostgreSQLDataSourcePluginTest | ⚠️ 跳过 | D |
| 加密服务 | EncryptionServiceTest | ⚠️ 跳过 | D |
| 告警服务 | AlertServiceTest | ⚠️ 跳过 | D |
| 连接测试Service | ConnectionTestServiceTest | ⚠️ 跳过 | D |
| 数据源配置Service | DatasourceConfigServiceTest | ⚠️ 跳过 | D |
| 通知服务 | NotificationServiceTest | ⚠️ 跳过 | D |
| 租户上下文 | TenantContextTest | ⚠️ 跳过 | D |
| 用户上下文 | UserContextTest | ⚠️ 跳过 | D |

### 2.11 支撑服务 — 财务管理（finance-service）⚠️ 仅7个测试

| 功能点 | 源码文件 | 测试状态 | 覆盖等级 |
|--------|----------|----------|----------|
| 凭证管理 | FinanceVoucherController / FinanceVoucherServiceImpl | ✅ 已覆盖 | C |
| 预算管理 | BudgetController / BudgetServiceImpl | ❌ 未覆盖 | — |
| 费用管理 | ExpenseController / IncomeExpenseServiceImpl | ❌ 未覆盖 | — |
| 收入管理 | IncomeController / IncomeService | ❌ 未覆盖 | — |
| 财务账户 | FinanceAccountController / FinanceAccountServiceImpl | ❌ 未覆盖 | — |
| 财务报表 | FinancialReportController / FinancialReportServiceImpl | ❌ 未覆盖 | — |
| 库存集成 | InventoryIntegrationController / InventoryIntegrationServiceImpl | ❌ 未覆盖 | — |
| 付款管理 | PaymentController / PaymentServiceImpl | ❌ 未覆盖 | — |
| 收款管理 | ReceiptController / ReceiptServiceImpl | ❌ 未覆盖 | — |
| 结算账户 | SettlementAccountController / SettlementAccountServiceImpl | ❌ 未覆盖 | — |
| 税务计算 | TaxController / TaxCalculationServiceImpl | ❌ 未覆盖 | — |

### 2.12 支撑服务 — 认证授权（auth-service）✅ 37个测试

| 功能点 | 测试文件 | 测试状态 | 覆盖等级 |
|--------|----------|----------|----------|
| JWT令牌服务 | TokenServiceTest | ✅ 已覆盖 | A |
| JWT黑名单 | JwtBlacklistServiceTest | ✅ 已覆盖 | A |
| 账户锁定 | AccountLockoutServiceTest | ✅ 已覆盖 | A |
| MFA多因子认证 | MFAServiceTest | ✅ 已覆盖 | A |
| 用户详情服务 | UserDetailsServiceImplTest | ✅ 已覆盖 | A |
| 密码策略验证 | PasswordPolicyValidatorTest | ✅ 已覆盖 | A |

### 2.13 支撑服务 — 管理后台（admin-service）✅ 29个测试

| 功能点 | 测试文件 | 测试状态 | 覆盖等级 |
|--------|----------|----------|----------|
| 用户管理 | UserServiceImplTest | ✅ 已覆盖 | A |
| 密码强度 | PasswordStrengthServiceTest | ✅ 已覆盖 | A |

### 2.14 支撑服务 — API网关（gateway-service）✅ 22个测试

| 功能点 | 测试文件 | 测试状态 | 覆盖等级 |
|--------|----------|----------|----------|
| API Key过滤器 | ApiKeyFilterTest | ✅ 已覆盖 | A |
| 审计日志过滤器 | AuditLogFilterTest | ✅ 已覆盖 | A |
| 授权过滤器 | AuthorizationFilterTest | ✅ 已覆盖 | A |
| IP过滤器 | IpFilterTest | ✅ 已覆盖 | A |
| API Key服务 | ApiKeyServiceTest | ✅ 已覆盖 | A |
| 审计日志服务 | AuditLogServiceTest | ✅ 已覆盖 | A |
| 认证服务 | AuthServiceTest | ✅ 已覆盖 | A |
| JWT工具 | JwtUtilTest | ✅ 已覆盖 | A |

### 2.15 支撑服务 — 注册中心（registry-service）✅ 20个测试

| 功能点 | 测试文件 | 测试状态 | 覆盖等级 |
|--------|----------|----------|----------|
| 服务注册Controller | ServiceRegistryControllerTest | ✅ 已覆盖 | A |
| 服务注册Service | ServiceRegistryServiceImplTest | ✅ 已覆盖 | A |

### 2.16 支撑服务 — 报表服务（report-service）✅ 12个测试

| 功能点 | 测试文件 | 测试状态 | 覆盖等级 |
|--------|----------|----------|----------|
| 财务报表 | FinancialReportServiceTest | ✅ 已覆盖 | B |
| 报表Service | ReportServiceImplTest | ✅ 已覆盖 | B |

### 2.17 支撑服务 — 模板服务（template-service）✅ 25个测试

| 功能点 | 测试文件 | 测试状态 | 覆盖等级 |
|--------|----------|----------|----------|
| 模板CRUD | TemplateServiceTest | ✅ 已覆盖 | A |
| 模板版本 | TemplateVersionServiceTest | ✅ 已覆盖 | A |
| 自定义字段 | CustomFieldServiceTest | ✅ 已覆盖 | A |
| 审计日志 | AuditLogServiceTest | ✅ 已覆盖 | A |
| 模板Controller | TemplateControllerTest | ✅ 已覆盖 | A |
| 代码生成器 | CodeGeneratorTest | ✅ 已覆盖 | A |
| 版本工具 | VersionUtilsTest | ✅ 已覆盖 | A |
| 性能测试 | TemplatePerformanceTest | ✅ 已覆盖 | B |
| 集成测试 | TemplateIntegrationTest | ✅ 已覆盖 | B |
| 统计集成测试 | TemplateStatisticsIntegrationTest | ✅ 已覆盖 | B |

### 2.18 公共模块（common）✅ 94个测试

| 功能点 | 测试文件 | 测试状态 | 覆盖等级 |
|--------|----------|----------|----------|
| 错误码生成 | ErrorCodeGeneratorTest | ✅ 已覆盖 | A |
| 全局异常处理 | UnifiedGlobalExceptionHandlerTest | ✅ 已覆盖 | A |
| 数据验证 | DataValidationUtilsTest | ✅ 已覆盖 | A |
| 日期工具 | DateTimeUtilsTest | ✅ 已覆盖 | A |
| 安全工具 | SecurityUtilsTest | ✅ 已覆盖 | A |
| 字符串工具 | StringUtilsTest | ✅ 已覆盖 | A |

### 2.19 监控模块（monitoring）✅ 70个测试

| 功能点 | 测试文件 | 测试状态 | 覆盖等级 |
|--------|----------|----------|----------|
| 业务异常 | BusinessExceptionTest | ✅ 已覆盖 | B |

---

## 3. 核心业务流程覆盖检查

### 3.1 采购入库流程

```
创建采购单(procurement) → 确认供应商(supplier) → 入库(inventory) → 生成应付(finance)
```

| 流程步骤 | 涉及服务 | 测试状态 | 说明 |
|----------|----------|----------|------|
| 创建采购单 | procurement-service | ❌ 编译失败 | ProcurementServiceImpl/Controller 无法运行 |
| 确认供应商 | supplier-service | ⚠️ 跳过 | SupplierControllerIntegrationTest 被跳过 |
| 采购入库（库存增加） | inventory-service | ✅ 已覆盖 | InventoryServiceImplTest 覆盖库存增减 |
| 采购退货 | procurement-service | ❌ 编译失败 | ProcurementReturnServiceImpl 无法运行 |
| 生成应付凭证 | finance-service | ⚠️ 部分 | 仅凭证Service有测试，应付逻辑未覆盖 |
| 采购单状态流转 | procurement-service | ❌ 编译失败 | ProcurementOrderStatusTransitionTest 无法运行 |

**流程覆盖判定**：❌ **不可用** — 6个步骤中仅1个有有效测试

### 3.2 销售出库流程

```
创建销售订单(sales) → 检查库存(inventory) → 扣减库存(inventory) → 生成应收(finance)
```

| 流程步骤 | 涉及服务 | 测试状态 | 说明 |
|----------|----------|----------|------|
| 创建销售订单 | sales-service | ❌ 编译失败 | SalesOrderServiceImpl 无法运行 |
| 零售订单 | sales-service | ❌ 编译失败 | RetailServiceImpl 无法运行 |
| 销售退货 | sales-service | ❌ 编译失败 | SalesReturnServiceImpl 无法运行 |
| 检查/扣减库存 | inventory-service | ✅ 已覆盖 | InventoryServiceImplTest 覆盖 |
| 生成应收凭证 | finance-service | ⚠️ 部分 | 仅凭证Service有测试，应收逻辑未覆盖 |
| 客户验证 | customer-service | ❌ 编译失败 | CustomerServiceImpl 无法运行 |

**流程覆盖判定**：❌ **不可用** — 6个步骤中仅1个有有效测试

### 3.3 库存调拨流程

```
创建调拨单 → 源仓库出库 → 目标仓库入库 → 更新调拨状态
```

| 流程步骤 | 涉及服务 | 测试状态 | 说明 |
|----------|----------|----------|------|
| 创建调拨单 | inventory-service | ❌ 未覆盖 | StockTransferController/Service 无测试 |
| 源仓库出库 | inventory-service | ✅ 已覆盖 | InventoryServiceImplTest 覆盖出库逻辑 |
| 目标仓库入库 | inventory-service | ✅ 已覆盖 | InventoryServiceImplTest 覆盖入库逻辑 |
| 其他入库操作 | inventory-service | ❌ 未覆盖 | OtherStockInOrder 无测试 |
| 其他出库操作 | inventory-service | ❌ 未覆盖 | OtherStockOutOrder 无测试 |
| 库存预警 | inventory-service | ✅ 已覆盖 | InventoryAlertServiceTest 覆盖 |

**流程覆盖判定**：⚠️ **部分可用** — 调拨单本身无测试，但底层出入库有覆盖

### 3.4 财务对账流程

```
采购入库→应付 → 销售出库→应收 → 收付款记录 → 对账结算
```

| 流程步骤 | 涉及服务 | 测试状态 | 说明 |
|----------|----------|----------|------|
| 应付管理 | finance-service | ❌ 未覆盖 | ExpenseController / PaymentController 无测试 |
| 应收管理 | finance-service | ❌ 未覆盖 | IncomeController / ReceiptController 无测试 |
| 收款记录 | finance-service | ❌ 未覆盖 | ReceiptController 无测试 |
| 付款记录 | finance-service | ❌ 未覆盖 | PaymentController 无测试 |
| 结算对账 | finance-service | ❌ 未覆盖 | SettlementAccountController 无测试 |
| 凭证管理 | finance-service | ✅ 已覆盖 | FinanceVoucherServiceImplTest 覆盖 |
| 预算管理 | finance-service | ❌ 未覆盖 | BudgetController 无测试 |
| 税务计算 | finance-service | ❌ 未覆盖 | TaxCalculationServiceImpl 无测试 |
| 库存-财务集成 | finance-service | ❌ 未覆盖 | InventoryIntegrationController 无测试 |

**流程覆盖判定**：❌ **不可用** — 9个步骤中仅1个有有效测试

### 3.5 用户认证授权流程

```
登录认证 → JWT签发 → 网关验证 → 服务鉴权 → Token刷新/黑名单
```

| 流程步骤 | 涉及服务 | 测试状态 | 说明 |
|----------|----------|----------|------|
| 用户登录认证 | auth-service | ✅ 已覆盖 | TokenServiceTest / UserDetailsServiceImplTest |
| JWT签发与验证 | auth-service | ✅ 已覆盖 | TokenServiceTest |
| JWT黑名单 | auth-service | ✅ 已覆盖 | JwtBlacklistServiceTest |
| 账户锁定策略 | auth-service | ✅ 已覆盖 | AccountLockoutServiceTest |
| MFA多因子认证 | auth-service | ✅ 已覆盖 | MFAServiceTest |
| 密码策略 | auth-service | ✅ 已覆盖 | PasswordPolicyValidatorTest |
| 网关JWT验证 | gateway-service | ✅ 已覆盖 | AuthorizationFilterTest / JwtUtilTest |
| 网关IP过滤 | gateway-service | ✅ 已覆盖 | IpFilterTest |
| 网关API Key | gateway-service | ✅ 已覆盖 | ApiKeyFilterTest / ApiKeyServiceTest |
| 网关审计日志 | gateway-service | ✅ 已覆盖 | AuditLogFilterTest / AuditLogServiceTest |

**流程覆盖判定**：✅ **完全可用** — 所有步骤均有测试覆盖

### 3.6 核心业务流程覆盖汇总

| 业务流程 | 覆盖状态 | 有效步骤 | 总步骤 | 覆盖率 |
|----------|----------|----------|--------|--------|
| 采购入库流程 | ❌ 不可用 | 1 | 6 | 16.7% |
| 销售出库流程 | ❌ 不可用 | 1 | 6 | 16.7% |
| 库存调拨流程 | ⚠️ 部分可用 | 3 | 6 | 50.0% |
| 财务对账流程 | ❌ 不可用 | 1 | 9 | 11.1% |
| 用户认证授权流程 | ✅ 完全可用 | 10 | 10 | 100% |
| **加权平均** | — | — | — | **38.9%** |

---

## 4. 测试类型覆盖矩阵

### 4.1 按测试类型统计

| 模块 | 单元测试 | 集成测试 | API测试 | 性能测试 | E2E测试 |
|------|----------|----------|---------|----------|---------|
| common | ✅ 94 | — | — | — | — |
| inventory-service | ✅ 69 | — | — | — | — |
| admin-service | ✅ 29 | — | — | — | — |
| auth-service | ✅ 37 | — | — | — | — |
| template-service | ✅ 15 | ✅ 10 | — | ✅ 1 | — |
| monitoring | ✅ 70 | — | — | — | — |
| gateway-service | ✅ 22 | — | — | — | — |
| registry-service | ✅ 20 | — | — | — | — |
| finance-service | ✅ 7 | — | — | — | — |
| report-service | ✅ 12 | — | — | — | — |
| product-service | ✅ 5 | — | — | — | — |
| supplier-service | — | ⚠️ 1(跳过) | — | — | — |
| datasource-service | — | ⚠️ 2(跳过) | ⚠️ 1(跳过) | — | — |
| customer-service | — | ❌ 编译失败 | — | — | — |
| order-service | ✅ ❌ 编译失败 | — | — | — | — |
| procurement-service | ✅ ❌ 编译失败 | — | — | — | — |
| sales-service | ✅ ❌ 编译失败 | — | — | — | — |
| mall-service | ✅ ❌ 编译失败 | — | — | — | — |
| invoice-service | ✅ ❌ 编译失败 | — | — | — | — |
| cross-service-tests | — | ❌ 编译失败 | — | — | — |
| config-service | — | — | — | — | — |

### 4.2 测试类型覆盖率分析

| 测试类型 | 已覆盖模块 | 未覆盖模块 | 覆盖率 |
|----------|-----------|-----------|--------|
| 单元测试 | 12/21 | 9个（6编译失败+2跳过+1无测试） | 57.1% |
| 集成测试 | 4/21 | 17个 | 19.0% |
| API测试 | 1/21 | 20个 | 4.8% |
| 性能测试 | 1/21 | 20个 | 4.8% |
| E2E测试 | 0/21 | 21个 | 0% |

### 4.3 测试层级缺失分析

| 缺失层级 | 影响评估 | 优先级 |
|----------|----------|--------|
| 跨服务E2E测试完全缺失 | 🔴 无法验证核心业务流程端到端正确性 | P0 |
| API测试几乎缺失 | 🔴 无法验证Controller层接口契约 | P0 |
| 集成测试覆盖不足 | 🟡 仅少数模块有集成测试 | P1 |
| 性能测试仅template有 | 🟢 非功能性指标暂时可控 | P2 |

---

## 5. 未覆盖风险评估

### 5.1 高风险模块详细分析

#### 🔴 customer-service — 客户管理（编译失败）

| 风险维度 | 评估 |
|----------|------|
| **业务影响** | 客户是进销存核心实体，销售、发票、商城均依赖客户信息 |
| **依赖方** | sales-service、invoice-service、mall-service 均通过Feign调用 |
| **功能缺失** | 客户CRUD、客户信用验证、客户信息缓存 |
| **数据风险** | 无测试保障下，客户数据错误可级联影响销售和发票 |
| **修复难度** | 中 — 已有2个测试文件，修复编译即可恢复 |

#### 🔴 order-service — 订单管理（编译失败）

| 风险维度 | 评估 |
|----------|------|
| **业务影响** | 订单是进销存中枢，连接产品、库存、客户、财务 |
| **依赖方** | sales-service、report-service、cross-service-tests |
| **功能缺失** | 订单CRUD、状态机（创建→确认→发货→完成）、库存扣减联动 |
| **数据风险** | 订单状态机错误可导致库存不一致、财务数据错误 |
| **修复难度** | 中 — 已有5个测试文件，含状态异常测试 |

#### 🔴 procurement-service — 采购管理（编译失败）

| 风险维度 | 评估 |
|----------|------|
| **业务影响** | 采购是供应链起点，影响库存入库和应付账款 |
| **依赖方** | inventory-service、finance-service、supplier-service |
| **功能缺失** | 采购单全流程、采购退货、状态流转、供应商联动 |
| **数据风险** | 采购入库错误可导致库存数据与实际不符 |
| **修复难度** | 中 — 已有4个测试文件，含状态流转和退货测试 |

#### 🟡 mall-service — 商城（编译失败）

| 风险维度 | 评估 |
|----------|------|
| **业务影响** | 面向C端用户的商城系统，业务复杂度高 |
| **依赖方** | 独立性较强，但涉及支付和发货 |
| **功能缺失** | 11个Controller、10个Service全部无测试 |
| **数据风险** | 秒杀并发超卖、支付金额计算错误、退款逻辑错误 |
| **修复难度** | 高 — 业务逻辑复杂，测试编写工作量大 |

#### 🟡 invoice-service — 发票（编译失败）

| 风险维度 | 评估 |
|----------|------|
| **业务影响** | 税务合规关键模块，计算错误可导致法律风险 |
| **依赖方** | 客户信息、产品信息 |
| **功能缺失** | 7个Controller、8个Service、3个工具类全部无测试 |
| **数据风险** | 发票金额计算错误、税额计算错误、客户信息泄露 |
| **修复难度** | 中 — 已有12个测试文件，修复编译后可直接运行 |

### 5.2 编译失败影响范围

```
编译失败模块: 6个
├── 核心业务模块: customer, order, procurement (3个) — 直接影响主流程
├── 商城模块: mall (1个) — 影响C端业务
├── 发票模块: invoice (1个) — 影响税务合规
└── 跨服务测试: cross-service-tests (1个) — 无法验证服务间协作

级联影响:
├── 采购入库流程: 3/6步骤受阻
├── 销售出库流程: 4/6步骤受阻
├── 财务对账流程: 4/9步骤受阻
└── 订单履约流程: 不可测试
```

### 5.3 风险矩阵

| 模块 | 业务关键性 | 测试覆盖 | 风险等级 | 风险分值 |
|------|-----------|----------|----------|----------|
| order-service | 🔴 极高 | ❌ 0% | 🔴 极高 | 25 |
| procurement-service | 🔴 极高 | ❌ 0% | 🔴 极高 | 25 |
| customer-service | 🔴 高 | ❌ 0% | 🔴 高 | 20 |
| finance-service | 🔴 高 | ⚠️ 8% | 🟡 中高 | 16 |
| invoice-service | 🟡 中高 | ❌ 0% | 🟡 中高 | 15 |
| sales-service | 🔴 极高 | ❌ 0% | 🔴 极高 | 25 |
| mall-service | 🟡 中 | ❌ 0% | 🟡 中 | 12 |
| product-service | 🔴 高 | ⚠️ 20% | 🟡 中 | 12 |
| supplier-service | 🟡 中 | ⚠️ 0%(跳过) | 🟡 中 | 10 |
| datasource-service | 🟢 低 | ⚠️ 0%(跳过) | 🟢 低 | 6 |
| config-service | 🟢 低 | ❌ 0% | 🟢 低 | 4 |

---

## 6. 边缘场景覆盖检查

### 6.1 并发场景

| 场景 | 涉及模块 | 测试状态 | 风险 |
|------|----------|----------|------|
| 库存超卖（乐观锁） | inventory-service | ❌ 未覆盖 | 🔴 高 |
| 秒杀并发 | mall-service | ❌ 编译失败 | 🔴 高 |
| 重复订单提交 | order-service | ❌ 编译失败 | 🟡 中 |
| 采购单重复提交 | procurement-service | ❌ 编译失败 | 🟡 中 |
| 分布式锁竞争 | inventory-service | ❌ 未覆盖 | 🟡 中 |
| 缓存击穿/雪崩 | 多模块 | ❌ 未覆盖 | 🟡 中 |

### 6.2 边界值场景

| 场景 | 涉及模块 | 测试状态 | 风险 |
|------|----------|----------|------|
| 库存数量为0 | inventory-service | ❌ 未覆盖 | 🟡 中 |
| 库存数量为负数 | inventory-service | ❌ 未覆盖 | 🔴 高 |
| 金额精度溢出 | finance-service | ❌ 未覆盖 | 🔴 高 |
| 超大订单（千行明细） | order-service | ❌ 编译失败 | 🟡 中 |
| 批次过期日期边界 | inventory-service | ❌ 未覆盖 | 🟡 中 |
| 分页查询边界（页码0/-1） | 多模块 | ❌ 未覆盖 | 🟢 低 |
| 字符串长度溢出 | 多模块 | ❌ 未覆盖 | 🟢 低 |

### 6.3 异常输入场景

| 场景 | 涉及模块 | 测试状态 | 风险 |
|------|----------|----------|------|
| SQL注入 | 多模块 | ⚠️ JPA参数化 | 🟢 低 |
| XSS攻击 | 多模块 | ❌ 未覆盖 | 🟡 中 |
| 非法JSON格式 | 多模块 | ❌ 未覆盖 | 🟡 中 |
| 空值/null字段 | 多模块 | ❌ 未覆盖 | 🟡 中 |
| 越权访问（角色越权） | auth-service / gateway | ✅ 已覆盖 | 🟢 低 |
| Token过期/伪造 | auth-service / gateway | ✅ 已覆盖 | 🟢 低 |
| Feign调用超时/降级 | product-service | ✅ 部分覆盖 | 🟡 中 |
| 数据库连接断开 | 多模块 | ❌ 未覆盖 | 🟡 中 |
| Redis不可用 | inventory-service | ❌ 未覆盖 | 🟡 中 |

### 6.4 数据一致性场景

| 场景 | 涉及模块 | 测试状态 | 风险 |
|------|----------|----------|------|
| 跨服务事务回滚 | order→inventory→finance | ❌ 编译失败 | 🔴 高 |
| 库存扣减后订单创建失败 | order-service | ❌ 编译失败 | 🔴 高 |
| 采购入库与应付不同步 | procurement→inventory→finance | ❌ 编译失败 | 🔴 高 |
| 缓存与数据库不一致 | inventory-service | ✅ 部分覆盖 | 🟡 中 |
| 乐观锁版本冲突 | inventory-service | ❌ 未覆盖 | 🟡 中 |

### 6.5 边缘场景覆盖汇总

| 分类 | 总场景数 | 已覆盖 | 未覆盖 | 覆盖率 |
|------|----------|--------|--------|--------|
| 并发场景 | 6 | 0 | 6 | 0% |
| 边界值场景 | 7 | 0 | 7 | 0% |
| 异常输入场景 | 9 | 2 | 7 | 22.2% |
| 数据一致性 | 5 | 1 | 4 | 20.0% |
| **合计** | **27** | **3** | **24** | **11.1%** |

---

## 7. 修复建议清单

### 7.1 P0 — 紧急修复（阻塞核心业务流程）

| 编号 | 建议 | 影响模块 | 预估工时 | 说明 |
|------|------|----------|----------|------|
| P0-01 | 修复 order-service 编译错误 | order-service | 2h | 订单是进销存核心中枢，5个测试已写好但无法运行 |
| P0-02 | 修复 procurement-service 编译错误 | procurement-service | 2h | 采购流程起点，4个测试已写好但无法运行 |
| P0-03 | 修复 customer-service 编译错误 | customer-service | 1h | 2个测试已写好，修复编译即可恢复 |
| P0-04 | 修复 sales-service 编译错误 | sales-service | 2h | 3个测试已写好但无法运行 |
| P0-05 | 修复 cross-service-tests 编译错误 | cross-service-tests | 3h | 跨服务集成测试是验证端到端流程的唯一手段 |
| P0-06 | 修复 invoice-service 编译错误 | invoice-service | 2h | 12个测试已写好，涉及税务合规必须覆盖 |
| P0-07 | 为 order-service 新增库存扣减并发测试 | order-service | 4h | 乐观锁/超卖是最高风险业务场景 |
| P0-08 | 为 inventory-service 新增调拨/出入库测试 | inventory-service | 4h | StockTransfer/OtherStockIn/Out 完全无测试 |

### 7.2 P1 — 重要修复（影响业务完整性和质量）

| 编号 | 建议 | 影响模块 | 预估工时 | 说明 |
|------|------|----------|----------|------|
| P1-01 | 修复 supplier-service 跳过测试 | supplier-service | 2h | SupplierControllerIntegrationTest 被跳过，需修复环境依赖 |
| P1-02 | 修复 datasource-service 跳过测试 | datasource-service | 3h | 16个测试全部跳过，需修复外部数据源依赖 |
| P1-03 | 补充 finance-service 核心 Service 测试 | finance-service | 8h | 11个Controller仅1个有测试，需覆盖收/付/结算/税务 |
| P1-04 | 补充 product-service Controller 测试 | product-service | 4h | 分类管理、产品API端点无测试 |
| P1-05 | 新增跨服务采购入库流程测试 | cross-service-tests | 6h | 验证 procurement→supplier→inventory→finance 完整流程 |
| P1-06 | 新增跨服务销售出库流程测试 | cross-service-tests | 6h | 验证 sales→order→inventory→finance 完整流程 |
| P1-07 | 补充 inventory-service Controller API 测试 | inventory-service | 4h | 当前仅Service层有测试，Controller层未覆盖 |
| P1-08 | 新增金额精度与负数输入测试 | finance-service | 3h | 财务模块的数值边界是最关键的质量保障 |
| P1-09 | 补充 sales-service 集成测试 | sales-service | 4h | 修复编译后需验证Feign Client调用链 |

### 7.3 P2 — 计划改进（提升测试覆盖率和质量）

| 编号 | 建议 | 影响模块 | 预估工时 | 说明 |
|------|------|----------|----------|------|
| P2-01 | 修复 mall-service 编译错误 | mall-service | 3h | 商城4个测试已写好，另有7个Service需补充测试 |
| P2-02 | 为 mall-service 补充秒杀/拼团并发测试 | mall-service | 6h | 秒杀和拼团是高并发场景，需压测验证 |
| P2-03 | 新增 config-service 基础测试 | config-service | 2h | 配置中心虽由Nacos托管，但应有健康检查测试 |
| P2-04 | 补充缓存与DB一致性测试 | inventory-service | 3h | 验证Redis缓存失效与数据库同步 |
| P2-05 | 新增分布式事务回滚测试 | cross-service-tests | 6h | 验证跨服务写操作的最终一致性 |
| P2-06 | 新增API契约测试（Pact/Spring Cloud Contract） | 全局 | 8h | 保障Feign Client接口兼容性 |
| P2-07 | 引入TestContainers集成测试框架 | 全局 | 4h | 当前集成测试依赖不可控，需容器化依赖 |
| P2-08 | 新增JWT Token过期/刷新边缘测试 | auth-service | 2h | 补充Token临界过期、并发刷新场景 |
| P2-09 | 建立JaCoCo覆盖率门禁 | 全局 | 2h | 设计文档要求行≥80%分支≥70%，需CI集成 |
| P2-10 | 新增XSS/注入防御验证测试 | 多模块 | 4h | 安全审计报告B+评级，需补齐输入验证测试 |

### 7.4 修复工时汇总

| 优先级 | 项数 | 预估总工时 | 建议完成时间 |
|--------|------|-----------|-------------|
| P0 紧急 | 8 | 20h | 3个工作日内 |
| P1 重要 | 9 | 40h | 2周内 |
| P2 计划 | 10 | 40h | 1个月内 |
| **合计** | **27** | **100h** | — |

---

## 8. 附录

### 8.1 覆盖等级定义

| 等级 | 定义 | 标准 |
|------|------|------|
| A | 完全覆盖 | 所有公开API和核心逻辑均有测试，含正常/异常路径 |
| B | 较好覆盖 | 主要功能和正常路径有测试，部分异常路径缺失 |
| C | 基本覆盖 | 仅核心功能有测试，异常和边缘场景未覆盖 |
| D | 极少覆盖 | 仅有少量或跳过的测试，无法保障功能正确性 |
| — | 无覆盖 | 无任何测试，或编译失败无法运行 |

### 8.2 风险等级定义

| 等级 | 定义 | 说明 |
|------|------|------|
| 🔴 高/极高 | 阻塞核心业务流程 | 可能导致数据不一致、财务错误、业务中断 |
| 🟡 中 | 影响业务完整性 | 部分功能无法验证，存在潜在缺陷风险 |
| 🟢 低 | 影响可控 | 非核心功能，可延后修复 |

### 8.3 模块测试文件对照表

| 模块 | 测试文件数 | 主要测试类 |
|------|-----------|-----------|
| common | 6 | ErrorCodeGeneratorTest, UnifiedGlobalExceptionHandlerTest, DataValidationUtilsTest, DateTimeUtilsTest, SecurityUtilsTest, StringUtilsTest |
| inventory-service | 5 | InventoryServiceImplTest, InventoryServiceImplCacheTest, BatchServiceImplTest, WarehouseServiceImplTest, InventoryAlertServiceTest |
| order-service | 5 | OrderControllerTest, OrderServiceImplTest, InsufficientInventoryExceptionTest, OrderNotFoundExceptionTest, OrderStatusExceptionTest |
| product-service | 4 | ProductServiceImplTest, ProductServiceImplCacheTest, InventoryServiceClientFallbackTest, OrderServiceClientFallbackTest |
| procurement-service | 4 | ProcurementControllerTest, ProcurementServiceImplTest, ProcurementOrderStatusTransitionTest, ProcurementReturnServiceImplTest |
| sales-service | 3 | SalesOrderServiceImplTest, SalesReturnServiceImplTest, RetailServiceImplTest |
| customer-service | 2 | CustomerControllerIntegrationTest, CustomerServiceImplCacheTest |
| supplier-service | 1 | SupplierControllerIntegrationTest |
| mall-service | 4 | BargainServiceImplTest, FullDiscountServiceImplTest, GroupBuyServiceImplTest, ShipmentServiceImplTest |
| invoice-service | 12 | CustomerInfoDTOValidationTest, InvoiceServiceIntegrationTest, CustomerInfoServiceTest, IntelligentSearchServiceImplTest, InvoiceCalculationServiceTest, InvoiceProductServiceImplTest, PinyinSearchServiceImplTest, UnitInfoServiceTest, AesEncryptionUtilTest, LevenshteinDistanceUtilTest, PinyinUtilTest |
| datasource-service | 16 | ConnectionTestControllerTest, DashboardControllerTest, DatasourceConfigControllerTest, DatasourceApiContractTest, ElasticsearchDataSourcePluginTest, KuduDataSourcePluginTest, MySQLDataSourcePluginTest, PluginRegistryTest, PostgreSQLDataSourcePluginTest, EncryptionServiceTest, AlertServiceTest, ConnectionTestServiceTest, DatasourceConfigServiceTest, NotificationServiceTest, TenantContextTest, UserContextTest |
| finance-service | 1 | FinanceVoucherServiceImplTest |
| auth-service | 6 | TokenServiceTest, JwtBlacklistServiceTest, AccountLockoutServiceTest, MFAServiceTest, UserDetailsServiceImplTest, PasswordPolicyValidatorTest |
| admin-service | 2 | UserServiceImplTest, PasswordStrengthServiceTest |
| gateway-service | 8 | ApiKeyFilterTest, AuditLogFilterTest, AuthorizationFilterTest, IpFilterTest, ApiKeyServiceTest, AuditLogServiceTest, AuthServiceTest, JwtUtilTest |
| registry-service | 2 | ServiceRegistryControllerTest, ServiceRegistryServiceImplTest |
| report-service | 2 | FinancialReportServiceTest, ReportServiceImplTest |
| template-service | 12 | TemplateServiceTest, TemplateVersionServiceTest, CustomFieldServiceTest, AuditLogServiceTest, TemplateControllerTest, CodeGeneratorTest, VersionUtilsTest, TemplatePerformanceTest, TemplateIntegrationTest, TemplateStatisticsIntegrationTest, TestCacheConfig, TestSecurityConfig |
| monitoring | 1 | BusinessExceptionTest |
| cross-service-tests | 4 | CrossServiceIntegrationTestBase, OrderCreationIntegrationTest, ReportServiceIntegrationTest, CrossServiceTestApplication |
| config-service | 0 | — |

### 8.4 版本记录

| 版本 | 日期 | 作者 | 说明 |
|------|------|------|------|
| v1.0 | 2026-06-08 | 严过关（QA工程师） | 初始版本，基于429个测试用例执行结果分析 |

---

> **结论**：当前系统测试通过率98.6%，但**功能覆盖深度严重不足**。5个核心业务模块（order、procurement、customer、sales、invoice）编译失败导致60%的核心业务流程无法通过测试验证。建议**优先修复P0级编译问题**（预估20h），使已编写的26个测试用例恢复运行，预计可将核心业务流程覆盖率从38.9%提升至60%以上。
