package com.inventory.customerservice.controller;

import com.inventory.customerservice.dto.CustomerDTO;
import com.inventory.customerservice.service.ICustomerService;
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
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "Customer Management API")
@Validated
public class CustomerController {

    private final ICustomerService customerService;

    /**
     * 分页获取所有客户
     *
     * @param page 页码，从0开始
     * @param size 每页数量
     * @param sortBy 排序字段
     * @param sortDir 排序方向
     * @return 分页客户列表
     */
    @GetMapping
    @Operation(summary = "分页获取所有客户")
    public ResponseEntity<Page<CustomerDTO>> getAllCustomers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(customerService.getAllCustomers(pageable));
    }

    /**
     * 获取所有客户列表
     *
     * @return 所有客户列表
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有客户")
    public ResponseEntity<List<CustomerDTO>> getAllCustomersList() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    /**
     * 根据ID获取客户
     *
     * @param id 客户ID
     * @return 客户信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取客户")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable final Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    /**
     * 根据邮箱获取客户
     *
     * @param email 客户邮箱
     * @return 客户信息
     */
    @GetMapping("/email/{email}")
    @Operation(summary = "根据邮箱获取客户")
    public ResponseEntity<CustomerDTO> getCustomerByEmail(@PathVariable final String email) {
        return ResponseEntity.ok(customerService.getCustomerByEmail(email));
    }

    /**
     * 分页获取活跃客户
     *
     * @param page 页码，从0开始
     * @param size 每页数量
     * @return 分页活跃客户列表
     */
    @GetMapping("/active")
    @Operation(summary = "分页获取活跃客户")
    public ResponseEntity<Page<CustomerDTO>> getActiveCustomers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(customerService.getActiveCustomers(pageable));
    }

    /**
     * 搜索客户
     *
     * @param keyword 搜索关键词
     * @param page 页码，从0开始
     * @param size 每页数量
     * @return 搜索结果分页列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索客户")
    public ResponseEntity<Page<CustomerDTO>> searchCustomers(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(customerService.searchCustomers(keyword, pageable));
    }

    /**
     * 根据名称搜索客户
     *
     * @param name 客户名称
     * @param page 页码，从0开始
     * @param size 每页数量
     * @return 搜索结果分页列表
     */
    @GetMapping("/search/name")
    @Operation(summary = "根据名称搜索客户")
    public ResponseEntity<Page<CustomerDTO>> searchCustomersByName(
            @Parameter(description = "Customer name") @RequestParam String name,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(customerService.searchCustomersByName(name, pageable));
    }

    /**
     * 创建客户
     *
     * @param customerDTO 客户数据
     * @return 创建的客户信息
     */
    @PostMapping
    @Operation(summary = "创建客户")
    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody final CustomerDTO customerDTO) {
        final CustomerDTO createdCustomer = customerService.createCustomer(customerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomer);
    }

    /**
     * 更新客户
     *
     * @param id 客户ID
     * @param customerDTO 更新后的客户数据
     * @return 更新后的客户信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新客户")
    public ResponseEntity<CustomerDTO> updateCustomer(
            @PathVariable final Long id,
            @Valid @RequestBody final CustomerDTO customerDTO) {
        return ResponseEntity.ok(customerService.updateCustomer(id, customerDTO));
    }

    /**
     * 删除客户
     *
     * @param id 客户ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除客户")
    public ResponseEntity<Void> deleteCustomer(@PathVariable final Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 激活客户
     *
     * @param id 客户ID
     * @return 激活后的客户信息
     */
    @PatchMapping("/{id}/activate")
    @Operation(summary = "激活客户")
    public ResponseEntity<CustomerDTO> activateCustomer(@PathVariable final Long id) {
        return ResponseEntity.ok(customerService.activateCustomer(id));
    }

    /**
     * 停用客户
     *
     * @param id 客户ID
     * @return 停用后的客户信息
     */
    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "停用客户")
    public ResponseEntity<CustomerDTO> deactivateCustomer(@PathVariable final Long id) {
        return ResponseEntity.ok(customerService.deactivateCustomer(id));
    }
}
