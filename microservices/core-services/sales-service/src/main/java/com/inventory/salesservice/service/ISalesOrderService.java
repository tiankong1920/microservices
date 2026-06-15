package com.inventory.salesservice.service;

import com.inventory.salesservice.dto.SalesOrderDTO;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 销售订单服务接口
 *
 * <p>定义销售订单相关的业务操作接口，包括销售订单的增删改查、状态管理等。
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
 *   <li>销售订单管理：销售订单的创建、查询、更新、删除</li>
 *   <li>订单查询：按ID查询、按订单号查询、按客户查询、按状态查询、按日期范围查询</li>
 *   <li>订单状态管理：订单状态的更新和流转</li>
 *   <li>销售统计：客户销售总额统计</li>
 * </ul></p>
 *
 * @author Inventory Team
 * @since 3.0.0
 * @version 5.0
 * @see SalesOrderDTO
 * @see SalesOrderServiceImpl
 */
public interface ISalesOrderService {

    /**
     * 查询所有销售订单
     *
     * <p>从数据库中检索所有销售订单信息，返回订单列表。
     * 该方法不会抛出业务异常，如果数据库中没有订单，返回空列表。</p>
     *
     * <p>性能考虑：
     * <ul>
     *   <li>该方法会查询数据库中的所有订单，可能返回大量数据</li>
     *   <li>建议使用分页查询方法以避免性能问题</li>
     * </ul></p>
     *
     * @return 所有销售订单的列表，以SalesOrderDTO对象形式返回，如果数据库中没有订单则返回空列表
     * @see SalesOrderDTO
     * @since 3.0.0
     */
    List<SalesOrderDTO> getAllSalesOrders();

    /**
     * 根据订单ID查询销售订单
     *
     * <p>根据订单的唯一标识符查询订单详细信息。
     * 如果订单不存在，抛出SalesOrderNotFoundException异常。</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>查看订单详情页面</li>
     *   <li>编辑订单信息</li>
     *   <li>删除订单前确认</li>
     * </ul></p>
     *
     * @param id 订单ID，必填，不能为null，必须大于0
     * @return 指定ID的订单详细信息，以SalesOrderDTO对象形式返回
     * @throws com.inventory.salesservice.exception.SalesOrderNotFoundException 当订单ID不存在时抛出
     * @throws IllegalArgumentException 当id为null或小于等于0时抛出
     * @see SalesOrderDTO
     * @since 3.0.0
     */
    SalesOrderDTO getSalesOrderById(Long id);

    /**
     * 根据订单号查询销售订单
     *
     * <p>根据订单号查询订单详细信息。
     * 如果订单不存在，抛出SalesOrderNotFoundException异常。</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>通过订单号快速查找订单</li>
     *   <li>订单跟踪和查询</li>
     *   <li>客户服务支持</li>
     * </ul></p>
     *
     * @param orderNumber 订单号，必填，不能为null或空字符串
     * @return 指定订单号的订单详细信息，以SalesOrderDTO对象形式返回
     * @throws com.inventory.salesservice.exception.SalesOrderNotFoundException 当订单号不存在时抛出
     * @throws IllegalArgumentException 当orderNumber为null或空字符串时抛出
     * @see SalesOrderDTO
     * @since 3.0.0
     */
    SalesOrderDTO getSalesOrderByOrderNumber(String orderNumber);

    /**
     * 根据客户ID查询销售订单
     *
     * <p>根据客户ID查询所有销售订单。
     * 如果没有订单，返回空列表。</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>查看客户的所有订单</li>
     *   <li>客户订单历史查询</li>
     *   <li>客户销售统计</li>
     * </ul></p>
     *
     * @param customerId 客户ID，必填，不能为null，必须大于0
     * @return 指定客户的所有订单列表，以SalesOrderDTO对象形式返回
     * @throws IllegalArgumentException 当customerId为null或小于等于0时抛出
     * @see SalesOrderDTO
     * @since 3.0.0
     */
    List<SalesOrderDTO> getSalesOrdersByCustomerId(Long customerId);

    /**
     * 根据状态查询销售订单
     *
     * <p>根据订单状态查询所有销售订单。
     * 如果没有订单，返回空列表。</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>查看特定状态的订单</li>
     *   <li>订单状态统计</li>
     *   <li>订单处理流程管理</li>
     * </ul></p>
     *
     * @param status 订单状态，必填，不能为null或空字符串
     * @return 指定状态的所有订单列表，以SalesOrderDTO对象形式返回
     * @throws IllegalArgumentException 当status为null或空字符串时抛出
     * @see SalesOrderDTO
     * @since 3.0.0
     */
    List<SalesOrderDTO> getSalesOrdersByStatus(String status);

    /**
     * 根据日期范围查询销售订单
     *
     * <p>根据日期范围查询所有销售订单。
     * 如果没有订单，返回空列表。</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>查看特定时间段的订单</li>
     *   <li>销售报表统计</li>
     *   <li>订单趋势分析</li>
     * </ul></p>
     *
     * @param startDate 开始日期，必填，不能为null
     * @param endDate 结束日期，必填，不能为null
     * @return 指定日期范围内的所有订单列表，以SalesOrderDTO对象形式返回
     * @throws IllegalArgumentException 当startDate为null时抛出
     * @throws IllegalArgumentException 当endDate为null时抛出
     * @throws IllegalArgumentException 当startDate晚于endDate时抛出
     * @see SalesOrderDTO
     * @since 3.0.0
     */
    List<SalesOrderDTO> getSalesOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 计算客户销售总额
     *
     * <p>计算指定客户的所有销售订单总金额。
     * 如果客户没有订单，返回0.0。</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>客户销售统计</li>
     *   <li>客户价值分析</li>
     *   <li>客户等级评定</li>
     * </ul></p>
     *
     * @param customerId 客户ID，必填，不能为null，必须大于0
     * @return 客户的销售总额
     * @throws IllegalArgumentException 当customerId为null或小于等于0时抛出
     * @since 3.0.0
     */
    Double getTotalSalesByCustomerId(Long customerId);

    /**
     * 创建销售订单
     *
     * <p>在数据库中创建新销售订单，并返回创建后的订单信息。
     * 创建成功后，订单会自动生成唯一ID和订单号。</p>
     *
     * <p>业务规则：
     * <ul>
     *   <li>客户ID必须存在</li>
     *   <li>订单项不能为空</li>
     *   <li>订单金额必须大于0</li>
     *   <li>创建的订单默认状态为"待处理"</li>
     *   <li>创建订单时会自动扣减库存</li>
     * </ul></p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>订单信息录入</li>
     *   <li>批量导入订单</li>
     *   <li>订单复制功能</li>
     * </ul></p>
     *
     * @param salesOrderDTO 要创建的订单数据，必填，不能为null
     * @return 创建后的订单信息，包含自动生成的ID和订单号，以SalesOrderDTO对象形式返回
     * @throws IllegalArgumentException 当salesOrderDTO为null时抛出
     * @throws IllegalArgumentException 当客户ID不存在时抛出
     * @throws IllegalArgumentException 当订单项为空时抛出
     * @throws IllegalArgumentException 当订单金额小于等于0时抛出
     * @see SalesOrderDTO
     * @since 3.0.0
     */
    SalesOrderDTO createSalesOrder(SalesOrderDTO salesOrderDTO);

    /**
     * 更新销售订单
     *
     * <p>根据订单ID更新订单的详细信息。
     * 只更新提供的字段，未提供的字段保持不变。</p>
     *
     * <p>业务规则：
     * <ul>
     *   <li>只能更新已存在的订单</li>
     *   <li>如果修改订单项，会重新计算订单金额</li>
     *   <li>订单状态流转：待处理 -> 处理中 -> 已完成 -> 已取消</li>
     *   <li>更新操作会清除订单缓存</li>
     * </ul></p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>编辑订单信息</li>
     *   <li>调整订单金额</li>
     *   <li>修改订单状态</li>
     * </ul></p>
     *
     * @param id 订单ID，必填，不能为null，必须大于0
     * @param salesOrderDTO 更新的订单数据，必填，不能为null
     * @return 更新后的订单信息，以SalesOrderDTO对象形式返回
     * @throws com.inventory.salesservice.exception.SalesOrderNotFoundException 当订单ID不存在时抛出
     * @throws IllegalArgumentException 当id为null或小于等于0时抛出
     * @throws IllegalArgumentException 当salesOrderDTO为null时抛出
     * @see SalesOrderDTO
     * @since 3.0.0
     */
    SalesOrderDTO updateSalesOrder(Long id, SalesOrderDTO salesOrderDTO);

    /**
     * 删除销售订单
     *
     * <p>根据订单ID从数据库中删除订单。
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
     *   <li>删除错误录入的订单</li>
     *   <li>清理不再需要的订单</li>
     *   <li>订单数据清理</li>
     * </ul></p>
     *
     * <p>安全考虑：
     * <ul>
     *   <li>删除操作不可逆，请谨慎操作</li>
     *   <li>建议先检查订单状态</li>
     *   <li>建议使用软删除而非物理删除</li>
     * </ul></p>
     *
     * @param id 订单ID，必填，不能为null，必须大于0
     * @throws com.inventory.salesservice.exception.SalesOrderNotFoundException 当订单ID不存在时抛出
     * @throws IllegalArgumentException 当id为null或小于等于0时抛出
     * @since 3.0.0
     */
    void deleteSalesOrder(Long id);

    /**
     * 更新订单状态
     *
     * <p>根据订单ID更新订单的状态。
     * 状态更新会触发相应的业务流程。</p>
     *
     * <p>业务规则：
     * <ul>
     *   <li>只能更新已存在的订单</li>
     *   <li>状态流转必须符合预定义的流程</li>
     *   <li>状态更新会触发相应的业务操作（如库存扣减、恢复）</li>
     *   <li>状态更新会清除订单缓存</li>
     * </ul></p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>订单处理流程管理</li>
     *   <li>订单状态变更通知</li>
     *   <li>订单状态统计</li>
     * </ul></p>
     *
     * @param id 订单ID，必填，不能为null，必须大于0
     * @param status 新状态，必填，不能为null或空字符串
     * @return 更新后的订单信息，以SalesOrderDTO对象形式返回
     * @throws com.inventory.salesservice.exception.SalesOrderNotFoundException 当订单ID不存在时抛出
     * @throws IllegalArgumentException 当id为null或小于等于0时抛出
     * @throws IllegalArgumentException 当status为null或空字符串时抛出
     * @throws IllegalArgumentException 当状态流转不符合预定义流程时抛出
     * @see SalesOrderDTO
     * @since 3.0.0
     */
    SalesOrderDTO updateSalesOrderStatus(Long id, String status);
}
