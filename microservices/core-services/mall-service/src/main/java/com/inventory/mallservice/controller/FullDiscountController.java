package com.inventory.mallservice.controller;

import com.inventory.mallservice.entity.FullDiscount;
import com.inventory.mallservice.entity.FullDiscountRule;
import com.inventory.mallservice.service.IFullDiscountService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

/**
 * 满减Controller.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/mall/full-discounts")
@Tag(name = "Full Discount", description = "满减活动管理接口")
@RequiredArgsConstructor
@Slf4j
@Validated
@SuppressWarnings("null")
public class FullDiscountController {

    private final IFullDiscountService fullDiscountService;

    /**
     * 创建满减活动
     *
     * @param fullDiscount 满减活动数据
     * @return 创建的满减活动信息
     */
    @PostMapping
    public ResponseEntity<FullDiscount> createFullDiscount(
            @Valid @RequestBody final FullDiscount fullDiscount) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fullDiscountService.createFullDiscount(fullDiscount));
    }

    /**
     * 添加满减规则
     *
     * @param discountId 满减活动ID
     * @param rule 满减规则数据
     * @return 添加的满减规则信息
     */
    @PostMapping("/{discountId}/rules")
    public ResponseEntity<FullDiscountRule> addRule(
            @PathVariable final Long discountId,
            @Valid @RequestBody final FullDiscountRule rule) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fullDiscountService.addRule(discountId, rule));
    }

    /**
     * 获取满减活动详情
     *
     * @param id 满减活动ID
     * @return 满减活动信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<FullDiscount> getFullDiscount(@PathVariable final Long id) {
        return ResponseEntity.ok(fullDiscountService.getFullDiscountById(id));
    }

    /**
     * 分页获取满减活动列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 满减活动分页列表
     */
    @GetMapping
    public ResponseEntity<Page<FullDiscount>> getFullDiscounts(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(fullDiscountService.getFullDiscounts(pageable));
    }

    /**
     * 获取正在进行的满减活动
     *
     * @return 正在进行的满减活动列表
     */
    @GetMapping("/active")
    public ResponseEntity<List<FullDiscount>> getActiveFullDiscounts() {
        return ResponseEntity.ok(fullDiscountService.getActiveFullDiscounts());
    }

    /**
     * 获取满减活动的规则列表
     *
     * @param discountId 满减活动ID
     * @return 满减规则列表
     */
    @GetMapping("/{discountId}/rules")
    public ResponseEntity<List<FullDiscountRule>> getRulesByDiscountId(
            @PathVariable final Long discountId) {
        return ResponseEntity.ok(fullDiscountService.getRulesByDiscountId(discountId));
    }

    /**
     * 计算满减优惠
     *
     * @param request 包含discountId和orderAmount的请求数据
     * @return 优惠计算结果
     */
    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateDiscount(
            @Valid @RequestBody final Map<String, Object> request) {
        Long discountId = request.get("discountId") != null
                ? Long.valueOf(request.get("discountId").toString()) : null;
        BigDecimal orderAmount = new BigDecimal(request.get("orderAmount").toString());
        BigDecimal discount;
        if (discountId != null) {
            discount = fullDiscountService.calculateDiscount(discountId, orderAmount);
        } else {
            discount = fullDiscountService.calculateBestDiscount(orderAmount);
        }
        return ResponseEntity.ok(Map.of(
                "orderAmount", orderAmount,
                "discountAmount", discount,
                "finalAmount", orderAmount.subtract(discount)));
    }
}
