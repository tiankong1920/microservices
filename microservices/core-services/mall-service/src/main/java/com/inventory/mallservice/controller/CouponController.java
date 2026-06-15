package com.inventory.mallservice.controller;

import com.inventory.mallservice.entity.Coupon;
import com.inventory.mallservice.entity.CouponTemplate;
import com.inventory.mallservice.service.ICouponService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
 * 优惠券Controller.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/mall/coupons")
@Tag(name = "Coupon", description = "优惠券管理接口")
@RequiredArgsConstructor
@Slf4j
@Validated
@SuppressWarnings("null")
public class CouponController {

    private final ICouponService couponService;

    /**
     * 创建优惠券模板
     *
     * @param template 优惠券模板数据
     * @return 创建的优惠券模板信息
     */
    @PostMapping("/templates")
    public ResponseEntity<CouponTemplate> createTemplate(
            @Valid @RequestBody final CouponTemplate template) {
        return ResponseEntity.status(HttpStatus.CREATED).body(couponService.createTemplate(template));
    }

    /**
     * 批量生成优惠券
     *
     * @param templateId 优惠券模板ID
     * @param request 包含count的请求数据
     * @return 生成的优惠券列表
     */
    @PostMapping("/templates/{templateId}/generate")
    public ResponseEntity<List<Coupon>> batchGenerate(
            @PathVariable final Long templateId, @Valid @RequestBody final Map<String, Integer> request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(couponService.batchGenerateCoupons(templateId, request.get("count")));
    }

    /**
     * 发放优惠券
     *
     * @param request 包含templateId和userId的请求数据
     * @return 发放的优惠券信息
     */
    @PostMapping("/issue")
    public ResponseEntity<Coupon> issueCoupon(@Valid @RequestBody final Map<String, Long> request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(couponService.issueCoupon(request.get("templateId"), request.get("userId")));
    }

    /**
     * 使用优惠券
     *
     * @param request 包含couponId和orderId的请求数据
     * @return 使用后的优惠券信息
     */
    @PostMapping("/use")
    public ResponseEntity<Coupon> useCoupon(@Valid @RequestBody final Map<String, Long> request) {
        return ResponseEntity.ok(couponService.useCoupon(request.get("couponId"), request.get("orderId")));
    }

    /**
     * 获取优惠券详情
     *
     * @param id 优惠券ID
     * @return 优惠券信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Coupon> getCoupon(@PathVariable final Long id) {
        return ResponseEntity.ok(couponService.getCouponById(id));
    }

    /**
     * 根据编码获取优惠券
     *
     * @param code 优惠券编码
     * @return 优惠券信息
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<Coupon> getCouponByCode(@PathVariable final String code) {
        return ResponseEntity.ok(couponService.getCouponByCode(code));
    }

    /**
     * 获取用户的优惠券列表
     *
     * @param userId 用户ID
     * @param status 优惠券状态（可选）
     * @return 用户的优惠券列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Coupon>> getUserCoupons(
            @PathVariable final Long userId,
            @RequestParam(required = false) final String status) {
        return ResponseEntity.ok(couponService.getUserCoupons(userId, status));
    }

    /**
     * 分页获取优惠券模板列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 优惠券模板分页列表
     */
    @GetMapping("/templates")
    public ResponseEntity<Page<CouponTemplate>> getTemplates(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(couponService.getTemplates(pageable));
    }

    /**
     * 验证优惠券
     *
     * @param request 包含couponId、userId、orderAmount和categoryIds的请求数据
     * @return 验证结果和优惠金额
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateCoupon(
            @Valid @RequestBody final Map<String, Object> request) {
        Long couponId = Long.valueOf(request.get("couponId").toString());
        Long userId = Long.valueOf(request.get("userId").toString());
        BigDecimal orderAmount = new BigDecimal(request.get("orderAmount").toString());
        @SuppressWarnings("unchecked")
        List<Long> categoryIds = (List<Long>) request.get("categoryIds");
        boolean valid = couponService.validateCoupon(couponId, userId, orderAmount, categoryIds);
        BigDecimal discount = valid ? couponService.calculateDiscount(couponId, orderAmount) : BigDecimal.ZERO;
        return ResponseEntity.ok(Map.of("valid", valid, "discount", discount));
    }
}
