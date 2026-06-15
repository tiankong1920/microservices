package com.inventory.inventoryservice.service;

import com.inventory.inventoryservice.dto.StockTransferOrderDTO;

import java.util.List;

/**
 * 库存转移服务接口
 *
 * <p>定义库存转移相关的业务操作接口，包括库存转移订单的增删改查、状态管理等。
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
 *   <li>库存转移订单管理：转移订单的创建、查询、更新、删除</li>
 *   <li>转移订单查询：按ID查询、按转移单号查询、按源仓库查询、按目标仓库查询、按状态查询</li>
 *   <li>转移订单状态管理：订单状态的更新和流转</li>
 *   <li>转移统计：仓库转移统计</li>
 * </ul></p>
 *
 * @author Inventory Team
 * @since 3.0.0
 * @version 5.0
 * @see StockTransferOrderDTO
 * @see com.inventory.inventoryservice.service.impl.StockTransferServiceImpl
 */
public interface IStockTransferService {

    /**
     * 查询所有库存转移订单
     *
     * <p>从数据库中检索所有库存转移订单信息，返回订单列表。
     * 该方法不会抛出业务异常，如果数据库中没有订单，返回空列表。</p>
     *
     * <p>性能考虑：
     * <ul>
     *   <li>该方法会查询数据库中的所有订单，可能返回大量数据</li>
     *   <li>建议使用分页查询方法以避免性能问题</li>
     * </ul></p>
     *
     * @return 所有库存转移订单的列表，以StockTransferOrderDTO对象形式返回，如果数据库中没有订单则返回空列表
     * @see StockTransferOrderDTO
     * @since 3.0.0
     */
    List<StockTransferOrderDTO> getAllStockTransferOrders();

    /**
     * 根据转移订单ID查询转移订单
     *
     * <p>根据转移订单的唯一标识符查询订单详细信息。
     * 如果订单不存在，抛出StockTransferOrderNotFoundException异常。</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>查看转移订单详情页面</li>
     *   <li>编辑转移订单信息</li>
     *   <li>删除转移订单前确认</li>
     * </ul></p>
     *
     * @param id 转移订单ID，必填，不能为null，必须大于0
     * @return 指定ID的转移订单详细信息，以StockTransferOrderDTO对象形式返回
     * @throws com.inventory.inventoryservice.exception.StockTransferOrderNotFoundException 当转移订单ID不存在时抛出
     * @throws IllegalArgumentException 当id为null或小于等于0时抛出
     * @see StockTransferOrderDTO
     * @since 3.0.0
     */
    StockTransferOrderDTO getStockTransferOrderById(Long id);

    /**
     * 根据转移单号查询转移订单
     *
     * <p>根据转移单号查询转移订单详细信息。
     * 如果订单不存在，抛出StockTransferOrderNotFoundException异常。</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>通过转移单号快速查找订单</li>
     *   <li>转移订单跟踪和查询</li>
     *   <li>转移单号唯一性检查</li>
     * </ul></p>
     *
     * @param transferNumber 转移单号，必填，不能为null或空字符串
     * @return 指定转移单号的订单详细信息，以StockTransferOrderDTO对象形式返回
     * @throws com.inventory.inventoryservice.exception.StockTransferOrderNotFoundException 当转移单号不存在时抛出
     * @throws IllegalArgumentException 当transferNumber为null或空字符串时抛出
     * @see StockTransferOrderDTO
     * @since 3.0.0
     */
    StockTransferOrderDTO getStockTransferOrderByTransferNumber(String transferNumber);

    /**
     * 根据源仓库ID查询转移订单
     *
     * <p>根据源仓库ID查询所有从该仓库转出的转移订单。
     * 如果没有订单，返回空列表。</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>查看仓库的转出记录</li>
     *   <li>仓库出库统计</li>
     *   <li>仓库库存分析</li>
     * </ul></p>
     *
     * @param sourceWarehouseId 源仓库ID，必填，不能为null，必须大于0
     * @return 指定源仓库的所有转移订单列表，以StockTransferOrderDTO对象形式返回
     * @throws IllegalArgumentException 当sourceWarehouseId为null或小于等于0时抛出
     * @see StockTransferOrderDTO
     * @since 3.0.0
     */
    List<StockTransferOrderDTO> getStockTransferOrdersBySourceWarehouse(Long sourceWarehouseId);

    /**
     * 根据目标仓库ID查询转移订单
     *
     * <p>根据目标仓库ID查询所有转入该仓库的转移订单。
     * 如果没有订单，返回空列表。</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>查看仓库的转入记录</li>
     *   <li>仓库入库统计</li>
     *   <li>仓库库存分析</li>
     * </ul></p>
     *
     * @param targetWarehouseId 目标仓库ID，必填，不能为null，必须大于0
     * @return 指定目标仓库的所有转移订单列表，以StockTransferOrderDTO对象形式返回
     * @throws IllegalArgumentException 当targetWarehouseId为null或小于等于0时抛出
     * @see StockTransferOrderDTO
     * @since 3.0.0
     */
    List<StockTransferOrderDTO> getStockTransferOrdersByTargetWarehouse(Long targetWarehouseId);

    /**
     * 根据状态查询转移订单
     *
     * <p>根据订单状态查询所有转移订单。
     * 如果没有订单，返回空列表。</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>查看特定状态的转移订单</li>
     *   <li>订单状态统计</li>
     *   <li>订单处理流程管理</li>
     * </ul></p>
     *
     * @param status 订单状态，必填，不能为null或空字符串
     * @return 指定状态的所有转移订单列表，以StockTransferOrderDTO对象形式返回
     * @throws IllegalArgumentException 当status为null或空字符串时抛出
     * @see StockTransferOrderDTO
     * @since 3.0.0
     */
    List<StockTransferOrderDTO> getStockTransferOrdersByStatus(String status);

    /**
     * 创建新库存转移订单
     *
     * <p>在数据库中创建新库存转移订单，并返回创建后的订单信息。
     * 创建成功后，订单会自动生成唯一ID和转移单号。</p>
     *
     * <p>业务规则：
     * <ul>
     *   <li>源仓库ID必须存在</li>
     *   <li>目标仓库ID必须存在</li>
     *   <li>源仓库和目标仓库不能相同</li>
     *   <li>转移产品必须存在</li>
     *   <li>转移数量必须大于0</li>
     *   <li>源仓库必须有足够的库存</li>
     *   <li>创建的订单默认状态为"待处理"</li>
     * </ul></p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>库存转移订单录入</li>
     *   <li>批量转移</li>
     *   <li>仓库调拨</li>
     * </ul></p>
     *
     * @param stockTransferOrderDTO 要创建的转移订单数据，必填，不能为null
     * @return 创建后的转移订单信息，包含自动生成的ID和转移单号，以StockTransferOrderDTO对象形式返回
     * @throws IllegalArgumentException 当stockTransferOrderDTO为null时抛出
     * @throws IllegalArgumentException 当源仓库ID不存在时抛出
     * @throws IllegalArgumentException 当目标仓库ID不存在时抛出
     * @throws IllegalArgumentException 当源仓库和目标仓库相同时抛出
     * @throws IllegalArgumentException 当转移数量小于等于0时抛出
     * @throws RuntimeException 当源仓库库存不足时抛出
     * @see StockTransferOrderDTO
     * @since 3.0.0
     */
    StockTransferOrderDTO createStockTransferOrder(StockTransferOrderDTO stockTransferOrderDTO);

    /**
     * 更新库存转移订单
     *
     * <p>根据转移订单ID更新订单的详细信息。
     * 只更新提供的字段，未提供的字段保持不变。</p>
     *
     * <p>业务规则：
     * <ul>
     *   <li>只能更新已存在的订单</li>
     *   <li>如果修改转移项，会重新计算转移数量</li>
     *   <li>订单状态流转：待处理 -> 处理中 -> 已完成 -> 已取消</li>
     *   <li>更新操作会清除订单缓存</li>
     * </ul></p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>编辑转移订单信息</li>
     *   <li>调整转移数量</li>
     *   <li>修改订单状态</li>
     * </ul></p>
     *
     * @param id 转移订单ID，必填，不能为null，必须大于0
     * @param stockTransferOrderDTO 更新的转移订单数据，必填，不能为null
     * @return 更新后的转移订单信息，以StockTransferOrderDTO对象形式返回
     * @throws com.inventory.inventoryservice.exception.StockTransferOrderNotFoundException 当转移订单ID不存在时抛出
     * @throws IllegalArgumentException 当id为null或小于等于0时抛出
     * @throws IllegalArgumentException 当stockTransferOrderDTO为null时抛出
     * @see StockTransferOrderDTO
     * @since 3.0.0
     */
    StockTransferOrderDTO updateStockTransferOrder(Long id, StockTransferOrderDTO stockTransferOrderDTO);

    /**
     * 删除库存转移订单
     *
     * <p>根据转移订单ID从数据库中删除订单。
     * 删除操作是物理删除，不可恢复。</p>
     *
     * <p>业务规则：
     * <ul>
     *   <li>只能删除已存在的订单</li>
     *   <li>如果订单状态为"已完成"，不能删除</li>
     *   <li>删除订单时会恢复库存</li>
     *   <li>删除操作会清除订单缓存</li>
     * </ul></p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>删除错误录入的转移订单</li>
     *   <li>清理不再需要的转移订单</li>
     *   <li>转移订单数据清理</li>
     * </ul></p>
     *
     * <p>安全考虑：
     * <ul>
     *   <li>删除操作不可逆，请谨慎操作</li>
     *   <li>建议先检查订单状态</li>
     *   <li>建议使用软删除而非物理删除</li>
     * </ul></p>
     *
     * @param id 转移订单ID，必填，不能为null，必须大于0
     * @throws com.inventory.inventoryservice.exception.StockTransferOrderNotFoundException 当转移订单ID不存在时抛出
     * @throws IllegalArgumentException 当id为null或小于等于0时抛出
     * @throws IllegalStateException 当订单状态为"已完成"时抛出
     * @since 3.0.0
     */
    void deleteStockTransferOrder(Long id);

    /**
     * 更新转移订单状态
     *
     * <p>根据转移订单ID更新订单的状态。
     * 状态更新会触发相应的业务流程。</p>
     *
     * <p>业务规则：
     * <ul>
     *   <li>只能更新已存在的订单</li>
     *   <li>状态流转必须符合预定义的流程</li>
     *   <li>状态更新会触发相应的业务操作（如库存转移、恢复）</li>
     *   <li>状态更新会清除订单缓存</li>
     * </ul></p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>转移订单处理流程管理</li>
     *   <li>转移订单状态变更通知</li>
     *   <li>转移订单状态统计</li>
     * </ul></p>
     *
     * @param id 转移订单ID，必填，不能为null，必须大于0
     * @param status 新状态，必填，不能为null或空字符串
     * @return 更新后的转移订单信息，以StockTransferOrderDTO对象形式返回
     * @throws com.inventory.inventoryservice.exception.StockTransferOrderNotFoundException 当转移订单ID不存在时抛出
     * @throws IllegalArgumentException 当id为null或小于等于0时抛出
     * @throws IllegalArgumentException 当status为null或空字符串时抛出
     * @throws IllegalArgumentException 当状态流转不符合预定义流程时抛出
     * @see StockTransferOrderDTO
     * @since 3.0.0
     */
    StockTransferOrderDTO updateStockTransferOrderStatus(Long id, String status);

    /**
     * 计算仓库的总转移数量
     *
     * <p>计算指定仓库的总转移数量（包括转出和转入）。
     * 如果仓库没有转移记录，返回0。</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>仓库转移统计</li>
     *   <li>仓库活跃度分析</li>
     *   <li>仓库绩效评估</li>
     * </ul></p>
     *
     * @param warehouseId 仓库ID，必填，不能为null，必须大于0
     * @return 指定仓库的总转移数量
     * @throws IllegalArgumentException 当warehouseId为null或小于等于0时抛出
     * @since 3.0.0
     */
    Integer getTotalTransferByWarehouse(Long warehouseId);
}
