# 业务伙伴服务迁移指南

## 概述

本文档描述了如何将现有的 `customer-service` 和 `supplier-service` 合并到新的 `business-partner-service` 中。

## 迁移原因

1. **代码重复**：Customer和Supplier实体具有高度相似的结构
2. **维护成本**：两个独立服务增加了维护复杂性
3. **业务灵活性**：统一的服务可以更好地处理既是客户又是供应商的业务伙伴
4. **资源优化**：减少服务数量，降低系统资源消耗

## 数据迁移

### 1. 数据库表结构

新的 `business_partners` 表结构：

```sql
CREATE TABLE business_partners (
    partner_id BIGSERIAL PRIMARY KEY,
    partner_code VARCHAR(50) NOT NULL UNIQUE,
    partner_name VARCHAR(255) NOT NULL,
    partner_type VARCHAR(20) NOT NULL, -- CUSTOMER, SUPPLIER, BOTH
    contact_person VARCHAR(255),
    phone VARCHAR(50),
    email VARCHAR(255),
    address TEXT,
    credit_rating VARCHAR(20), -- EXCELLENT, GOOD, FAIR, POOR
    credit_limit DECIMAL(15,2),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE
    remark TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 创建索引
CREATE INDEX idx_business_partners_code ON business_partners(partner_code);
CREATE INDEX idx_business_partners_type ON business_partners(partner_type);
CREATE INDEX idx_business_partners_status ON business_partners(status);
CREATE INDEX idx_business_partners_deleted ON business_partners(deleted);
```

### 2. 数据迁移脚本

```sql
-- 迁移客户数据
INSERT INTO business_partners (
    partner_code, partner_name, partner_type, contact_person, phone, email, 
    address, credit_rating, credit_limit, status, created_at, updated_at
)
SELECT 
    customer_code, 
    customer_name, 
    'CUSTOMER' as partner_type,
    contact_person,
    phone,
    email,
    address,
    credit_rating,
    credit_limit,
    status,
    created_at,
    updated_at
FROM customers
WHERE deleted = false;

-- 迁移供应商数据
INSERT INTO business_partners (
    partner_code, partner_name, partner_type, contact_person, phone, 
    email, address, credit_rating, status, created_at, updated_at
)
SELECT 
    supplier_code, 
    supplier_name, 
    'SUPPLIER' as partner_type,
    contact_person,
    phone,
    email,
    address,
    credit_rating,
    status,
    created_at,
    updated_at
FROM suppliers
WHERE deleted = false;

-- 处理既是客户又是供应商的情况（根据业务逻辑）
UPDATE business_partners 
SET partner_type = 'BOTH'
WHERE partner_code IN (
    -- 查找在客户和供应商表中都存在的业务伙伴
    SELECT customer_code FROM customers WHERE deleted = false
    INTERSECT
    SELECT supplier_code FROM suppliers WHERE deleted = false
);
```

## API 迁移

### 1. 客户API映射

| 原API (customer-service) | 新API (business-partner-service) | 说明 |
|--------------------------|----------------------------------|------|
| GET /api/customers | GET /api/business-partners/customers | 获取所有客户 |
| GET /api/customers/{id} | GET /api/business-partners/{id} | 根据ID获取客户 |
| POST /api/customers | POST /api/business-partners | 创建客户 |
| PUT /api/customers/{id} | PUT /api/business-partners/{id} | 更新客户信息 |
| DELETE /api/customers/{id} | DELETE /api/business-partners/{id} | 删除客户 |

### 2. 供应商API映射

| 原API (supplier-service) | 新API (business-partner-service) | 说明 |
|--------------------------|----------------------------------|------|
| GET /api/suppliers | GET /api/business-partners/suppliers | 获取所有供应商 |
| GET /api/suppliers/{id} | GET /api/business-partners/{id} | 根据ID获取供应商 |
| POST /api/suppliers | POST /api/business-partners | 创建供应商 |
| PUT /api/suppliers/{id} | PUT /api/business-partners/{id} | 更新供应商信息 |
| DELETE /api/suppliers/{id} | DELETE /api/business-partners/{id} | 删除供应商 |

## 代码迁移

### 1. 客户端代码更新

#### 原代码示例：
```java
// 使用RestTemplate调用客户服务
String url = "http://customer-service/api/customers";
CustomerDTO customer = restTemplate.getForObject(url + "/" + id, CustomerDTO.class);
```

#### 迁移后代码：
```java
// 使用Feign客户端调用业务伙伴服务
@FeignClient(name = "business-partner-service")
public interface BusinessPartnerClient {
    @GetMapping("/api/business-partners/{id}")
    BusinessPartnerDTO getBusinessPartner(@PathVariable("id") Long id);
    
    @GetMapping("/api/business-partners/customers")
    List<BusinessPartnerDTO> getAllCustomers();
}

// 使用示例
BusinessPartnerDTO partner = businessPartnerClient.getBusinessPartner(id);
// 检查是否为客户
if (partner.isCustomer()) {
    // 处理客户逻辑
}
```

### 2. DTO转换

创建适配器类来处理DTO转换：

```java
public class CustomerAdapter {
    public static CustomerDTO toCustomerDTO(BusinessPartnerDTO partnerDTO) {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setCustomerId(partnerDTO.getPartnerId());
        customerDTO.setCustomerCode(partnerDTO.getPartnerCode());
        customerDTO.setCustomerName(partnerDTO.getPartnerName());
        customerDTO.setContactPerson(partnerDTO.getContactPerson());
        customerDTO.setPhone(partnerDTO.getPhone());
        customerDTO.setEmail(partnerDTO.getEmail());
        customerDTO.setAddress(partnerDTO.getAddress());
        customerDTO.setCreditRating(partnerDTO.getCreditRating());
        customerDTO.setCreditLimit(partnerDTO.getCreditLimit());
        customerDTO.setStatus(partnerDTO.getStatus());
        customerDTO.setCreatedAt(partnerDTO.getCreatedAt());
        customerDTO.setUpdatedAt(partnerDTO.getUpdatedAt());
        return customerDTO;
    }
    
    public static BusinessPartnerDTO fromCustomerDTO(CustomerDTO customerDTO) {
        BusinessPartnerDTO partnerDTO = new BusinessPartnerDTO();
        partnerDTO.setPartnerId(customerDTO.getCustomerId());
        partnerDTO.setPartnerCode(customerDTO.getCustomerCode());
        partnerDTO.setPartnerName(customerDTO.getCustomerName());
        partnerDTO.setPartnerType("CUSTOMER");
        partnerDTO.setContactPerson(customerDTO.getContactPerson());
        partnerDTO.setPhone(customerDTO.getPhone());
        partnerDTO.setEmail(customerDTO.getEmail());
        partnerDTO.setAddress(customerDTO.getAddress());
        partnerDTO.setCreditRating(customerDTO.getCreditRating());
        partnerDTO.setCreditLimit(customerDTO.getCreditLimit());
        partnerDTO.setStatus(customerDTO.getStatus());
        partnerDTO.setCreatedAt(customerDTO.getCreatedAt());
        partnerDTO.setUpdatedAt(customerDTO.getUpdatedAt());
        return partnerDTO;
    }
}
```

## 配置更新

### 1. 服务注册

更新 `application.yml` 配置：

```yaml
# 移除customer-service和supplier-service配置
# 添加business-partner-service配置

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

### 2. 网关路由

更新API网关路由配置：

```yaml
spring:
  cloud:
    gateway:
      routes:
        # 移除customer-service和supplier-service路由
        # 添加business-partner-service路由
        - id: business-partner-service
          uri: lb://business-partner-service
          predicates:
            - Path=/api/business-partners/**
```

## 测试策略

### 1. 单元测试

- 测试BusinessPartner实体的业务逻辑
- 测试Service层的CRUD操作
- 测试Controller的API端点

### 2. 集成测试

- 测试数据库迁移脚本
- 测试API兼容性
- 测试与其他服务的集成

### 3. 性能测试

- 对比迁移前后的性能指标
- 测试并发访问能力
- 验证缓存效果

## 回滚计划

如果迁移过程中出现问题，可以采取以下回滚措施：

1. **服务回滚**：重新启动customer-service和supplier-service
2. **数据回滚**：从备份恢复原始数据
3. **配置回滚**：恢复原有的路由和配置

## 迁移检查清单

- [ ] 备份原始数据库
- [ ] 执行数据迁移脚本
- [ ] 验证数据完整性
- [ ] 更新客户端代码
- [ ] 更新API网关配置
- [ ] 执行集成测试
- [ ] 执行性能测试
- [ ] 监控系统运行状态
- [ ] 文档更新

## 注意事项

1. **渐进式迁移**：建议采用蓝绿部署策略，逐步切换流量
2. **监控告警**：设置迁移期间的监控和告警
3. **兼容性**：保持API向后兼容，给客户端足够的迁移时间
4. **培训**：对开发团队进行新API的培训

## 联系信息

如有迁移相关问题，请联系：
- 技术负责人：tech-lead@company.com
- 数据库团队：dba@company.com
- 运维团队：ops@company.com