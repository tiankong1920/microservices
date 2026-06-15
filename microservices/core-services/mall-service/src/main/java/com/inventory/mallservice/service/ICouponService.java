package com.inventory.mallservice.service;

import com.inventory.mallservice.entity.Coupon;
import com.inventory.mallservice.entity.CouponTemplate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券服务接口.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
public interface ICouponService {

    CouponTemplate createTemplate(CouponTemplate template);

    List<Coupon> batchGenerateCoupons(Long templateId, Integer count);

    Coupon issueCoupon(Long templateId, Long userId);

    void batchIssueCoupons(Long templateId, List<Long> userIds);

    Coupon useCoupon(Long couponId, Long orderId);

    Coupon getCouponById(Long id);

    Coupon getCouponByCode(String couponCode);

    List<Coupon> getUserCoupons(Long userId, String status);

    Page<CouponTemplate> getTemplates(Pageable pageable);

    boolean validateCoupon(Long couponId, Long userId, BigDecimal orderAmount, List<Long> categoryIds);

    BigDecimal calculateDiscount(Long couponId, BigDecimal orderAmount);

    void expireCoupons();
}
