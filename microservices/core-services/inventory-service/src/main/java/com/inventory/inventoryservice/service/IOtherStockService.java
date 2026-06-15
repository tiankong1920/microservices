package com.inventory.inventoryservice.service;

import com.inventory.inventoryservice.dto.OtherStockInOrderDTO;
import com.inventory.inventoryservice.dto.OtherStockOutOrderDTO;

import java.util.List;

/**
 * 其他库存服务接口
 *
 * <p>定义其他库存相关的业务操作接口，包括其他入库和出库订单的管理。
 * 所有实现类必须保证线程安全，并支持事务管理。</p>
 *
 * <p>主要功能：
 * <ul>
 *   <li>其他入库订单管理：其他入库订单的创建、查询、删除</li>
 *   <li>其他出库订单管理：其他出库订单的创建、查询、删除</li>
 *   <li>库存调整：通过其他入库和出库调整库存</li>
 * </ul></p>
 *
 * @author Inventory Team
 * @since 3.0.0
 * @version 5.0
 * @see OtherStockInOrderDTO
 * @see OtherStockOutOrderDTO
 */
public interface IOtherStockService {

    /**
     * 查询所有其他入库订单
     *
     * @return 所有其他入库订单的列表
     */
    List<OtherStockInOrderDTO> getAllOtherStockInOrders();

    /**
     * 查询所有其他出库订单
     *
     * @return 所有其他出库订单的列表
     */
    List<OtherStockOutOrderDTO> getAllOtherStockOutOrders();

    /**
     * 根据其他入库订单ID查询订单
     *
     * @param id 其他入库订单ID
     * @return 指定ID的其他入库订单详细信息
     */
    OtherStockInOrderDTO getOtherStockInOrderById(Long id);

    /**
     * 根据其他出库订单ID查询订单
     *
     * @param id 其他出库订单ID
     * @return 指定ID的其他出库订单详细信息
     */
    OtherStockOutOrderDTO getOtherStockOutOrderById(Long id);

    /**
     * 创建新其他入库订单
     *
     * @param orderDTO 要创建的其他入库订单数据
     * @return 创建后的其他入库订单信息
     */
    OtherStockInOrderDTO createOtherStockInOrder(OtherStockInOrderDTO orderDTO);

    /**
     * 创建新其他出库订单
     *
     * @param orderDTO 要创建的其他出库订单数据
     * @return 创建后的其他出库订单信息
     */
    OtherStockOutOrderDTO createOtherStockOutOrder(OtherStockOutOrderDTO orderDTO);

    /**
     * 删除其他入库订单
     *
     * @param id 其他入库订单ID
     */
    void deleteOtherStockInOrder(Long id);

    /**
     * 删除其他出库订单
     *
     * @param id 其他出库订单ID
     */
    void deleteOtherStockOutOrder(Long id);
}
