package com.inventory.financeservice.controller;

import com.inventory.financeservice.dto.SettlementAccountDTO;
import com.inventory.financeservice.service.ISettlementAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/settlement-accounts")
@Tag(name = "Settlement Account", description = "结算账户管理接口")
@RequiredArgsConstructor
@Validated
public class SettlementAccountController {
    private final ISettlementAccountService settlementAccountService;

    /**
     * 创建结算账户
     *
     * @param settlementAccountDTO 结算账户数据
     * @return 创建的结算账户信息
     */
    @PostMapping
    @Operation(summary = "创建结算账户")
    public ResponseEntity<SettlementAccountDTO> createSettlementAccount(
            @Valid @RequestBody SettlementAccountDTO settlementAccountDTO) {
        SettlementAccountDTO createdAccount =
                settlementAccountService.createSettlementAccount(settlementAccountDTO);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }

    /**
     * 根据ID获取结算账户
     *
     * @param id 结算账户ID
     * @return 结算账户信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取结算账户")
    public ResponseEntity<SettlementAccountDTO> getSettlementAccountById(
            @PathVariable Long id) {
        SettlementAccountDTO settlementAccountDTO =
                settlementAccountService.getSettlementAccountById(id);
        return ResponseEntity.ok(settlementAccountDTO);
    }

    /**
     * 获取所有结算账户
     *
     * @return 结算账户列表
     */
    @GetMapping
    @Operation(summary = "获取所有结算账户")
    public ResponseEntity<List<SettlementAccountDTO>> getAllSettlementAccounts() {
        List<SettlementAccountDTO> settlementAccountDTOs =
                settlementAccountService.getAllSettlementAccounts();
        return ResponseEntity.ok(settlementAccountDTOs);
    }

    /**
     * 更新结算账户
     *
     * @param id 结算账户ID
     * @param settlementAccountDTO 更新后的结算账户数据
     * @return 更新后的结算账户信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新结算账户")
    public ResponseEntity<SettlementAccountDTO> updateSettlementAccount(
            @PathVariable Long id,
            @Valid @RequestBody SettlementAccountDTO settlementAccountDTO) {
        SettlementAccountDTO updatedAccount =
                settlementAccountService.updateSettlementAccount(id, settlementAccountDTO);
        return ResponseEntity.ok(updatedAccount);
    }

    /**
     * 删除结算账户
     *
     * @param id 结算账户ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除结算账户")
    public ResponseEntity<Void> deleteSettlementAccount(@PathVariable Long id) {
        settlementAccountService.deleteSettlementAccount(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 获取默认结算账户
     *
     * @return 默认结算账户信息
     */
    @GetMapping("/default")
    @Operation(summary = "获取默认结算账户")
    public ResponseEntity<SettlementAccountDTO> getDefaultSettlementAccount() {
        SettlementAccountDTO defaultAccount = settlementAccountService.getDefaultSettlementAccount();
        return ResponseEntity.ok(defaultAccount);
    }

    /**
     * 搜索结算账户
     *
     * @param keyword 搜索关键词
     * @return 结算账户列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索结算账户")
    public ResponseEntity<List<SettlementAccountDTO>> searchAccounts(
            @RequestParam String keyword) {
        List<SettlementAccountDTO> results = settlementAccountService.searchAccounts(keyword);
        return ResponseEntity.ok(results);
    }

    /**
     * 导出结算账户到Excel
     *
     * @return Excel 文件字节数组
     */
    @GetMapping("/export/excel")
    @Operation(summary = "导出结算账户到Excel")
    public ResponseEntity<byte[]> exportAccountsToExcel() {
        byte[] data = settlementAccountService.exportAccountsToExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "settlement_accounts.xlsx");
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    /**
     * 导出结算账户到CSV
     *
     * @return CSV 文件字节数组
     */
    @GetMapping("/export/csv")
    @Operation(summary = "导出结算账户到CSV")
    public ResponseEntity<byte[]> exportAccountsToCsv() {
        byte[] data = settlementAccountService.exportAccountsToCsv();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "settlement_accounts.csv");
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
