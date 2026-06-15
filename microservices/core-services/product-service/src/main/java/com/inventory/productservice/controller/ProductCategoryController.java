package com.inventory.productservice.controller;

import com.inventory.productservice.dto.ProductCategoryDTO;
import com.inventory.productservice.service.ProductCategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
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

import java.util.List;

/**
 * 商品分类控制器 - 管理商品分类的增删改查和树形结构
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Product Category", description = "商品分类管理接口")
@RequiredArgsConstructor
@Slf4j
@Validated
@SuppressWarnings("null")
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    /**
     * 获取分类详情
     *
     * @param id 分类ID
     * @return 分类信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取分类详情")
    public ResponseEntity<ProductCategoryDTO> getCategory(@PathVariable final Long id) {
        log.debug("REST request to get category : {}", id);
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    /**
     * 获取分类树
     *
     * @return 分类树形结构列表
     */
    @GetMapping("/tree")
    @Operation(summary = "获取分类树")
    public ResponseEntity<List<ProductCategoryDTO>> getCategoryTree() {
        log.debug("REST request to get category tree");
        return ResponseEntity.ok(categoryService.getCategoryTree());
    }

    /**
     * 获取子分类列表
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    @GetMapping("/{parentId}/children")
    @Operation(summary = "获取子分类列表")
    public ResponseEntity<List<ProductCategoryDTO>> getSubCategories(
            @PathVariable final Long parentId) {
        log.debug("REST request to get sub categories for parent : {}", parentId);
        return ResponseEntity.ok(categoryService.getSubCategories(parentId));
    }

    /**
     * 创建商品分类
     *
     * @param dto 分类数据
     * @return 创建的分类信息
     */
    @PostMapping
    @Operation(summary = "创建商品分类")
    public ResponseEntity<ProductCategoryDTO> createCategory(
            @Valid @RequestBody final ProductCategoryDTO dto) {
        log.debug("REST request to create category : {}", dto.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(dto));
    }

    /**
     * 更新商品分类
     *
     * @param id 分类ID
     * @param dto 更新后的分类数据
     * @return 更新后的分类信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新商品分类")
    public ResponseEntity<ProductCategoryDTO> updateCategory(
            @PathVariable final Long id, @Valid @RequestBody final ProductCategoryDTO dto) {
        log.debug("REST request to update category : {}", id);
        return ResponseEntity.ok(categoryService.updateCategory(id, dto));
    }

    /**
     * 删除商品分类
     *
     * @param id 分类ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品分类")
    public ResponseEntity<Void> deleteCategory(@PathVariable final Long id) {
        log.debug("REST request to delete category : {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 更新分类状态
     *
     * @param id 分类ID
     * @param status 状态
     * @return 更新后的分类信息
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新分类状态")
    public ResponseEntity<ProductCategoryDTO> updateCategoryStatus(
            @PathVariable final Long id, @RequestBody final String status) {
        log.debug("REST request to update category status : {} to {}", id, status);
        return ResponseEntity.ok(categoryService.updateCategoryStatus(id, status));
    }

    /**
     * 移动分类到新父级
     *
     * @param id 分类ID
     * @param newParentId 新父分类ID
     * @return 无内容响应
     */
    @PutMapping("/{id}/move")
    @Operation(summary = "移动分类到新父级")
    public ResponseEntity<Void> moveCategory(
            @PathVariable final Long id, @RequestBody final Long newParentId) {
        log.debug("REST request to move category {} to parent {}", id, newParentId);
        categoryService.moveCategory(id, newParentId);
        return ResponseEntity.ok().build();
    }

    /**
     * 排序分类
     *
     * @param categoryIds 分类ID列表
     * @return 无内容响应
     */
    @PutMapping("/sort")
    public ResponseEntity<Void> sortCategories(@RequestBody final List<Long> categoryIds) {
        log.debug("REST request to sort categories");
        categoryService.sortCategories(categoryIds);
        return ResponseEntity.ok().build();
    }
}
