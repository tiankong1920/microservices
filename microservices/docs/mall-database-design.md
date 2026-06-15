# 商城系统数据库设计

## 1. 商品管理模块

### 1.1 商品表 (products)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 商品ID |
| product_code | VARCHAR(50) | UNIQUE | 商品编码 |
| name | VARCHAR(255) | NOT NULL | 商品名称 |
| brand | VARCHAR(100) | | 品牌 |
| category_id | BIGINT | FOREIGN KEY | 分类ID |
| price | DECIMAL(10,2) | NOT NULL | 售价 |
| cost_price | DECIMAL(10,2) | | 成本价 |
| stock | INT | DEFAULT 0 | 库存 |
| status | VARCHAR(20) | NOT NULL | 状态(DRAFT, PENDING, PUBLISHED, OFFLINE) |
| description | TEXT | | 商品描述 |
| main_image | VARCHAR(255) | | 主图 |
| video_url | VARCHAR(255) | | 视频地址 |
| weight | DECIMAL(8,2) | | 重量 |
| dimensions | VARCHAR(100) | | 尺寸 |
| created_by | BIGINT | | 创建人 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

### 1.2 商品分类表 (product_categories)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 分类ID |
| name | VARCHAR(100) | NOT NULL | 分类名称 |
| parent_id | BIGINT | FOREIGN KEY | 父分类ID |
| level | INT | NOT NULL | 层级 |
| sort_order | INT | DEFAULT 0 | 排序 |
| status | VARCHAR(20) | NOT NULL | 状态(ACTIVE, INACTIVE) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

### 1.3 商品规格表 (product_specs)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 规格ID |
| product_id | BIGINT | FOREIGN KEY | 商品ID |
| spec_name | VARCHAR(50) | NOT NULL | 规格名称(如颜色、尺寸) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 1.4 商品规格值表 (product_spec_values)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 规格值ID |
| spec_id | BIGINT | FOREIGN KEY | 规格ID |
| value | VARCHAR(50) | NOT NULL | 规格值(如红色、M码) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 1.5 SKU表 (product_skus)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | SKU ID |
| sku_code | VARCHAR(50) | UNIQUE | SKU编码 |
| product_id | BIGINT | FOREIGN KEY | 商品ID |
| attributes | JSONB | NOT NULL | 属性组合(JSON格式) |
| price | DECIMAL(10,2) | NOT NULL | SKU价格 |
| stock | INT | DEFAULT 0 | SKU库存 |
| barcode | VARCHAR(50) | | 条码 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

### 1.6 商品图片表 (product_images)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 图片ID |
| product_id | BIGINT | FOREIGN KEY | 商品ID |
| image_url | VARCHAR(255) | NOT NULL | 图片地址 |
| sort_order | INT | DEFAULT 0 | 排序 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 1.7 商品审核表 (product_audits)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 审核ID |
| product_id | BIGINT | FOREIGN KEY | 商品ID |
| old_status | VARCHAR(20) | NOT NULL | 旧状态 |
| new_status | VARCHAR(20) | NOT NULL | 新状态 |
| auditor_id | BIGINT | | 审核人 |
| audit_notes | TEXT | | 审核备注 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

## 2. 库存管理模块

### 2.1 仓库表 (warehouses)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 仓库ID |
| name | VARCHAR(100) | NOT NULL | 仓库名称 |
| code | VARCHAR(50) | UNIQUE | 仓库编码 |
| address | VARCHAR(255) | | 仓库地址 |
| contact | VARCHAR(50) | | 联系人 |
| phone | VARCHAR(20) | | 联系电话 |
| status | VARCHAR(20) | NOT NULL | 状态(ACTIVE, INACTIVE) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

### 2.2 库存表 (inventory_levels)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 库存ID |
| sku_id | BIGINT | FOREIGN KEY | SKU ID |
| warehouse_id | BIGINT | FOREIGN KEY | 仓库ID |
| quantity | INT | NOT NULL | 库存数量 |
| reserved_quantity | INT | DEFAULT 0 | 已预留数量 |
| last_updated | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 最后更新时间 |

### 2.3 库存变动表 (inventory_movements)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 变动ID |
| sku_id | BIGINT | FOREIGN KEY | SKU ID |
| warehouse_id | BIGINT | FOREIGN KEY | 仓库ID |
| movement_type | VARCHAR(20) | NOT NULL | 变动类型(IN, OUT, ADJUST) |
| quantity | INT | NOT NULL | 变动数量 |
| reference_type | VARCHAR(50) | | 参考类型(如订单、采购单) |
| reference_id | BIGINT | | 参考ID |
| operator_id | BIGINT | | 操作人 |
| notes | TEXT | | 备注 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 2.4 库存锁定表 (inventory_locks)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 锁定ID |
| sku_id | BIGINT | FOREIGN KEY | SKU ID |
| warehouse_id | BIGINT | FOREIGN KEY | 仓库ID |
| order_id | BIGINT | FOREIGN KEY | 订单ID |
| quantity | INT | NOT NULL | 锁定数量 |
| locked_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 锁定时间 |
| expired_at | TIMESTAMP | | 过期时间 |
| status | VARCHAR(20) | NOT NULL | 状态(LOCKED, RELEASED) |

### 2.5 库存预警表 (inventory_alerts)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 预警ID |
| sku_id | BIGINT | FOREIGN KEY | SKU ID |
| warehouse_id | BIGINT | FOREIGN KEY | 仓库ID |
| threshold | INT | NOT NULL | 预警阈值 |
| current_quantity | INT | NOT NULL | 当前数量 |
| status | VARCHAR(20) | NOT NULL | 状态(ACTIVE, RESOLVED) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| resolved_at | TIMESTAMP | | 解决时间 |

## 3. 订单管理模块

### 3.1 订单表 (orders)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 订单ID |
| order_no | VARCHAR(50) | UNIQUE | 订单号 |
| user_id | BIGINT | FOREIGN KEY | 用户ID |
| customer_name | VARCHAR(100) | NOT NULL | 客户名称 |
| customer_phone | VARCHAR(20) | NOT NULL | 客户电话 |
| customer_address | VARCHAR(255) | NOT NULL | 客户地址 |
| total_amount | DECIMAL(10,2) | NOT NULL | 总金额 |
| actual_amount | DECIMAL(10,2) | NOT NULL | 实付金额 |
| payment_status | VARCHAR(20) | NOT NULL | 支付状态(UNPAID, PAID, REFUNDED) |
| order_status | VARCHAR(20) | NOT NULL | 订单状态(PENDING, PAID, SHIPPED, COMPLETED, CANCELLED) |
| payment_method | VARCHAR(50) | | 支付方式 |
| payment_time | TIMESTAMP | | 支付时间 |
| shipping_fee | DECIMAL(10,2) | DEFAULT 0 | 运费 |
| coupon_id | BIGINT | FOREIGN KEY | 优惠券ID |
| discount_amount | DECIMAL(10,2) | DEFAULT 0 | 优惠金额 |
| notes | TEXT | | 订单备注 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

### 3.2 订单项表 (order_items)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 订单项ID |
| order_id | BIGINT | FOREIGN KEY | 订单ID |
| sku_id | BIGINT | FOREIGN KEY | SKU ID |
| product_name | VARCHAR(255) | NOT NULL | 商品名称 |
| sku_attributes | JSONB | NOT NULL | SKU属性 |
| quantity | INT | NOT NULL | 数量 |
| unit_price | DECIMAL(10,2) | NOT NULL | 单价 |
| subtotal | DECIMAL(10,2) | NOT NULL | 小计 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 3.3 订单状态历史表 (order_status_history)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 历史ID |
| order_id | BIGINT | FOREIGN KEY | 订单ID |
| old_status | VARCHAR(20) | | 旧状态 |
| new_status | VARCHAR(20) | NOT NULL | 新状态 |
| operator_id | BIGINT | | 操作人 |
| notes | TEXT | | 备注 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 3.4 购物车表 (shopping_carts)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 购物车ID |
| user_id | BIGINT | FOREIGN KEY | 用户ID |
| sku_id | BIGINT | FOREIGN KEY | SKU ID |
| quantity | INT | NOT NULL | 数量 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

### 3.5 支付表 (payments)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 支付ID |
| order_id | BIGINT | FOREIGN KEY | 订单ID |
| payment_no | VARCHAR(50) | UNIQUE | 支付单号 |
| amount | DECIMAL(10,2) | NOT NULL | 支付金额 |
| payment_method | VARCHAR(50) | NOT NULL | 支付方式 |
| payment_status | VARCHAR(20) | NOT NULL | 支付状态(INIT, SUCCESS, FAILED, REFUNDING, REFUNDED) |
| transaction_id | VARCHAR(100) | | 第三方交易ID |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

### 3.6 物流表 (shipments)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 物流ID |
| order_id | BIGINT | FOREIGN KEY | 订单ID |
| logistics_company | VARCHAR(100) | NOT NULL | 物流公司 |
| tracking_number | VARCHAR(100) | UNIQUE | 物流单号 |
| status | VARCHAR(20) | NOT NULL | 物流状态 |
| estimated_delivery | TIMESTAMP | | 预计送达时间 |
| actual_delivery | TIMESTAMP | | 实际送达时间 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

### 3.7 物流轨迹表 (shipment_tracks)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 轨迹ID |
| shipment_id | BIGINT | FOREIGN KEY | 物流ID |
| status | VARCHAR(50) | NOT NULL | 状态描述 |
| location | VARCHAR(255) | | 地点 |
| description | TEXT | | 详细描述 |
| tracking_time | TIMESTAMP | NOT NULL | 轨迹时间 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

## 4. 营销活动模块

### 4.1 优惠券模板表 (coupon_templates)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 模板ID |
| name | VARCHAR(100) | NOT NULL | 优惠券名称 |
| type | VARCHAR(20) | NOT NULL | 类型(FIXED, PERCENTAGE, CATEGORY) |
| value | DECIMAL(10,2) | NOT NULL | 优惠值 |
| min_spend | DECIMAL(10,2) | DEFAULT 0 | 最低消费 |
| max_discount | DECIMAL(10,2) | | 最大优惠 |
| start_time | TIMESTAMP | NOT NULL | 开始时间 |
| end_time | TIMESTAMP | NOT NULL | 结束时间 |
| usage_limit | INT | | 总使用次数 |
| per_user_limit | INT | | 每人使用次数 |
| status | VARCHAR(20) | NOT NULL | 状态(ACTIVE, INACTIVE) |
| applicable_categories | JSONB | | 适用分类 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 4.2 优惠券表 (coupons)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 优惠券ID |
| template_id | BIGINT | FOREIGN KEY | 模板ID |
| coupon_code | VARCHAR(50) | UNIQUE | 优惠券码 |
| user_id | BIGINT | FOREIGN KEY | 用户ID |
| status | VARCHAR(20) | NOT NULL | 状态(UNUSED, USED, EXPIRED) |
| issued_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 发放时间 |
| used_at | TIMESTAMP | | 使用时间 |
| order_id | BIGINT | FOREIGN KEY | 使用订单ID |

### 4.3 秒杀活动表 (flash_sales)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 活动ID |
| name | VARCHAR(100) | NOT NULL | 活动名称 |
| start_time | TIMESTAMP | NOT NULL | 开始时间 |
| end_time | TIMESTAMP | NOT NULL | 结束时间 |
| status | VARCHAR(20) | NOT NULL | 状态(ACTIVE, INACTIVE) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 4.4 秒杀商品表 (flash_sale_products)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | ID |
| flash_sale_id | BIGINT | FOREIGN KEY | 秒杀活动ID |
| sku_id | BIGINT | FOREIGN KEY | SKU ID |
| flash_price | DECIMAL(10,2) | NOT NULL | 秒杀价格 |
| stock_limit | INT | NOT NULL | 秒杀库存 |
| sold_count | INT | DEFAULT 0 | 已售数量 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 4.5 拼团活动表 (group_buys)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 活动ID |
| name | VARCHAR(100) | NOT NULL | 活动名称 |
| start_time | TIMESTAMP | NOT NULL | 开始时间 |
| end_time | TIMESTAMP | NOT NULL | 结束时间 |
| group_size | INT | NOT NULL | 拼团人数 |
| group_price | DECIMAL(10,2) | NOT NULL | 拼团价格 |
| single_price | DECIMAL(10,2) | NOT NULL | 单独购买价格 |
| status | VARCHAR(20) | NOT NULL | 状态(ACTIVE, INACTIVE) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 4.6 拼团详情表 (group_buy_details)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 拼团ID |
| group_buy_id | BIGINT | FOREIGN KEY | 拼团活动ID |
| sku_id | BIGINT | FOREIGN KEY | SKU ID |
| group_no | VARCHAR(50) | UNIQUE | 拼团编号 |
| leader_id | BIGINT | FOREIGN KEY | 团长ID |
| current_members | INT | DEFAULT 1 | 当前人数 |
| status | VARCHAR(20) | NOT NULL | 状态(PENDING, SUCCESS, FAILED) |
| expired_at | TIMESTAMP | NOT NULL | 过期时间 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 4.7 拼团成员表 (group_buy_members)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 成员ID |
| group_detail_id | BIGINT | FOREIGN KEY | 拼团详情ID |
| user_id | BIGINT | FOREIGN KEY | 用户ID |
| role | VARCHAR(20) | NOT NULL | 角色(LEADER, MEMBER) |
| joined_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 加入时间 |

### 4.8 砍价活动表 (bargain_activities)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 活动ID |
| name | VARCHAR(100) | NOT NULL | 活动名称 |
| sku_id | BIGINT | FOREIGN KEY | SKU ID |
| original_price | DECIMAL(10,2) | NOT NULL | 原价 |
| min_price | DECIMAL(10,2) | NOT NULL | 最低价 |
| start_time | TIMESTAMP | NOT NULL | 开始时间 |
| end_time | TIMESTAMP | NOT NULL | 结束时间 |
| status | VARCHAR(20) | NOT NULL | 状态(ACTIVE, INACTIVE) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 4.9 砍价详情表 (bargain_details)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 砍价ID |
| activity_id | BIGINT | FOREIGN KEY | 砍价活动ID |
| user_id | BIGINT | FOREIGN KEY | 发起用户ID |
| current_price | DECIMAL(10,2) | NOT NULL | 当前价格 |
| status | VARCHAR(20) | NOT NULL | 状态(ONGOING, SUCCESS, FAILED) |
| expired_at | TIMESTAMP | NOT NULL | 过期时间 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| completed_at | TIMESTAMP | | 完成时间 |

### 4.10 砍价记录表 (bargain_records)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 记录ID |
| bargain_id | BIGINT | FOREIGN KEY | 砍价详情ID |
| helper_id | BIGINT | FOREIGN KEY | 帮助用户ID |
| discount_amount | DECIMAL(10,2) | NOT NULL | 砍价金额 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 4.11 满减活动表 (full_discounts)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 活动ID |
| name | VARCHAR(100) | NOT NULL | 活动名称 |
| start_time | TIMESTAMP | NOT NULL | 开始时间 |
| end_time | TIMESTAMP | NOT NULL | 结束时间 |
| status | VARCHAR(20) | NOT NULL | 状态(ACTIVE, INACTIVE) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 4.12 满减规则表 (full_discount_rules)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 规则ID |
| discount_id | BIGINT | FOREIGN KEY | 满减活动ID |
| min_amount | DECIMAL(10,2) | NOT NULL | 满金额 |
| discount_amount | DECIMAL(10,2) | NOT NULL | 减金额 |
| sort_order | INT | DEFAULT 0 | 排序 |

## 5. 售后退款模块

### 5.1 退款申请表 (refund_applications)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 申请ID |
| order_id | BIGINT | FOREIGN KEY | 订单ID |
| user_id | BIGINT | FOREIGN KEY | 用户ID |
| refund_type | VARCHAR(20) | NOT NULL | 退款类型(REFUND_ONLY, RETURN_REFUND) |
| refund_amount | DECIMAL(10,2) | NOT NULL | 退款金额 |
| reason | TEXT | NOT NULL | 退款原因 |
| status | VARCHAR(20) | NOT NULL | 状态(PENDING, APPROVED, REJECTED, PROCESSING, COMPLETED, FAILED) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

### 5.2 退款审核表 (refund_audits)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 审核ID |
| application_id | BIGINT | FOREIGN KEY | 退款申请ID |
| auditor_id | BIGINT | | 审核人 |
| audit_level | INT | NOT NULL | 审核层级 |
| audit_result | VARCHAR(20) | NOT NULL | 审核结果(APPROVED, REJECTED) |
| audit_notes | TEXT | | 审核备注 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 5.3 退款详情表 (refund_details)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 详情ID |
| application_id | BIGINT | FOREIGN KEY | 退款申请ID |
| order_item_id | BIGINT | FOREIGN KEY | 订单项ID |
| quantity | INT | NOT NULL | 退款数量 |
| refund_amount | DECIMAL(10,2) | NOT NULL | 退款金额 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

## 6. 分销裂变模块

### 6.1 分销员表 (distributors)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 分销员ID |
| user_id | BIGINT | FOREIGN KEY | 用户ID |
| distributor_code | VARCHAR(50) | UNIQUE | 分销码 |
| status | VARCHAR(20) | NOT NULL | 状态(ACTIVE, INACTIVE) |
| total_commission | DECIMAL(10,2) | DEFAULT 0 | 总佣金 |
| available_commission | DECIMAL(10,2) | DEFAULT 0 | 可用佣金 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

### 6.2 分销关系表 (distribution_relations)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 关系ID |
| distributor_id | BIGINT | FOREIGN KEY | 分销员ID |
| referred_user_id | BIGINT | FOREIGN KEY | 推荐用户ID |
| level | INT | NOT NULL | 分销层级 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 6.3 佣金记录表 (commission_records)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 记录ID |
| distributor_id | BIGINT | FOREIGN KEY | 分销员ID |
| order_id | BIGINT | FOREIGN KEY | 订单ID |
| amount | DECIMAL(10,2) | NOT NULL | 佣金金额 |
| commission_rate | DECIMAL(5,2) | NOT NULL | 佣金比例 |
| status | VARCHAR(20) | NOT NULL | 状态(PENDING, SETTLED, CANCELLED) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| settled_at | TIMESTAMP | | 结算时间 |

### 6.4 推广数据表 (promotion_data)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 数据ID |
| distributor_id | BIGINT | FOREIGN KEY | 分销员ID |
| date | DATE | NOT NULL | 日期 |
| new_users | INT | DEFAULT 0 | 新增用户 |
| orders_count | INT | DEFAULT 0 | 订单数 |
| sales_amount | DECIMAL(10,2) | DEFAULT 0 | 销售额 |
| commission_amount | DECIMAL(10,2) | DEFAULT 0 | 佣金金额 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

## 7. 用户权限模块

### 7.1 用户表 (users)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 用户ID |
| username | VARCHAR(50) | UNIQUE | 用户名 |
| password | VARCHAR(255) | NOT NULL | 密码 |
| name | VARCHAR(100) | NOT NULL | 姓名 |
| phone | VARCHAR(20) | UNIQUE | 电话 |
| email | VARCHAR(100) | UNIQUE | 邮箱 |
| role_id | BIGINT | FOREIGN KEY | 角色ID |
| department_id | BIGINT | | 部门ID |
| status | VARCHAR(20) | NOT NULL | 状态(ACTIVE, INACTIVE) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

### 7.2 角色表 (roles)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 角色ID |
| name | VARCHAR(50) | NOT NULL | 角色名称 |
| description | TEXT | | 角色描述 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

### 7.3 权限表 (permissions)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 权限ID |
| name | VARCHAR(100) | NOT NULL | 权限名称 |
| code | VARCHAR(50) | UNIQUE | 权限编码 |
| type | VARCHAR(20) | NOT NULL | 类型(MENU, BUTTON, API) |
| parent_id | BIGINT | | 父权限ID |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

### 7.4 角色权限关系表 (role_permissions)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| role_id | BIGINT | PRIMARY KEY | 角色ID |
| permission_id | BIGINT | PRIMARY KEY | 权限ID |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 7.5 用户角色关系表 (user_roles)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| user_id | BIGINT | PRIMARY KEY | 用户ID |
| role_id | BIGINT | PRIMARY KEY | 角色ID |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

## 8. 系统配置模块

### 8.1 系统配置表 (system_configs)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 配置ID |
| config_key | VARCHAR(100) | UNIQUE | 配置键 |
| config_value | TEXT | NOT NULL | 配置值 |
| description | TEXT | | 配置描述 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

### 8.2 功能开关表 (feature_flags)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 开关ID |
| feature_name | VARCHAR(100) | UNIQUE | 功能名称 |
| enabled | BOOLEAN | DEFAULT false | 是否启用 |
| description | TEXT | | 功能描述 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

### 8.3 操作日志表 (operation_logs)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 日志ID |
| user_id | BIGINT | FOREIGN KEY | 用户ID |
| operation_type | VARCHAR(50) | NOT NULL | 操作类型 |
| module | VARCHAR(100) | NOT NULL | 操作模块 |
| description | TEXT | NOT NULL | 操作描述 |
| ip_address | VARCHAR(50) | | IP地址 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 8.4 业务日志表 (business_logs)
| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| id | BIGINT | PRIMARY KEY | 日志ID |
| business_type | VARCHAR(50) | NOT NULL | 业务类型 |
| business_id | BIGINT | | 业务ID |
| description | TEXT | NOT NULL | 业务描述 |
| level | VARCHAR(20) | NOT NULL | 日志级别 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

## 9. 索引设计

### 9.1 商品模块索引
- products: (product_code), (category_id), (status), (created_at)
- product_categories: (parent_id), (level), (status)
- product_skus: (sku_code), (product_id), (stock)
- inventory_levels: (sku_id, warehouse_id), (quantity)

### 9.2 订单模块索引
- orders: (order_no), (user_id), (order_status), (created_at)
- order_items: (order_id), (sku_id)
- payments: (order_id), (payment_status), (created_at)
- shipments: (order_id), (tracking_number), (status)

### 9.3 营销模块索引
- coupons: (coupon_code), (user_id, status)
- flash_sales: (start_time, end_time, status)
- group_buys: (start_time, end_time, status)
- bargain_activities: (start_time, end_time, status)

### 9.4 分销模块索引
- distributors: (user_id), (distributor_code)
- distribution_relations: (distributor_id), (referred_user_id)
- commission_records: (distributor_id), (order_id), (status)

## 10. 数据库初始化脚本

### 10.1 初始数据
- 系统管理员用户
- 基础商品分类
- 系统默认配置
- 初始角色和权限
- 仓库信息