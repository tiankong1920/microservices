-- 创建客户信息表
CREATE TABLE IF NOT EXISTS tb_customer (
    customer_code VARCHAR(20) PRIMARY KEY COMMENT '客户编码',
    customer_name VARCHAR(100) NOT NULL COMMENT '客户名称',
    contact_person VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    address TEXT COMMENT '地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用状态(1:启用, 0:禁用)',
    ext1 VARCHAR(50) COMMENT '扩展字段1',
    ext2 VARCHAR(50) COMMENT '扩展字段2',
    ext3 TEXT COMMENT '扩展字段3',
    INDEX idx_customer_name (customer_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户信息表';

-- 创建物料清单表
CREATE TABLE IF NOT EXISTS tb_bom (
    material_code VARCHAR(30) PRIMARY KEY COMMENT '物料编码',
    material_name VARCHAR(100) NOT NULL COMMENT '物料名称',
    spec_model VARCHAR(100) COMMENT '规格型号',
    unit VARCHAR(10) NOT NULL COMMENT '单位',
    quantity DECIMAL(18,4) NOT NULL COMMENT '数量',
    parent_material_code VARCHAR(30) COMMENT '父项物料编码',
    material_type VARCHAR(20) NOT NULL COMMENT '物料类型(成品、半成品、原材料、辅料)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用状态(1:启用, 0:禁用)',
    version INT DEFAULT 1 COMMENT '版本号(乐观锁控制)',
    ext1 VARCHAR(50) COMMENT '扩展字段1',
    ext2 VARCHAR(50) COMMENT '扩展字段2',
    ext3 TEXT COMMENT '扩展字段3',
    INDEX idx_parent_material (parent_material_code),
    INDEX idx_material_type_enabled (material_type, is_enabled),
    INDEX idx_material_name (material_name),
    FOREIGN KEY (parent_material_code) REFERENCES tb_bom(material_code) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料清单表';

-- 创建订单表
CREATE TABLE IF NOT EXISTS tb_order (
    order_no VARCHAR(20) PRIMARY KEY COMMENT '订单号',
    order_date DATETIME NOT NULL COMMENT '订单日期',
    customer_code VARCHAR(20) NOT NULL COMMENT '客户编码',
    customer_name VARCHAR(100) NOT NULL COMMENT '客户名称',
    bom_material_code VARCHAR(30) NOT NULL COMMENT '关联BOM物料编码',
    order_quantity DECIMAL(18,4) NOT NULL CHECK (order_quantity > 0) COMMENT '订单数量',
    expected_delivery_date DATE COMMENT '预计交付日期',
    order_status VARCHAR(20) NOT NULL COMMENT '订单状态(待处理、已确认、生产中、已发货、已完成、已取消)',
    order_priority INT DEFAULT 2 COMMENT '订单优先级(1:低, 2:中, 3:高)',
    create_user VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_user VARCHAR(50) COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark TEXT COMMENT '备注信息',
    version INT DEFAULT 1 COMMENT '版本号(乐观锁控制)',
    ext1 VARCHAR(50) COMMENT '扩展字段1',
    ext2 VARCHAR(50) COMMENT '扩展字段2',
    ext3 TEXT COMMENT '扩展字段3',
    INDEX idx_customer_order_date_status (customer_code, order_date, order_status),
    INDEX idx_order_status_delivery (order_status, expected_delivery_date),
    INDEX idx_bom_material (bom_material_code),
    FOREIGN KEY (customer_code) REFERENCES tb_customer(customer_code) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (bom_material_code) REFERENCES tb_bom(material_code) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 创建销售表
CREATE TABLE IF NOT EXISTS tb_sales (
    sales_no VARCHAR(20) PRIMARY KEY COMMENT '销售单号',
    order_no VARCHAR(20) NOT NULL COMMENT '关联订单号',
    customer_code VARCHAR(20) NOT NULL COMMENT '客户编码',
    customer_name VARCHAR(100) NOT NULL COMMENT '客户名称',
    sales_date DATETIME NOT NULL COMMENT '销售日期',
    bom_material_code VARCHAR(30) NOT NULL COMMENT '关联BOM物料编码',
    sales_quantity DECIMAL(18,4) NOT NULL CHECK (sales_quantity > 0) COMMENT '销售数量',
    unit_price DECIMAL(18,2) NOT NULL CHECK (unit_price > 0) COMMENT '单价',
    amount DECIMAL(18,2) NOT NULL COMMENT '金额',
    sales_status VARCHAR(20) NOT NULL COMMENT '销售状态(待付款、已付款、已发货、已完成、已取消)',
    sales_person VARCHAR(50) COMMENT '销售人员',
    remark TEXT COMMENT '备注信息',
    create_user VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_user VARCHAR(50) COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    version INT DEFAULT 1 COMMENT '版本号(乐观锁控制)',
    ext1 VARCHAR(50) COMMENT '扩展字段1',
    ext2 VARCHAR(50) COMMENT '扩展字段2',
    ext3 TEXT COMMENT '扩展字段3',
    INDEX idx_customer_sales_date (customer_code, sales_date),
    INDEX idx_sales_status_date (sales_status, sales_date),
    INDEX idx_order_no (order_no),
    INDEX idx_bom_material (bom_material_code),
    FOREIGN KEY (order_no) REFERENCES tb_order(order_no) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (customer_code) REFERENCES tb_customer(customer_code) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (bom_material_code) REFERENCES tb_bom(material_code) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售表';

-- 创建销售表金额自动计算触发器
DELIMITER //
CREATE TRIGGER IF NOT EXISTS trg_tb_sales_amount BEFORE INSERT ON tb_sales FOR EACH ROW
BEGIN
    SET NEW.amount = NEW.sales_quantity * NEW.unit_price;
END//

CREATE TRIGGER IF NOT EXISTS trg_tb_sales_amount_update BEFORE UPDATE ON tb_sales FOR EACH ROW
BEGIN
    SET NEW.amount = NEW.sales_quantity * NEW.unit_price;
END//
DELIMITER ;

-- 创建BOM层级展开查询存储过程
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS sp_bom_expand(IN p_material_code VARCHAR(30), IN p_level INT)
BEGIN
    WITH RECURSIVE bom_tree AS (
        SELECT 
            material_code, 
            material_name, 
            spec_model, 
            unit, 
            quantity, 
            parent_material_code, 
            material_type, 
            1 AS level
        FROM tb_bom
        WHERE material_code = p_material_code AND is_enabled = 1
        UNION ALL
        SELECT 
            b.material_code, 
            b.material_name, 
            b.spec_model, 
            b.unit, 
            b.quantity, 
            b.parent_material_code, 
            b.material_type, 
            bt.level + 1 AS level
        FROM tb_bom b
        INNER JOIN bom_tree bt ON b.parent_material_code = bt.material_code
        WHERE bt.level < p_level AND b.is_enabled = 1
    )
    SELECT * FROM bom_tree ORDER BY level, material_code;
END//
DELIMITER ;
