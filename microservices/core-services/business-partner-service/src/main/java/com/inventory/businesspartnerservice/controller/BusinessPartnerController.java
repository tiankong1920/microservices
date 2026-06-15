package com.inventory.businesspartnerservice.controller;

import com.inventory.businesspartnerservice.dto.BusinessPartnerDTO;
import com.inventory.businesspartnerservice.dto.BusinessPartnerStatisticsDTO;
import com.inventory.businesspartnerservice.entity.BusinessPartner.PartnerType;
import com.inventory.businesspartnerservice.service.IBusinessPartnerService;
import io.swagger.v3.oas.annotations.Operation;
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

/**
 * 商业伙伴控制器 - 管理商业伙伴的增删改查
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/business-partners")
@RequiredArgsConstructor
@Tag(name = "Business Partner", description = "Business Partner Management API")
@Validated
public class BusinessPartnerController {

    private final IBusinessPartnerService businessPartnerService;

    /**
     * 创建新的商业伙伴
     *
     * @param dto 商业伙伴数据
     * @return 创建的商业伙伴信息
     */
    @PostMapping
    @Operation(summary = "创建商业伙伴")
    public ResponseEntity<BusinessPartnerDTO> createBusinessPartner(
            @Valid @RequestBody final BusinessPartnerDTO dto) {
        return new ResponseEntity<>(
                businessPartnerService.createBusinessPartner(dto),
                HttpStatus.CREATED);
    }

    /**
     * 根据ID获取商业伙伴
     *
     * @param id 商业伙伴ID
     * @return 商业伙伴信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取商业伙伴")
    public ResponseEntity<BusinessPartnerDTO> getBusinessPartnerById(@PathVariable final Long id) {
        return ResponseEntity.ok(businessPartnerService.getBusinessPartnerById(id));
    }

    /**
     * 根据编码获取商业伙伴
     *
     * @param partnerCode 商业伙伴编码
     * @return 商业伙伴信息
     */
    @GetMapping("/code/{partnerCode}")
    @Operation(summary = "根据编码获取商业伙伴")
    public ResponseEntity<BusinessPartnerDTO> getBusinessPartnerByCode(@PathVariable final String partnerCode) {
        return ResponseEntity.ok(businessPartnerService.getBusinessPartnerByCode(partnerCode));
    }

    /**
     * 获取所有商业伙伴
     *
     * @return 所有商业伙伴列表
     */
    @GetMapping
    @Operation(summary = "获取所有商业伙伴")
    public ResponseEntity<List<BusinessPartnerDTO>> getAllBusinessPartners() {
        return ResponseEntity.ok(businessPartnerService.getAllBusinessPartners());
    }

    /**
     * 分页获取商业伙伴
     *
     * @param page 页码，从0开始
     * @param size 每页数量
     * @param sortBy 排序字段
     * @param sortDir 排序方向（asc/desc）
     * @return 分页的商业伙伴列表
     */
    @GetMapping("/paged")
    @Operation(summary = "分页获取商业伙伴")
    public ResponseEntity<Page<BusinessPartnerDTO>> getBusinessPartnersPaged(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size,
            @RequestParam(defaultValue = "id") final String sortBy,
            @RequestParam(defaultValue = "asc") final String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(businessPartnerService.getAllBusinessPartnersPaged(pageable));
    }

    /**
     * 更新商业伙伴信息
     *
     * @param id 商业伙伴ID
     * @param dto 更新后的商业伙伴数据
     * @return 更新后的商业伙伴信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新商业伙伴")
    public ResponseEntity<BusinessPartnerDTO> updateBusinessPartner(
            @PathVariable final Long id,
            @Valid @RequestBody final BusinessPartnerDTO dto) {
        return ResponseEntity.ok(businessPartnerService.updateBusinessPartner(id, dto));
    }

    /**
     * 删除商业伙伴
     *
     * @param id 商业伙伴ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除商业伙伴")
    public ResponseEntity<Void> deleteBusinessPartner(@PathVariable final Long id) {
        businessPartnerService.deleteBusinessPartner(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 根据类型获取商业伙伴
     *
     * @param partnerType 商业伙伴类型
     * @return 符合类型的商业伙伴列表
     */
    @GetMapping("/type/{partnerType}")
    @Operation(summary = "根据类型获取商业伙伴")
    public ResponseEntity<List<BusinessPartnerDTO>> getBusinessPartnersByType(
            @PathVariable final PartnerType partnerType) {
        return ResponseEntity.ok(businessPartnerService.getBusinessPartnersByType(partnerType));
    }

    /**
     * 根据类型分页获取商业伙伴
     *
     * @param partnerType 商业伙伴类型
     * @param page 页码，从0开始
     * @param size 每页数量
     * @return 分页的商业伙伴列表
     */
    @GetMapping("/type/{partnerType}/paged")
    @Operation(summary = "根据类型分页获取商业伙伴")
    public ResponseEntity<Page<BusinessPartnerDTO>> getBusinessPartnersByTypePaged(
            @PathVariable final PartnerType partnerType,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(businessPartnerService.getBusinessPartnersByTypePaged(partnerType, pageable));
    }

    /**
     * 获取所有活跃商业伙伴
     *
     * @return 活跃商业伙伴列表
     */
    @GetMapping("/active")
    @Operation(summary = "获取活跃商业伙伴")
    public ResponseEntity<List<BusinessPartnerDTO>> getActiveBusinessPartners() {
        return ResponseEntity.ok(businessPartnerService.getActiveBusinessPartners());
    }

    /**
     * 分页获取活跃商业伙伴
     *
     * @param page 页码，从0开始
     * @param size 每页数量
     * @return 分页的活跃商业伙伴列表
     */
    @GetMapping("/active/paged")
    @Operation(summary = "分页获取活跃商业伙伴")
    public ResponseEntity<Page<BusinessPartnerDTO>> getActiveBusinessPartnersPaged(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(businessPartnerService.getActiveBusinessPartnersPaged(pageable));
    }

    /**
     * 搜索商业伙伴
     *
     * @param keyword 搜索关键词
     * @param page 页码，从0开始
     * @param size 每页数量
     * @return 搜索结果分页列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索商业伙伴")
    public ResponseEntity<Page<BusinessPartnerDTO>> searchBusinessPartners(
            @RequestParam final String keyword,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(businessPartnerService.searchBusinessPartners(keyword, pageable));
    }

    /**
     * 根据城市获取商业伙伴
     *
     * @param city 城市名称
     * @param page 页码，从0开始
     * @param size 每页数量
     * @return 符合城市的商业伙伴分页列表
     */
    @GetMapping("/city/{city}")
    @Operation(summary = "根据城市获取商业伙伴")
    public ResponseEntity<Page<BusinessPartnerDTO>> getBusinessPartnersByCity(
            @PathVariable final String city,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(businessPartnerService.getBusinessPartnersByCity(city, pageable));
    }

    /**
     * 根据国家获取商业伙伴
     *
     * @param country 国家名称
     * @param page 页码，从0开始
     * @param size 每页数量
     * @return 符合国家的商业伙伴分页列表
     */
    @GetMapping("/country/{country}")
    @Operation(summary = "根据国家获取商业伙伴")
    public ResponseEntity<Page<BusinessPartnerDTO>> getBusinessPartnersByCountry(
            @PathVariable final String country,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(businessPartnerService.getBusinessPartnersByCountry(country, pageable));
    }

    /**
     * 切换商业伙伴状态
     *
     * @param id 商业伙伴ID
     * @return 状态切换后的商业伙伴信息
     */
    @PatchMapping("/{id}/toggle-status")
    @Operation(summary = "切换商业伙伴状态")
    public ResponseEntity<BusinessPartnerDTO> toggleBusinessPartnerStatus(@PathVariable final Long id) {
        return ResponseEntity.ok(businessPartnerService.toggleBusinessPartnerStatus(id));
    }

    /**
     * 激活商业伙伴
     *
     * @param id 商业伙伴ID
     * @return 激活后的商业伙伴信息
     */
    @PatchMapping("/{id}/activate")
    @Operation(summary = "激活商业伙伴")
    public ResponseEntity<BusinessPartnerDTO> activateBusinessPartner(@PathVariable final Long id) {
        return ResponseEntity.ok(businessPartnerService.activateBusinessPartner(id));
    }

    /**
     * 停用商业伙伴
     *
     * @param id 商业伙伴ID
     * @return 停用后的商业伙伴信息
     */
    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "停用商业伙伴")
    public ResponseEntity<BusinessPartnerDTO> deactivateBusinessPartner(@PathVariable final Long id) {
        return ResponseEntity.ok(businessPartnerService.deactivateBusinessPartner(id));
    }

    /**
     * 批量激活商业伙伴
     *
     * @param ids 商业伙伴ID列表
     * @return 成功激活的数量
     */
    @PatchMapping("/batch/activate")
    @Operation(summary = "批量激活商业伙伴")
    public ResponseEntity<Integer> batchActivateBusinessPartners(@RequestBody final List<Long> ids) {
        return ResponseEntity.ok(businessPartnerService.batchActivateBusinessPartners(ids));
    }

    /**
     * 批量停用商业伙伴
     *
     * @param ids 商业伙伴ID列表
     * @return 成功停用的数量
     */
    @PatchMapping("/batch/deactivate")
    @Operation(summary = "批量停用商业伙伴")
    public ResponseEntity<Integer> batchDeactivateBusinessPartners(@RequestBody final List<Long> ids) {
        return ResponseEntity.ok(businessPartnerService.batchDeactivateBusinessPartners(ids));
    }

    /**
     * 获取商业伙伴统计信息
     *
     * @return 统计信息，包括总数、活跃数等
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取商业伙伴统计")
    public ResponseEntity<BusinessPartnerStatisticsDTO> getStatistics() {
        return ResponseEntity.ok(businessPartnerService.getStatistics());
    }

    /**
     * 获取所有城市列表
     *
     * @return 所有商业伙伴所在城市列表
     */
    @GetMapping("/cities")
    @Operation(summary = "获取所有城市")
    public ResponseEntity<List<String>> getAllCities() {
        return ResponseEntity.ok(businessPartnerService.getAllCities());
    }

    /**
     * 获取所有国家列表
     *
     * @return 所有商业伙伴所在国家列表
     */
    @GetMapping("/countries")
    @Operation(summary = "获取所有国家")
    public ResponseEntity<List<String>> getAllCountries() {
        return ResponseEntity.ok(businessPartnerService.getAllCountries());
    }
}
