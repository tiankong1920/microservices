package com.inventory.productservice.service;

import java.util.List;

import com.inventory.productservice.dto.ProductDTO;
import com.inventory.productservice.dto.ProductSKUDTO;
import com.inventory.productservice.exception.ProductNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 产品服务接口
 *
 * <p>定义产品相关的业务操作接口，包括产品的增删改查、SKU管理、BOM管理等。
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
 *   <li>产品信息管理：产品的创建、查询、更新、删除</li>
 *   <li>产品SKU管理：产品规格、库存单位、价格等SKU信息管理</li>
 *   <li>产品BOM管理：产品物料清单配置，支持复杂产品的组装</li>
 *   <li>产品查询支持：按ID查询、按SKU查询、分页查询等</li>
 * </ul></p>
 *
 * @author Inventory Team
 * @since 3.0.0
 * @version 5.0
 * @see ProductDTO
 * @see ProductServiceImpl
 */
public interface ProductService {

    /**
     * 查询所有产品信息
     *
     * <p>从数据库中检索所有产品信息，返回产品列表。
     * 该方法不会抛出业务异常，如果数据库中没有产品，返回空列表。</p>
     *
     * <p>性能考虑：
     * <ul>
     *   <li>该方法会查询数据库中的所有产品，可能返回大量数据</li>
     *   <li>建议使用分页查询方法以避免性能问题</li>
     *   <li>结果会被缓存，后续查询会直接从缓存获取</li>
     * </ul></p>
     *
     * @return 所有产品的列表，以ProductDTO对象形式返回，如果数据库中没有产品则返回空列表
     * @see ProductDTO
     * @since 3.0.0
     * @deprecated 请使用 {@link #getAllProducts(Pageable)} 代替，该方法缺少分页，大数据量下存在性能风险
     */
    @Deprecated(since = "3.0.0", forRemoval = false)
    List<ProductDTO> getAllProducts();

    /**
     * 分页查询产品信息
     *
     * <p>使用分页方式查询产品信息，适用于大数据量场景。
     * 支持排序和分页参数。</p>
     *
     * @param pageable 分页参数，包含页码、每页大小和排序信息
     * @return 分页后的产品列表，以Page对象形式返回
     * @see Pageable
     * @see Page
     * @since 3.0.0
     */
    Page<ProductDTO> getAllProducts(Pageable pageable);

    /**
     * 根据产品ID查询产品信息
     *
     * <p>根据产品的唯一标识符查询产品详细信息。
     * 如果产品不存在，抛出ProductNotFoundException异常。</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>查看产品详情页面</li>
     *   <li>编辑产品信息</li>
     *   <li>删除产品前确认</li>
     * </ul></p>
     *
     * @param id 产品ID，必填，不能为null，必须大于0
     * @return 指定ID的产品详细信息，以ProductDTO对象形式返回
     * @throws ProductNotFoundException 当产品ID不存在时抛出
     * @throws IllegalArgumentException 当id为null或小于等于0时抛出
     * @see ProductDTO
     * @see ProductNotFoundException
     * @since 3.0.0
     */
    ProductDTO getProductById(Long id);

    /**
     * 根据产品SKU代码查询产品信息
     *
     * <p>根据产品的SKU（Stock Keeping Unit）代码查询产品详细信息。
     * SKU是产品的唯一编码，用于库存管理和销售。</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>通过SKU快速查找产品</li>
     *   <li>库存管理中根据SKU查询产品</li>
     *   <li>销售订单中根据SKU添加产品</li>
     * </ul></p>
     *
     * @param sku 产品SKU代码，必填，不能为null或空字符串
     * @return 指定SKU的产品详细信息，以ProductDTO对象形式返回
     * @throws ProductNotFoundException 当产品SKU不存在时抛出
     * @throws IllegalArgumentException 当sku为null或空字符串时抛出
     * @see ProductDTO
     * @see ProductNotFoundException
     * @since 3.0.0
     */
    ProductDTO getProductBySku(String sku);

    /**
     * 创建新产品
     *
     * <p>在数据库中创建新产品，并返回创建后的产品信息。
     * 创建成功后，产品会自动生成唯一ID。</p>
     *
     * <p>业务规则：
     * <ul>
     *   <li>产品SKU必须唯一，如果SKU已存在会抛出异常</li>
     *   <li>产品名称不能为空</li>
     *   <li>产品价格必须大于0</li>
     *   <li>创建的产品默认状态为"草稿"</li>
     * </ul></p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>产品信息录入</li>
     *   <li>批量导入产品</li>
     *   <li>产品复制功能</li>
     * </ul></p>
     *
     * @param productDTO 要创建的产品数据，必填，不能为null
     * @return 创建后的产品信息，包含自动生成的ID，以ProductDTO对象形式返回
     * @throws IllegalArgumentException 当productDTO为null时抛出
     * @throws IllegalArgumentException 当产品名称为空时抛出
     * @throws IllegalArgumentException 当产品价格小于等于0时抛出
     * @throws IllegalArgumentException 当产品SKU已存在时抛出
     * @see ProductDTO
     * @since 3.0.0
     */
    ProductDTO createProduct(ProductDTO productDTO);

    /**
     * 更新产品信息
     *
     * <p>根据产品ID更新产品的详细信息。
     * 只更新提供的字段，未提供的字段保持不变。</p>
     *
     * <p>业务规则：
     * <ul>
     *   <li>只能更新已存在的产品</li>
     *   <li>如果修改SKU，新的SKU必须唯一</li>
     *   <li>产品名称不能为空</li>
     *   <li>产品价格必须大于0</li>
     *   <li>更新操作会清除产品缓存</li>
     * </ul></p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>编辑产品信息</li>
     *   <li>调整产品价格</li>
     *   <li>修改产品状态（发布、下架）</li>
     * </ul></p>
     *
     * @param id 产品ID，必填，不能为null，必须大于0
     * @param productDTO 更新的产品数据，必填，不能为null
     * @return 更新后的产品信息，以ProductDTO对象形式返回
     * @throws ProductNotFoundException 当产品ID不存在时抛出
     * @throws IllegalArgumentException 当id为null或小于等于0时抛出
     * @throws IllegalArgumentException 当productDTO为null时抛出
     * @throws IllegalArgumentException 当产品名称为空时抛出
     * @throws IllegalArgumentException 当产品价格小于等于0时抛出
     * @throws IllegalArgumentException 当产品SKU已存在时抛出
     * @see ProductDTO
     * @see ProductNotFoundException
     * @since 3.0.0
     */
    ProductDTO updateProduct(Long id, ProductDTO productDTO);

    /**
     * 删除产品
     *
     * <p>根据产品ID从数据库中删除产品。
     * 删除操作是物理删除，不可恢复。</p>
     *
     * <p>业务规则：
     * <ul>
     *   <li>只能删除已存在的产品</li>
     *   <li>如果产品已被订单引用，不能删除</li>
     *   <li>删除操作会级联删除相关的SKU和BOM信息</li>
     *   <li>删除操作会清除产品缓存</li>
     * </ul></p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>删除错误录入的产品</li>
     *   <li>清理不再销售的产品</li>
     *   <li>产品数据清理</li>
     * </ul></p>
     *
     * <p>安全考虑：
     * <ul>
     *   <li>删除操作不可逆，请谨慎操作</li>
     *   <li>建议先检查产品是否被订单引用</li>
     *   <li>建议使用软删除而非物理删除</li>
     * </ul></p>
     *
     * @param id 产品ID，必填，不能为null，必须大于0
     * @throws ProductNotFoundException 当产品ID不存在时抛出
     * @throws IllegalArgumentException 当id为null或小于等于0时抛出
     * @throws IllegalStateException 当产品已被订单引用时抛出
     * @see ProductNotFoundException
     * @since 3.0.0
     */
    void deleteProduct(Long id);

    List<ProductSKUDTO> getProductSKUs(Long productId);

    ProductSKUDTO getProductSKUByCode(String skuCode);

    ProductSKUDTO createProductSKU(ProductSKUDTO skuDTO);

    ProductSKUDTO updateProductSKU(Long skuId, ProductSKUDTO skuDTO);

    void deleteProductSKU(Long skuId);
}
