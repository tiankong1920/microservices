package com.inventory.salesservice.service;

import com.inventory.salesservice.dto.RetailOrderDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 零售服务接口
 *
 * <p>定义零售相关的业务操作接口，包括零售订单的增删改查、支付状态管理等。
 * 所有实现类必须保证线程安全，并支持事务管理。</p>
 *
 * <p>主要功能：
 * <ul>
 *   <li>零售订单管理：零售订单的创建、查询、更新、删除</li>
 *   <li>零售订单查询：按ID查询、按零售单号查询、按客户查询、按仓库查询、按支付状态查询</li>
 *   <li>支付状态管理：支付状态的更新</li>
 *   <li>零售统计：客户零售总额统计</li>
 * </ul></p>
 *
 * @author Inventory Team
 * @since 3.0.0
 * @version 5.0
 * @see RetailOrderDTO
 */
public interface IRetailService {

    /**
     * 查询所有零售订单
     *
     * @return 所有零售订单的列表
     */
    List<RetailOrderDTO> getAllRetailOrders();

    /**
     * 根据零售订单ID查询订单
     *
     * @param id 零售订单ID
     * @return 指定ID的零售订单详细信息
     */
    RetailOrderDTO getRetailOrderById(Long id);

    /**
     * 根据零售单号查询订单
     *
     * @param retailNumber 零售单号
     * @return 指定零售单号的订单详细信息
     */
    RetailOrderDTO getRetailOrderByRetailNumber(String retailNumber);

    /**
     * 根据客户ID查询零售订单
     *
     * @param customerId 客户ID
     * @return 指定客户的所有零售订单列表
     */
    List<RetailOrderDTO> getRetailOrdersByCustomerId(Long customerId);

    /**
     * 根据仓库ID查询零售订单
     *
     * @param warehouseId 仓库ID
     * @return 指定仓库的所有零售订单列表
     */
    List<RetailOrderDTO> getRetailOrdersByWarehouseId(Long warehouseId);

    /**
     * 根据支付状态查询零售订单
     *
     * @param paymentStatus 支付状态
     * @return 指定支付状态的所有零售订单列表
     */
    List<RetailOrderDTO> getRetailOrdersByPaymentStatus(String paymentStatus);

    /**
     * 根据日期范围查询零售订单
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 指定日期范围内的所有零售订单列表
     */
    List<RetailOrderDTO> getRetailOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 创建新零售订单
     *
     * @param retailOrderDTO 要创建的零售订单数据
     * @return 创建后的零售订单信息
     */
    RetailOrderDTO createRetailOrder(RetailOrderDTO retailOrderDTO);

    /**
     * 更新零售订单
     *
     * @param id 零售订单ID
     * @param retailOrderDTO 更新的零售订单数据
     * @return 更新后的零售订单信息
     */
    RetailOrderDTO updateRetailOrder(Long id, RetailOrderDTO retailOrderDTO);

    /**
     * 删除零售订单
     *
     * @param id 零售订单ID
     */
    void deleteRetailOrder(Long id);

    /**
     * 更新零售订单支付状态
     *
     * @param id 零售订单ID
     * @param paymentStatus 新支付状态
     * @return 更新后的零售订单信息
     */
    RetailOrderDTO updateRetailOrderPaymentStatus(Long id, String paymentStatus);

    /**
     * 计算客户零售总额
     *
     * @param customerId 客户ID
     * @return 客户的零售总额
     */
    Double getTotalRetailByCustomerId(Long customerId);
}
