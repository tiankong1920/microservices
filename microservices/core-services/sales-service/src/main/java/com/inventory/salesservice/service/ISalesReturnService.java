package com.inventory.salesservice.service;

import com.inventory.salesservice.dto.SalesReturnOrderDTO;

import java.util.List;

/**
 * 销售退货服务接口
 *
 * <p>定义销售退货相关的业务操作接口，包括销售退货订单的增删改查、状态管理等。
 * 所有实现类必须保证线程安全，并支持事务管理。</p>
 *
 * <p>主要功能：
 * <ul>
 *   <li>销售退货订单管理：退货订单的创建、查询、更新、删除</li>
 *   <li>退货订单查询：按ID查询、按退货单号查询、按客户查询、按仓库查询、按状态查询</li>
 *   <li>退货订单状态管理：订单状态的更新和流转</li>
 *   <li>退货统计：客户退货总额统计</li>
 * </ul></p>
 *
 * @author Inventory Team
 * @since 3.0.0
 * @version 5.0
 * @see SalesReturnOrderDTO
 */
public interface ISalesReturnService {

    /**
     * 查询所有销售退货订单
     *
     * @return 所有销售退货订单的列表
     */
    List<SalesReturnOrderDTO> getAllSalesReturnOrders();

    /**
     * 根据退货订单ID查询退货订单
     *
     * @param id 退货订单ID
     * @return 指定ID的退货订单详细信息
     */
    SalesReturnOrderDTO getSalesReturnOrderById(Long id);

    /**
     * 根据退货单号查询退货订单
     *
     * @param returnNumber 退货单号
     * @return 指定退货单号的订单详细信息
     */
    SalesReturnOrderDTO getSalesReturnOrderByReturnNumber(String returnNumber);

    /**
     * 根据客户ID查询退货订单
     *
     * @param customerId 客户ID
     * @return 指定客户的所有退货订单列表
     */
    List<SalesReturnOrderDTO> getSalesReturnOrdersByCustomerId(Long customerId);

    /**
     * 根据仓库ID查询退货订单
     *
     * @param warehouseId 仓库ID
     * @return 指定仓库的所有退货订单列表
     */
    List<SalesReturnOrderDTO> getSalesReturnOrdersByWarehouseId(Long warehouseId);

    /**
     * 根据状态查询退货订单
     *
     * @param status 订单状态
     * @return 指定状态的所有退货订单列表
     */
    List<SalesReturnOrderDTO> getSalesReturnOrdersByStatus(String status);

    /**
     * 根据原始订单ID查询退货订单
     *
     * @param originalOrderId 原始订单ID
     * @return 指定原始订单的所有退货订单列表
     */
    List<SalesReturnOrderDTO> getSalesReturnOrdersByOriginalOrderId(Long originalOrderId);

    /**
     * 创建新销售退货订单
     *
     * @param salesReturnOrderDTO 要创建的退货订单数据
     * @return 创建后的退货订单信息
     */
    SalesReturnOrderDTO createSalesReturnOrder(SalesReturnOrderDTO salesReturnOrderDTO);

    /**
     * 更新销售退货订单
     *
     * @param id 退货订单ID
     * @param salesReturnOrderDTO 更新的退货订单数据
     * @return 更新后的退货订单信息
     */
    SalesReturnOrderDTO updateSalesReturnOrder(Long id, SalesReturnOrderDTO salesReturnOrderDTO);

    /**
     * 删除销售退货订单
     *
     * @param id 退货订单ID
     */
    void deleteSalesReturnOrder(Long id);

    /**
     * 更新退货订单状态
     *
     * @param id 退货订单ID
     * @param status 新状态
     * @return 更新后的退货订单信息
     */
    SalesReturnOrderDTO updateSalesReturnOrderStatus(Long id, String status);

    /**
     * 计算客户退货总额
     *
     * @param customerId 客户ID
     * @return 客户的退货总额
     */
    Double getTotalReturnByCustomerId(Long customerId);
}
