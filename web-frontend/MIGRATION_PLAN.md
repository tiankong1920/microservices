# JSX页面迁移计划

## 概述

将现有的JSX页面迁移到TypeScript (TSX)，提升代码类型安全性和可维护性。

## 迁移策略

按业务模块分批迁移，每批5-10个页面，确保：
1. 功能完整性
2. UI视觉一致性
3. 充分的测试覆盖

## 批次划分

### 第1批：客户管理模块 (3个页面)
| 页面 | 文件 | 复杂度 | 预估工时 |
|------|------|--------|----------|
| 客户详情 | CustomerDetail.jsx | 中 | 4h |
| 客户编辑 | CustomerEdit.jsx | 中 | 4h |
| 客户管理 | CustomerManagement.jsx | 高 | 6h |

**依赖分析**：
- API服务：`customerApi`
- 共享组件：Layout
- 路由：react-router-dom

---

### 第2批：供应商管理模块 (3个页面)
| 页面 | 文件 | 复杂度 | 预估工时 |
|------|------|--------|----------|
| 供应商详情 | SupplierDetail.jsx | 中 | 4h |
| 供应商编辑 | SupplierEdit.jsx | 中 | 4h |
| 供应商管理 | SupplierManagement.jsx | 高 | 6h |

**依赖分析**：
- API服务：`supplierApi`
- 共享组件：Layout

---

### 第3批：产品管理模块 (3个页面)
| 页面 | 文件 | 复杂度 | 预估工时 |
|------|------|--------|----------|
| 产品详情 | ProductDetail.jsx | 中 | 4h |
| 产品编辑 | ProductEdit.jsx | 中 | 4h |
| 产品管理 | ProductManagement.tsx | 低 | 2h (已是TSX) |

---

### 第4批：订单管理模块 (3个页面)
| 页面 | 文件 | 复杂度 | 预估工时 |
|------|------|--------|----------|
| 创建订单 | OrderCreate.jsx | 高 | 6h |
| 订单详情 | OrderDetail.jsx | 中 | 4h |
| 订单管理 | OrderManagement.jsx | 高 | 6h |

---

### 第5批：销售管理模块 (5个页面)
| 页面 | 文件 | 复杂度 | 预估工时 |
|------|------|--------|----------|
| 销售详情 | SalesDetail.jsx | 中 | 4h |
| 销售管理 | SalesManagement.jsx | 高 | 6h |
| 销售退货详情 | SalesReturnDetail.jsx | 中 | 4h |
| 零售详情 | RetailDetail.jsx | 中 | 4h |
| 零售管理 | RetailManagement.jsx | 高 | 6h |

---

### 第6批：采购管理模块 (3个页面)
| 页面 | 文件 | 复杂度 | 预估工时 |
|------|------|--------|----------|
| 采购详情 | ProcurementDetail.jsx | 中 | 4h |
| 入库详情 | ReceiptDetail.jsx | 中 | 4h |
| 入库管理 | ReceiptManagement.jsx | 高 | 6h |

---

### 第7批：财务管理模块 (4个页面)
| 页面 | 文件 | 复杂度 | 预估工时 |
|------|------|--------|----------|
| 付款详情 | PaymentDetail.jsx | 中 | 4h |
| 付款管理 | PaymentManagement.jsx | 高 | 6h |
| 收入详情 | IncomeDetail.jsx | 中 | 4h |
| 收入管理 | IncomeManagement.jsx | 高 | 6h |

---

### 第8批：报表系统模块 (4个页面)
| 页面 | 文件 | 复杂度 | 预估工时 |
|------|------|--------|----------|
| 财务报表 | FinancialReport.jsx | 高 | 6h |
| 库存报表 | InventoryReport.jsx | 高 | 6h |
| 采购报表 | PurchaseReport.jsx | 高 | 6h |
| 销售报表 | SalesReport.jsx | 高 | 6h |

---

### 第9批：库存管理模块 (1个页面)
| 页面 | 文件 | 复杂度 | 预估工时 |
|------|------|--------|----------|
| 其他库存详情 | OtherStockDetail.jsx | 低 | 2h |

---

### 第10批：系统管理模块 (4个页面)
| 页面 | 文件 | 复杂度 | 预估工时 |
|------|------|--------|----------|
| 仪表盘 | Dashboard.tsx | 低 | 2h (已是TSX) |
| 登录 | Login.tsx | 低 | 2h (已是TSX) |
| 系统管理 | SystemManagement.jsx | 高 | 6h |
| 用户管理 | UserManagement.jsx | 高 | 6h |

---

## 迁移流程

每批页面按以下流程执行：

### 1. 影响范围评估 (2小时)
- [ ] 分析依赖组件
- [ ] 识别API调用
- [ ] 评估数据流影响
- [ ] 更新迁移跟踪文档

### 2. 代码迁移 (4-6小时)
- [ ] 创建类型定义文件
- [ ] 文件重命名 .jsx → .tsx
- [ ] 添加类型注解
- [ ] 修复类型错误
- [ ] 更新导入路径

### 3. 功能验证 (2小时)
- [ ] 功能测试
- [ ] UI一致性检查
- [ ] 性能测试
- [ ] 兼容性测试

### 4. 单元测试 (4小时)
- [ ] 编写测试用例
- [ ] 运行测试
- [ ] 验证覆盖率 ≥ 80%
- [ ] 修复测试失败

---

## 类型定义规划

### 共享类型 (src/types/index.ts)
```typescript
// 基础实体类型
export interface Customer {
  id: string;
  customerId: string;
  customerName: string;
  contactPerson: string;
  phone: string;
  email: string;
  address: string;
  status: 'ACTIVE' | 'INACTIVE';
  createdAt: string;
  updatedAt: string;
  remark?: string;
}

export interface Product {
  id: string;
  productId: string;
  productName: string;
  category: string;
  price: number;
  stock: number;
  status: string;
}

export interface Order {
  id: string;
  orderId: string;
  customerId: string;
  items: OrderItem[];
  totalAmount: number;
  status: string;
  createdAt: string;
}

export interface OrderItem {
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

// API响应类型
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: string;
}

// 分页类型
export interface PageResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
```

---

## 测试策略

### 测试框架
- **Jest**: 测试运行器
- **React Testing Library**: 组件测试
- **MSW (Mock Service Worker)**: API模拟

### 测试覆盖率要求
- 行覆盖率 ≥ 80%
- 分支覆盖率 ≥ 70%
- 函数覆盖率 ≥ 80%

### 测试内容
1. **组件渲染测试**: 验证组件正确渲染
2. **用户交互测试**: 模拟点击、输入等操作
3. **API调用测试**: 模拟API请求和响应
4. **边界条件测试**: 空数据、错误状态等
5. **路由测试**: 页面导航验证

---

## 风险与应对

| 风险 | 影响 | 应对措施 |
|------|------|----------|
| 类型定义不完整 | 中 | 逐步完善类型定义，使用any作为临时方案 |
| 第三方库无类型 | 低 | 安装@types包或创建声明文件 |
| 迁移后功能异常 | 高 | 充分的单元测试和集成测试 |
| 工期延误 | 中 | 每批预留缓冲时间，及时沟通 |

---

## 验收标准

1. 所有JSX页面成功迁移为TSX
2. TypeScript编译无错误
3. 单元测试覆盖率 ≥ 80%
4. 功能测试通过率 100%
5. 性能无显著下降
