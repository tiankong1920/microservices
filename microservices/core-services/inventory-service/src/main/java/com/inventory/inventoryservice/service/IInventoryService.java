package com.inventory.inventoryservice.service;

import com.inventory.inventoryservice.dto.InventoryDTO;
import com.inventory.inventoryservice.exception.InventoryNotFoundException;

import java.util.List;

/**
 * 库存服务接口
 *
 * <p>定义库存相关的业务操作接口，包括库存的增删改查、库存转移、批次管理等。
 * 所有实现类必须保证线程安全，并支持事务管理。</p>
 *
 * <p>使用说明：
 * <ul>
 *   <li>通过Spring依赖注入获取实例</li>
 *   <li>所有方法都必须在事务上下文中调用</li>
 *   <li>查询方法不会抛出业务异常</li>
 *   <li>修改方法可能会抛出业务异常</li>
 * </ul></p>
 *
 * <p>主要功能：
 * <ul>
 *   <li>库存信息管理：库存的创建、查询、更新、删除</li>
 *   <li>库存查询：按产品查询、按仓库查询、按批次查询、按状态查询</li>
 *   <li>库存预留：预留库存数量，支持订单创建</li>
 *   <li>库存释放：释放预留的库存数量，支持订单取消</li>
 *   <li>库存调整：调整库存数量，支持盘点和库存修正</li>
 *   <li>批次管理：批次的创建、查询、更新、删除</li>
 *   <li>仓库管理：仓库的增删改查</li>
 * </ul></p>
 *
 * @author Inventory Team
 * @since 3.0.0
 * @version 5.0
 * @see InventoryDTO
 * @see InventoryServiceImpl
 */
public interface IInventoryService {

    /**
     * 查询所有库存记录
     *
     * <p>从数据库中检索所有库存记录，返回库存列表。
     * 该方法不会抛出业务异常，如果数据库中没有库存记录，返回空列表。</p>
     *
     * <p>性能考虑：
     * <ul>
     *   <li>该方法会查询数据库中的所有库存记录，可能返回大量数据</li>
     *   <li>建议使用分页查询方法以避免性能问题</li>
     *   <li>结果会被缓存，后续查询会直接从缓存获取</li>
     * </ul></p>
     *
     * @return 所有库存记录的列表，以InventoryDTO对象形式返回，如果数据库中没有库存记录则返回空列表
     * @see InventoryDTO
     * @since 3.0.0
     */
    List<InventoryDTO> getAllInventory();

    /**
     * 根据库存ID查询库存记录
     *
     * <p>根据库存记录的唯一标识符查询库存详细信息。
     * 如果库存记录不存在，抛出InventoryNotFoundException异常。</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>查看库存详情页面</li>
     *   <li>编辑库存信息</li>
     *   <li>删除库存记录前确认</li>
     * </ul></p>
     *
     * @param id 库存ID，必填，不能为null，必须大于0
     * @return 指定ID的库存记录详细信息，以InventoryDTO对象形式返回
     * @throws InventoryNotFoundException 当库存ID不存在时抛出
     * @throws IllegalArgumentException 当id为null或小于等于0时抛出
     * @see InventoryDTO
     * @see InventoryNotFoundException
     * @since 3.0.0
     */
    InventoryDTO getInventoryById(Long id);

    /**
     * 根据产品和仓库查询库存记录
     *
     * <p>根据产品ID和仓库ID查询库存详细信息。
     * 如果库存记录不存在，抛出InventoryNotFoundException异常。</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>查询特定产品在特定仓库的库存</li>
     *   <li>库存分配和调拨</li>
     *   <li>库存统计和分析</li>
     * </ul></p>
     *
     * @param productId 产品ID，必填，不能为null，必须大于0
     * @param warehouseId 仓库ID，必填，不能为null，必须大于0
     * @return 匹配产品和仓库的库存记录，以InventoryDTO对象形式返回
     * @throws InventoryNotFoundException 当库存记录不存在时抛出
     * @throws IllegalArgumentException 当productId为null或小于等于0时抛出
     * @throws IllegalArgumentException 当warehouseId为null或小于等于0时抛出
     * @see InventoryDTO
     * @see InventoryNotFoundException
     * @since 3.0.0
     */
    InventoryDTO getInventoryByProductAndWarehouse(Long productId, Long warehouseId);

    /**
     * 根据产品ID查询库存记录
     *
     * <p>根据产品ID查询所有仓库的库存记录。
     * 如果没有库存记录，返回空列表。</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>查询产品在所有仓库的库存分布</li>
     *   <li>库存调拨和转移</li>
     *   <li>库存统计和分析</li>
     * </ul></p>
     *
     * @param productId 产品ID，必填，不能为null，必须大于0
     * @return 指定产品的所有库存记录列表，以InventoryDTO对象形式返回
     * @throws IllegalArgumentException 当productId为null或小于等于0时抛出
     * @see InventoryDTO
     * @since 3.0.0
     */
    List<InventoryDTO> getInventoryByProductId(Long productId);

    /**
     * 根据仓库ID查询库存记录
     *
     * <p>根据仓库ID查询所有产品的库存记录。
     * 如果没有库存记录，返回空列表。</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>查询仓库中所有产品的库存</li>
     *   <li>库存调拨和转移</li>
     *   <li>库存统计和分析</li>
     * </ul></p>
     *
     * @param warehouseId 仓库ID，必填，不能为null，必须大于0
     * @return 指定仓库的所有库存记录列表，以InventoryDTO对象形式返回
     * @throws IllegalArgumentException 当warehouseId为null或小于等于0时抛出
     * @see InventoryDTO
     * @since 3.0.0
     */
    List<InventoryDTO> getInventoryByWarehouseId(Long warehouseId);

    /**
     * 根据批次ID查询库存记录
     *
     * <p>根据批次ID查询所有库存记录。
     * 如果没有库存记录，返回空列表。</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>查询批次中的所有库存记录</li>
     *   <li>批次管理和盘点</li>
     *   <li>库存统计和分析</li>
     * </ul></p>
     *
     * @param batchId 批次ID，必填，不能为null，必须大于0
     * @return 指定批次的所有库存记录列表，以InventoryDTO对象形式返回
     * @throws IllegalArgumentException 当batchId为null或小于等于0时抛出
     * @see InventoryDTO
     * @since 3.0.0
     */
    List<InventoryDTO> getInventoryByBatchId(Long batchId);

    /**
     * 根据状态查询库存记录
     *
     * <p>根据库存状态查询所有库存记录。
     * 如果没有库存记录，返回空列表。</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>查询特定状态的库存记录</li>
     *   <li>库存状态统计和分析</li>
     *   <li>库存状态流转监控</li>
     * </ul></p>
     *
     * @param status 库存状态，必填，不能为null或空字符串
     * @return 指定状态的所有库存记录列表，以InventoryDTO对象形式返回
     * @throws IllegalArgumentException 当status为null或空字符串时抛出
     * @see InventoryDTO
     * @since 3.0.0
     */
    List<InventoryDTO> getInventoryByStatus(String status);

    /**
     * 创建库存记录
     *
     * <p>在数据库中创建新库存记录，并返回创建后的库存信息。
     * 创建成功后，库存记录会自动生成唯一ID。</p>
     *
     * <p>业务规则：
     * <ul>
     *   <li>产品ID必须存在</li>
     *   <li>仓库ID必须存在</li>
     *   <li>库存数量必须大于0</li>
     *   <li>创建的库存默认状态为"可用"</li>
     * </ul></p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>库存信息录入</li>
     *   <li>批量导入库存</li>
     *   <li>库存复制功能</li>
     * </ul></p>
     *
     * @param inventoryDTO 要创建的库存数据，必填，不能为null
     * @return 创建后的库存记录信息，包含自动生成的ID，以InventoryDTO对象形式返回
     * @throws IllegalArgumentException 当inventoryDTO为null时抛出
     * @throws IllegalArgumentException 当产品ID不存在时抛出
     * @throws IllegalArgumentException 当仓库ID不存在时抛出
     * @throws IllegalArgumentException 当库存数量小于等于0时抛出
     * @see InventoryDTO
     * @since 3.0.0
     */
    InventoryDTO createInventory(InventoryDTO inventoryDTO);

    /**
     * 更新库存记录
     *
     * <p>根据库存ID更新库存记录的详细信息。
     * 只更新提供的字段，未提供的字段保持不变。</p>
     *
     * <p>业务规则：
     * <ul>
     *   <li>只能更新已存在的库存记录</li>
     *   <li>更新操作会重新计算可用数量</li>
     *   <li>更新操作会清除库存缓存</li>
     * </ul></p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>编辑库存信息</li>
     *   <li>调整库存数量</li>
     *   <li>修改库存状态</li>
     * </ul></p>
     *
     * @param id 库存ID，必填，不能为null，必须大于0
     * @param inventoryDTO 更新的库存数据，必填，不能为null
     * @return 更新后的库存记录信息，以InventoryDTO对象形式返回
     * @throws InventoryNotFoundException 当库存ID不存在时抛出
     * @throws IllegalArgumentException 当id为null或小于等于0时抛出
     * @throws IllegalArgumentException 当inventoryDTO为null时抛出
     * @see InventoryDTO
     * @since 3.0.0
     */
    InventoryDTO updateInventory(Long id, InventoryDTO inventoryDTO);

    /**
     * 删除库存记录
     *
     * <p>根据库存ID从数据库中删除库存记录。
     * 删除操作是物理删除，不可恢复。</p>
     *
     * <p>业务规则：
     * <ul>
     *   <li>只能删除已存在的库存记录</li>
     *   <li>删除操作会级联删除相关的批次信息</li>
     *   <li>删除操作会清除库存缓存</li>
     * </ul></p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>删除错误录入的库存记录</li>
     *   <li>清理不再需要的库存记录</li>
     *   <li>库存数据清理</li>
     * </ul></p>
     *
     * <p>安全考虑：
     * <ul>
     *   <li>删除操作不可逆，请谨慎操作</li>
     *   <li>建议先检查库存记录是否被订单引用</li>
     *   <li>建议使用软删除而非物理删除</li>
     * </ul></p>
     *
     * @param id 库存ID，必填，不能为null，必须大于0
     * @throws InventoryNotFoundException 当库存ID不存在时抛出
     * @throws IllegalArgumentException 当id为null或小于等于0时抛出
     * @see InventoryNotFoundException
     * @since 3.0.0
     */
    void deleteInventory(Long id);

    /**
     * 预留库存数量
     *
     * <p>为指定产品和仓库预留指定数量的库存。
     * 预留成功后，库存数量会减少，但不会从总库存中扣除。</p>
     *
     * <p>业务规则：
     * <ul>
     *   <li>产品ID必须存在</li>
     *   <li>仓库ID必须存在</li>
     *   <li>预留数量必须大于0</li>
     *   <li>可用库存必须足够预留</li>
     * </ul></p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>订单创建时预留库存</li>
     *   <li>库存预分配和调拨</li>
     *   <li>库存规划和预测</li>
     * </ul></p>
     *
     * @param productId 产品ID，必填，不能为null，必须大于0
     * @param warehouseId 仓库ID，必填，不能为null，必须大于0
     * @param quantity 要预留的数量，必填，必须大于0
     * @return 预留后剩余的可用数量
     * @throws InventoryNotFoundException 当库存记录不存在时抛出
     * @throws IllegalArgumentException 当productId为null或小于等于0时抛出
     * @throws IllegalArgumentException 当warehouseId为null或小于等于0时抛出
     * @throws IllegalArgumentException 当quantity小于等于0时抛出
     * @throws RuntimeException 当可用库存不足时抛出
     * @see InventoryDTO
     * @since 3.0.0
     */
    Integer reserveInventory(Long productId, Long warehouseId, Integer quantity);

    /**
     * 释放预留的库存数量
     *
     * <p>释放之前预留的库存数量，使其重新变为可用库存。
     * 释放成功后，可用库存数量会增加。</p>
     *
     * <p>业务规则：
     * <ul>
     *   <li>产品ID必须存在</li>
     *   <li>仓库ID必须存在</li>
     *   <li>释放数量必须大于0</li>
     *   <li>释放数量不能超过已预留的数量</li>
     * </ul></p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>订单取消时释放预留库存</li>
     *   <li>订单超时自动释放</li>
     *   <li>库存预分配调整</li>
     * </ul></p>
     *
     * @param productId 产品ID，必填，不能为null，必须大于0
     * @param warehouseId 仓库ID，必填，不能为null，必须大于0
     * @param quantity 要释放的数量，必填，必须大于0
     * @return 释放后剩余的可用数量
     * @throws InventoryNotFoundException 当库存记录不存在时抛出
     * @throws IllegalArgumentException 当productId为null或小于等于0时抛出
     * @throws IllegalArgumentException 当warehouseId为null或小于等于0时抛出
     * @throws IllegalArgumentException 当quantity小于等于0时抛出
     * @see InventoryDTO
     * @since 3.0.0
     */
    Integer releaseInventory(Long productId, Long warehouseId, Integer quantity);

    /**
     * 调整库存数量
     *
     * <p>为指定产品和仓库调整库存数量。
     * 正值增加库存，负值减少库存。</p>
     *
     * <p>业务规则：
     * <ul>
     *   <li>产品ID必须存在</li>
     *   <li>仓库ID必须存在</li>
     *   <li>调整后库存不能为负数</li>
     *   <li>调整操作会记录库存变动历史</li>
     * </ul></p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>库存盘点和修正</li>
     *   <li>库存损耗和报溢出</li>
     *   <li>库存转移和调拨</li>
     * </ul></p>
     *
     * @param productId 产品ID，必填，不能为null，必须大于0
     * @param warehouseId 仓库ID，必填，不能为null，必须大于0
     * @param quantity 要调整的数量，正数增加，负数减少
     * @return 调整后剩余的可用数量
     * @throws InventoryNotFoundException 当库存记录不存在时抛出
     * @throws IllegalArgumentException 当productId为null或小于等于0时抛出
     * @throws IllegalArgumentException 当warehouseId为null或小于等于0时抛出
     * @throws RuntimeException 当调整后库存为负数时抛出
     * @see InventoryDTO
     * @since 3.0.0
     */
    Integer adjustInventory(Long productId, Long warehouseId, Integer quantity);
}
