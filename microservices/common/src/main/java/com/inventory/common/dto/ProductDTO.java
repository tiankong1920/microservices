package com.inventory.common.dto;

import java.util.Map;

/**
 * 通用产品数据传输对象，用于Feign客户端.
 *
 * <p>在没有具体DTO定义时，使用Map结构传递数据。</p>
 *
 * @author Inventory Team
 * @version 5.0
 * @since 3.0.0
 */
public class ProductDTO {

    /** 产品数据. */
    private Map<String, Object> data;

    /**
     * 默认构造函数.
     */
    public ProductDTO() {
    }

    /**
     * 带参数的构造函数.
     *
     * @param data 产品数据
     */
    public ProductDTO(final Map<String, Object> data) {
        this.data = data;
    }

    /**
     * 获取产品数据.
     *
     * @return 产品数据
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * 设置产品数据.
     *
     * @param data 产品数据
     */
    public void setData(final Map<String, Object> data) {
        this.data = data;
    }
}
