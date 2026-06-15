package com.invoice.invoiceservice.controller;

import java.util.List;

import com.invoice.invoiceservice.dto.UnitInfoDTO;
import com.invoice.invoiceservice.service.UnitInfoService;

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
@RequestMapping("/api/v1/invoice/units")
@RequiredArgsConstructor
@Tag(name = "Unit Info", description = "计量单位管理API")
@Validated
public class UnitInfoController {

    private final UnitInfoService unitInfoService;

    /**
     * 创建计量单位
     *
     * @param dto 计量单位数据
     * @return 创建的计量单位
     */
    @PostMapping
    @Operation(summary = "创建计量单位")
    public ResponseEntity<UnitInfoDTO> createUnit(@Valid @RequestBody UnitInfoDTO dto) {
        UnitInfoDTO created = unitInfoService.createUnit(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 更新计量单位
     *
     * @param id 计量单位ID
     * @param dto 更新后的计量单位数据
     * @return 更新后的计量单位
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新计量单位")
    public ResponseEntity<UnitInfoDTO> updateUnit(@PathVariable Long id, @Valid @RequestBody UnitInfoDTO dto) {
        UnitInfoDTO updated = unitInfoService.updateUnit(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * 根据ID获取计量单位
     *
     * @param id 计量单位ID
     * @return 计量单位信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取计量单位")
    public ResponseEntity<UnitInfoDTO> getUnitById(@PathVariable Long id) {
        UnitInfoDTO unit = unitInfoService.getUnitById(id);
        return ResponseEntity.ok(unit);
    }

    /**
     * 获取所有计量单位
     *
     * @return 计量单位列表
     */
    @GetMapping
    @Operation(summary = "获取所有计量单位")
    public ResponseEntity<List<UnitInfoDTO>> getAllUnits() {
        List<UnitInfoDTO> units = unitInfoService.getAllUnits();
        return ResponseEntity.ok(units);
    }

    /**
     * 逻辑删除计量单位
     *
     * @param id 计量单位ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "逻辑删除计量单位")
    public ResponseEntity<Void> deleteUnit(@PathVariable Long id) {
        unitInfoService.deleteUnit(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 搜索计量单位
     *
     * @param keyword 搜索关键字
     * @return 符合条件的计量单位列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索计量单位")
    public ResponseEntity<List<UnitInfoDTO>> searchUnits(@RequestParam String keyword) {
        List<UnitInfoDTO> results = unitInfoService.searchUnits(keyword);
        return ResponseEntity.ok(results);
    }

    /**
     * 查找或创建计量单位（自动记忆）
     *
     * @param unitName 计量单位名称
     * @return 计量单位信息
     */
    @PostMapping("/find-or-create")
    @Operation(summary = "查找或创建计量单位（自动记忆）")
    public ResponseEntity<UnitInfoDTO> findOrCreateUnit(@RequestParam String unitName) {
        UnitInfoDTO unit = unitInfoService.findOrCreateUnit(unitName);
        return ResponseEntity.ok(unit);
    }
}
