package com.invoice.invoiceservice.controller;

import java.util.List;

import com.invoice.invoiceservice.dto.InvoiceProductDTO;
import com.invoice.invoiceservice.service.InvoiceProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invoice/products")
@RequiredArgsConstructor
@Tag(name = "Invoice Product", description = "开票商品信息管理API")
@Validated
public class InvoiceProductController {

    private final InvoiceProductService productService;

    /**
     * 创建商品信息
     *
     * @param dto 商品信息数据
     * @return 创建的商品信息
     */
    @PostMapping
    @Operation(summary = "创建商品信息")
    public ResponseEntity<InvoiceProductDTO> createProduct(@Valid @RequestBody InvoiceProductDTO dto) {
        InvoiceProductDTO created = productService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 更新商品信息
     *
     * @param id 商品ID
     * @param dto 更新后的商品信息数据
     * @return 更新后的商品信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新商品信息")
    public ResponseEntity<InvoiceProductDTO> updateProduct(
            @PathVariable Long id, @Valid @RequestBody InvoiceProductDTO dto) {
        InvoiceProductDTO updated = productService.updateProduct(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * 根据ID获取商品信息
     *
     * @param id 商品ID
     * @return 商品信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取商品信息")
    public ResponseEntity<InvoiceProductDTO> getProductById(@PathVariable Long id) {
        InvoiceProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * 获取所有商品信息
     *
     * @return 商品信息列表
     */
    @GetMapping
    @Operation(summary = "获取所有商品信息")
    public ResponseEntity<List<InvoiceProductDTO>> getAllProducts() {
        List<InvoiceProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * 获取启用状态的商品信息
     *
     * @return 启用状态的商品信息列表
     */
    @GetMapping("/active")
    @Operation(summary = "获取启用状态的商品信息")
    public ResponseEntity<List<InvoiceProductDTO>> getActiveProducts() {
        List<InvoiceProductDTO> products = productService.getActiveProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * 逻辑删除商品信息
     *
     * @param id 商品ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "逻辑删除商品信息")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 搜索商品信息
     *
     * @param keyword 搜索关键字
     * @return 符合条件的商品信息列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索商品信息")
    public ResponseEntity<List<InvoiceProductDTO>> searchProducts(@RequestParam String keyword) {
        List<InvoiceProductDTO> results = productService.searchProducts(keyword);
        return ResponseEntity.ok(results);
    }

    /**
     * 获取高频使用商品
     *
     * @param limit 返回数量限制
     * @return 高频使用的商品列表
     */
    @GetMapping("/frequent")
    @Operation(summary = "获取高频使用商品")
    public ResponseEntity<List<InvoiceProductDTO>> getFrequentProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<InvoiceProductDTO> results = productService.getFrequentlyUsedProducts(limit);
        return ResponseEntity.ok(results);
    }

    /**
     * 根据客户历史推荐商品
     *
     * @param customerId 客户ID
     * @param limit 返回数量限制
     * @return 推荐商品列表
     */
    @GetMapping("/recommend")
    @Operation(summary = "根据客户历史推荐商品")
    public ResponseEntity<List<InvoiceProductDTO>> recommendProducts(
            @RequestParam Long customerId,
            @RequestParam(defaultValue = "10") int limit) {
        List<InvoiceProductDTO> results = productService.recommendProductsByCustomer(customerId, limit);
        return ResponseEntity.ok(results);
    }
}
