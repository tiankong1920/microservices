package com.inventory.productservice.controller;

import java.util.List;
import java.util.Set;

import com.inventory.productservice.dto.ProductDTO;
import com.inventory.productservice.dto.ProductSKUDTO;
import com.inventory.productservice.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "Product Management API")
@Validated
public class ProductController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "id", "productCode", "sku", "name", "brand",
        "price", "costPrice", "stockQuantity", "status",
        "weight", "isActive", "createdAt", "updatedAt"
    );

    private final ProductService productService;

    /**
     * 获取所有产品
     *
     * @return 所有产品列表
     */
    @GetMapping
    @Operation(summary = "获取所有产品", description = "获取系统中所有产品的完整列表")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        log.info("Request received to get all products");
        final List<ProductDTO> products = productService.getAllProducts();
        log.info("Retrieved {} products", products.size());
        return ResponseEntity.ok(products);
    }

    /**
     * 分页获取产品
     *
     * @param pageable 分页参数
     * @return 分页产品列表
     */
    @GetMapping("/paged")
    @Operation(summary = "分页获取产品", description = "分页获取产品列表")
    public ResponseEntity<Page<ProductDTO>> getProductsPaged(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        log.info("Request received to get products with pagination: {}", pageable);
        Pageable sanitizedPageable = sanitizeSort(pageable);
        Page<ProductDTO> products = productService.getAllProducts(sanitizedPageable);
        log.info("Retrieved {} products on page {}", products.getNumberOfElements(), products.getNumber());
        return ResponseEntity.ok(products);
    }

    private Pageable sanitizeSort(Pageable pageable) {
        Sort sort = pageable.getSort();
        if (sort == null || sort.isUnsorted()) {
            return pageable;
        }
        for (Sort.Order order : sort) {
            if (!ALLOWED_SORT_FIELDS.contains(order.getProperty())) {
                log.warn("Blocked sort field '{}', falling back to default sort (id)", order.getProperty());
                return Pageable.ofSize(pageable.getPageSize()).withPage(pageable.getPageNumber());
            }
        }
        return pageable;
    }

    /**
     * 根据ID获取产品
     *
     * @param id 产品ID
     * @return 产品信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取产品", description = "根据唯一标识符获取单个产品")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        log.info("Request received to get product with id: {}", id);
        final ProductDTO product = productService.getProductById(id);
        log.info("Retrieved product: {}", product.getName());
        return ResponseEntity.ok(product);
    }

    /**
     * 创建产品
     *
     * @param productDTO 产品数据
     * @return 创建的产品信息
     */
    @PostMapping
    @Operation(summary = "创建产品", description = "使用提供的信息创建新产品")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        log.info("Request received to create product: {}", productDTO.getName());
        final ProductDTO createdProduct = productService.createProduct(productDTO);
        log.info("Created product with id: {}", createdProduct.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    /**
     * 更新产品
     *
     * @param id 产品ID
     * @param productDTO 更新后的产品数据
     * @return 更新后的产品信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新产品", description = "使用提供的信息更新现有产品")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO productDTO) {
        log.info("Request received to update product with id: {}", id);
        final ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
        log.info("Updated product: {}", updatedProduct.getName());
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * 删除产品
     *
     * @param id 产品ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除产品", description = "根据唯一标识符删除产品")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("Request received to delete product with id: {}", id);
        productService.deleteProduct(id);
        log.info("Deleted product with id: {}", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 根据SKU获取产品
     *
     * @param sku 产品SKU编码
     * @return 产品信息
     */
    @GetMapping("/sku/{sku}")
    @Operation(summary = "根据SKU获取产品", description = "根据SKU编码获取产品")
    public ResponseEntity<ProductDTO> getProductBySku(@PathVariable String sku) {
        log.info("Request received to get product with SKU: {}", sku);
        final ProductDTO product = productService.getProductBySku(sku);
        log.info("Retrieved product: {}", product.getName());
        return ResponseEntity.ok(product);
    }

    /**
     * 获取产品SKU列表
     *
     * @param productId 产品ID
     * @return SKU列表
     */
    @GetMapping("/{productId}/skus")
    @Operation(summary = "获取产品SKU列表", description = "获取指定产品的所有SKU")
    public ResponseEntity<List<ProductSKUDTO>> getProductSKUs(@PathVariable Long productId) {
        log.info("Request received to get SKUs for product id: {}", productId);
        return ResponseEntity.ok(productService.getProductSKUs(productId));
    }

    /**
     * 根据SKU编码获取产品SKU
     *
     * @param skuCode SKU编码
     * @return 产品SKU信息
     */
    @GetMapping("/skus/code/{skuCode}")
    @Operation(summary = "根据SKU编码获取产品SKU", description = "根据SKU编码获取产品SKU详情")
    public ResponseEntity<ProductSKUDTO> getProductSKUByCode(@PathVariable String skuCode) {
        log.info("Request received to get product SKU by code: {}", skuCode);
        return ResponseEntity.ok(productService.getProductSKUByCode(skuCode));
    }

    /**
     * 创建产品SKU
     *
     * @param productId 产品ID
     * @param skuDTO SKU数据
     * @return 创建的SKU信息
     */
    @PostMapping("/{productId}/skus")
    @Operation(summary = "创建产品SKU", description = "为指定产品创建SKU")
    public ResponseEntity<ProductSKUDTO> createProductSKU(
            @PathVariable Long productId, @Valid @RequestBody ProductSKUDTO skuDTO) {
        log.info("Request received to create SKU for product id: {}", productId);
        skuDTO.setProductId(productId);
        final ProductSKUDTO createdSKU = productService.createProductSKU(skuDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSKU);
    }

    /**
     * 更新产品SKU
     *
     * @param skuId SKU ID
     * @param skuDTO 更新后的SKU数据
     * @return 更新后的SKU信息
     */
    @PutMapping("/skus/{skuId}")
    @Operation(summary = "更新产品SKU", description = "更新现有产品SKU")
    public ResponseEntity<ProductSKUDTO> updateProductSKU(
            @PathVariable Long skuId, @Valid @RequestBody ProductSKUDTO skuDTO) {
        log.info("Request received to update product SKU with id: {}", skuId);
        final ProductSKUDTO updatedSKU = productService.updateProductSKU(skuId, skuDTO);
        return ResponseEntity.ok(updatedSKU);
    }

    /**
     * 删除产品SKU
     *
     * @param skuId SKU ID
     * @return 无内容响应
     */
    @DeleteMapping("/skus/{skuId}")
    @Operation(summary = "删除产品SKU", description = "根据唯一标识符删除产品SKU")
    public ResponseEntity<Void> deleteProductSKU(@PathVariable Long skuId) {
        log.info("Request received to delete product SKU with id: {}", skuId);
        productService.deleteProductSKU(skuId);
        return ResponseEntity.noContent().build();
    }
}
