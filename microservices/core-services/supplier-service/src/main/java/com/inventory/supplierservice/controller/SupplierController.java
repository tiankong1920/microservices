package com.inventory.supplierservice.controller;

import com.inventory.supplierservice.dto.SupplierDTO;
import com.inventory.supplierservice.service.ISupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@Tag(name = "Supplier", description = "Supplier Management API")
@Validated
public class SupplierController {

    private final ISupplierService supplierService;

    /**
     * 分页获取所有供应商
     *
     * @param page 页码，从0开始
     * @param size 每页数量
     * @param sortBy 排序字段
     * @param sortDir 排序方向
     * @return 分页供应商列表
     */
    @GetMapping
    @Operation(summary = "分页获取所有供应商")
    public ResponseEntity<Page<SupplierDTO>> getAllSuppliers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(supplierService.getAllSuppliers(pageable));
    }

    /**
     * 获取所有供应商列表
     *
     * @return 所有供应商列表
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有供应商")
    public ResponseEntity<List<SupplierDTO>> getAllSuppliersList() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    /**
     * 根据ID获取供应商
     *
     * @param id 供应商ID
     * @return 供应商信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取供应商")
    public ResponseEntity<SupplierDTO> getSupplierById(@PathVariable final Long id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    /**
     * 分页获取活跃供应商
     *
     * @param page 页码，从0开始
     * @param size 每页数量
     * @return 分页活跃供应商列表
     */
    @GetMapping("/active")
    @Operation(summary = "分页获取活跃供应商")
    public ResponseEntity<Page<SupplierDTO>> getActiveSuppliers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(supplierService.getActiveSuppliers(pageable));
    }

    /**
     * 根据产品类别获取供应商
     *
     * @param category 产品类别
     * @param page 页码，从0开始
     * @param size 每页数量
     * @return 分页供应商列表
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "根据产品类别获取供应商")
    public ResponseEntity<Page<SupplierDTO>> getSuppliersByCategory(
            @PathVariable final String category,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(supplierService.getSuppliersByProductCategory(category, pageable));
    }

    /**
     * 搜索供应商
     *
     * @param keyword 搜索关键词
     * @param page 页码，从0开始
     * @param size 每页数量
     * @return 搜索结果分页列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索供应商")
    public ResponseEntity<Page<SupplierDTO>> searchSuppliers(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(supplierService.searchSuppliers(keyword, pageable));
    }

    /**
     * 根据名称搜索供应商
     *
     * @param name 供应商名称
     * @param page 页码，从0开始
     * @param size 每页数量
     * @return 搜索结果分页列表
     */
    @GetMapping("/search/name")
    @Operation(summary = "根据名称搜索供应商")
    public ResponseEntity<Page<SupplierDTO>> searchSuppliersByName(
            @Parameter(description = "Supplier name") @RequestParam String name,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(supplierService.searchSuppliersByName(name, pageable));
    }

    /**
     * 创建供应商
     *
     * @param supplierDTO 供应商数据
     * @return 创建的供应商信息
     */
    @PostMapping
    @Operation(summary = "创建供应商")
    public ResponseEntity<SupplierDTO> createSupplier(@Valid @RequestBody final SupplierDTO supplierDTO) {
        final SupplierDTO createdSupplier = supplierService.createSupplier(supplierDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSupplier);
    }

    /**
     * 更新供应商
     *
     * @param id 供应商ID
     * @param supplierDTO 更新后的供应商数据
     * @return 更新后的供应商信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新供应商")
    public ResponseEntity<SupplierDTO> updateSupplier(
            @PathVariable final Long id,
            @Valid @RequestBody final SupplierDTO supplierDTO) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, supplierDTO));
    }

    /**
     * 删除供应商
     *
     * @param id 供应商ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除供应商")
    public ResponseEntity<Void> deleteSupplier(@PathVariable final Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 激活供应商
     *
     * @param id 供应商ID
     * @return 激活后的供应商信息
     */
    @PatchMapping("/{id}/activate")
    @Operation(summary = "激活供应商")
    public ResponseEntity<SupplierDTO> activateSupplier(@PathVariable final Long id) {
        return ResponseEntity.ok(supplierService.activateSupplier(id));
    }

    /**
     * 停用供应商
     *
     * @param id 供应商ID
     * @return 停用后的供应商信息
     */
    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "停用供应商")
    public ResponseEntity<SupplierDTO> deactivateSupplier(@PathVariable final Long id) {
        return ResponseEntity.ok(supplierService.deactivateSupplier(id));
    }
}
