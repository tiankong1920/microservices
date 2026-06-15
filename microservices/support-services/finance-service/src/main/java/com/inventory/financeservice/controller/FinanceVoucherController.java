package com.inventory.financeservice.controller;

import com.inventory.financeservice.dto.FinanceVoucherDTO;
import com.inventory.financeservice.service.IFinanceVoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/vouchers")
@Tag(name = "Finance Voucher", description = "财务凭证管理接口")
@RequiredArgsConstructor
@Validated
public class FinanceVoucherController {

    private final IFinanceVoucherService financeVoucherService;

    /**
     * 获取所有财务凭证
     *
     * @return 财务凭证列表
     */
    @GetMapping
    @Operation(summary = "获取所有财务凭证")
    public ResponseEntity<List<FinanceVoucherDTO>> getAllVouchers() {
        List<FinanceVoucherDTO> vouchers = financeVoucherService.getAllFinanceVouchers();
        return ResponseEntity.ok(vouchers);
    }

    /**
     * 根据ID获取财务凭证
     *
     * @param id 财务凭证ID
     * @return 财务凭证信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取财务凭证")
    public ResponseEntity<FinanceVoucherDTO> getVoucherById(@PathVariable Long id) {
        FinanceVoucherDTO voucher = financeVoucherService.getFinanceVoucherById(id);
        return ResponseEntity.ok(voucher);
    }

    /**
     * 创建财务凭证
     *
     * @param voucherDTO 财务凭证数据
     * @return 创建的财务凭证信息
     */
    @PostMapping
    @Operation(summary = "创建财务凭证")
    public ResponseEntity<FinanceVoucherDTO> createVoucher(@Valid @RequestBody FinanceVoucherDTO voucherDTO) {
        FinanceVoucherDTO createdVoucher = financeVoucherService.createFinanceVoucher(voucherDTO);
        return new ResponseEntity<>(createdVoucher, HttpStatus.CREATED);
    }

    /**
     * 更新财务凭证
     *
     * @param id 财务凭证ID
     * @param voucherDTO 更新后的财务凭证数据
     * @return 更新后的财务凭证信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新财务凭证")
    public ResponseEntity<FinanceVoucherDTO> updateVoucher(
            @PathVariable Long id,
            @Valid @RequestBody FinanceVoucherDTO voucherDTO) {
        FinanceVoucherDTO updatedVoucher =
                financeVoucherService.updateFinanceVoucher(id, voucherDTO);
        return ResponseEntity.ok(updatedVoucher);
    }

    /**
     * 删除财务凭证
     *
     * @param id 财务凭证ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除财务凭证")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long id) {
        financeVoucherService.deleteFinanceVoucher(id);
        return ResponseEntity.noContent().build();
    }
}
