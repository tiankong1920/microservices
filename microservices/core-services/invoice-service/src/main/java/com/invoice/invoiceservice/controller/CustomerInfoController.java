package com.invoice.invoiceservice.controller;

import java.util.List;

import com.invoice.invoiceservice.dto.CustomerInfoDTO;
import com.invoice.invoiceservice.service.CustomerInfoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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

@RestController
@RequestMapping("/api/v1/invoice/customers")
@RequiredArgsConstructor
@Tag(name = "Invoice Customer", description = "开票客户信息管理API")
@Validated
public class CustomerInfoController {

    private final CustomerInfoService customerInfoService;

    /**
     * 创建客户信息
     *
     * @param dto 客户信息数据
     * @return 创建的客户信息
     */
    @PostMapping
    @Operation(summary = "创建客户信息")
    public ResponseEntity<CustomerInfoDTO> createCustomer(@Valid @RequestBody CustomerInfoDTO dto) {
        CustomerInfoDTO created = customerInfoService.createCustomer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 更新客户信息
     *
     * @param id 客户ID
     * @param dto 更新后的客户信息数据
     * @return 更新后的客户信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新客户信息")
    public ResponseEntity<CustomerInfoDTO> updateCustomer(
            @PathVariable Long id, @Valid @RequestBody CustomerInfoDTO dto) {
        CustomerInfoDTO updated = customerInfoService.updateCustomer(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * 根据ID获取客户信息
     *
     * @param id 客户ID
     * @return 客户信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取客户信息")
    public ResponseEntity<CustomerInfoDTO> getCustomerById(@PathVariable Long id) {
        CustomerInfoDTO customer = customerInfoService.getCustomerById(id);
        return ResponseEntity.ok(customer);
    }

    /**
     * 获取所有客户信息
     *
     * @return 客户信息列表
     */
    @GetMapping
    @Operation(summary = "获取所有客户信息")
    public ResponseEntity<List<CustomerInfoDTO>> getAllCustomers() {
        List<CustomerInfoDTO> customers = customerInfoService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    /**
     * 获取启用状态的客户信息
     *
     * @return 启用状态的客户信息列表
     */
    @GetMapping("/active")
    @Operation(summary = "获取启用状态的客户信息")
    public ResponseEntity<List<CustomerInfoDTO>> getActiveCustomers() {
        List<CustomerInfoDTO> customers = customerInfoService.getActiveCustomers();
        return ResponseEntity.ok(customers);
    }

    /**
     * 逻辑删除客户信息
     *
     * @param id 客户ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "逻辑删除客户信息")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerInfoService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 启用客户
     *
     * @param id 客户ID
     * @return 无内容响应
     */
    @PatchMapping("/{id}/enable")
    @Operation(summary = "启用客户")
    public ResponseEntity<Void> enableCustomer(@PathVariable Long id) {
        customerInfoService.enableCustomer(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 禁用客户
     *
     * @param id 客户ID
     * @return 无内容响应
     */
    @PatchMapping("/{id}/disable")
    @Operation(summary = "禁用客户")
    public ResponseEntity<Void> disableCustomer(@PathVariable Long id) {
        customerInfoService.disableCustomer(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 多条件搜索客户信息
     *
     * @param keyword 搜索关键字
     * @return 符合条件的客户信息列表
     */
    @GetMapping("/search")
    @Operation(summary = "多条件搜索客户信息")
    public ResponseEntity<List<CustomerInfoDTO>> searchCustomers(@RequestParam String keyword) {
        List<CustomerInfoDTO> results = customerInfoService.searchCustomers(keyword);
        return ResponseEntity.ok(results);
    }

    /**
     * 自动填充客户信息到开票界面
     *
     * @param id 客户ID
     * @return 客户信息
     */
    @PostMapping("/{id}/auto-fill")
    @Operation(summary = "自动填充客户信息到开票界面")
    public ResponseEntity<CustomerInfoDTO> autoFillCustomerInfo(@PathVariable Long id) {
        CustomerInfoDTO customer = customerInfoService.autoFillCustomerInfo(id);
        return ResponseEntity.ok(customer);
    }
}
