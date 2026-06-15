package com.inventory.mallservice.service.impl;

import com.inventory.common.core.exception.EntityNotFoundException;
import com.inventory.mallservice.entity.Coupon;
import com.inventory.mallservice.entity.CouponTemplate;
import com.inventory.mallservice.exception.CouponException;
import com.inventory.mallservice.repository.ICouponRepository;
import com.inventory.mallservice.repository.ICouponTemplateRepository;
import com.inventory.mallservice.service.ICouponService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 优惠券服务实现类.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class CouponServiceImpl implements ICouponService {

    private final ICouponTemplateRepository templateRepository;
    private final ICouponRepository couponRepository;

    @Override
    @Transactional
    public CouponTemplate createTemplate(final CouponTemplate template) {
        log.info("Creating coupon template: {}", template.getName());
        return templateRepository.save(template);
    }

    @Override
    @Transactional
    public List<Coupon> batchGenerateCoupons(final Long templateId, final Integer count) {
        log.info("Batch generating {} coupons for template {}", count, templateId);
        CouponTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> EntityNotFoundException.forEntity("CouponTemplate", templateId));
        List<Coupon> coupons = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Coupon coupon = new Coupon();
            coupon.setTemplate(template);
            coupon.setCouponCode(generateCouponCode());
            coupon.setStatus("UNUSED");
            coupons.add(coupon);
        }
        return couponRepository.saveAll(coupons);
    }

    @Override
    @Transactional
    public Coupon issueCoupon(final Long templateId, final Long userId) {
        log.info("Issuing coupon from template {} to user {}", templateId, userId);
        CouponTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> EntityNotFoundException.forEntity("CouponTemplate", templateId));
        Integer usedCount = couponRepository.countByTemplateIdAndStatus(templateId, "USED");
        if (template.getUsageLimit() != null && usedCount >= template.getUsageLimit()) {
            throw CouponException.usageLimitReached();
        }
        Integer userUsedCount = couponRepository.findByUserIdAndStatus(userId, "USED").size();
        if (template.getPerUserLimit() != null && userUsedCount >= template.getPerUserLimit()) {
            throw CouponException.userUsageLimitReached();
        }
        Coupon coupon = new Coupon();
        coupon.setTemplate(template);
        coupon.setCouponCode(generateCouponCode());
        coupon.setUserId(userId);
        coupon.setStatus("UNUSED");
        return couponRepository.save(coupon);
    }

    @Override
    @Transactional
    public void batchIssueCoupons(final Long templateId, final List<Long> userIds) {
        log.info("Batch issuing coupons from template {} to {} users", templateId, userIds.size());
        for (Long userId : userIds) {
            try {
                issueCoupon(templateId, userId);
            } catch (RuntimeException e) {
                log.warn("Failed to issue coupon to user {}: {}", userId, e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public Coupon useCoupon(final Long couponId, final Long orderId) {
        log.info("Using coupon {} for order {}", couponId, orderId);
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> EntityNotFoundException.forEntity("Coupon", couponId));
        if (!"UNUSED".equals(coupon.getStatus())) {
            throw CouponException.notAvailable();
        }
        coupon.setStatus("USED");
        coupon.setUsedAt(LocalDateTime.now());
        coupon.setOrderId(orderId);
        return couponRepository.save(coupon);
    }

    @Override
    public Coupon getCouponById(final Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> EntityNotFoundException.forEntity("Coupon", id));
    }

    @Override
    public Coupon getCouponByCode(final String couponCode) {
        Coupon coupon = couponRepository.findByCouponCode(couponCode);
        if (coupon == null) {
            throw CouponException.notFound(couponCode);
        }
        return coupon;
    }

    @Override
    public List<Coupon> getUserCoupons(final Long userId, final String status) {
        if (status != null) {
            return couponRepository.findByUserIdAndStatus(userId, status);
        }
        return couponRepository.findByUserIdAndStatus(userId, "UNUSED");
    }

    @Override
    public Page<CouponTemplate> getTemplates(final Pageable pageable) {
        return templateRepository.findAll(pageable);
    }

    @Override
    public boolean validateCoupon(final Long couponId, final Long userId,
                                   final BigDecimal orderAmount, final List<Long> categoryIds) {
        Coupon coupon = getCouponById(couponId);
        if (!"UNUSED".equals(coupon.getStatus())) {
            return false;
        }
        if (!coupon.getUserId().equals(userId)) {
            return false;
        }
        CouponTemplate template = coupon.getTemplate();
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(template.getStartTime()) || now.isAfter(template.getEndTime())) {
            return false;
        }
        if (template.getMinSpend() != null && orderAmount.compareTo(template.getMinSpend()) < 0) {
            return false;
        }
        return true;
    }

    @Override
    public BigDecimal calculateDiscount(final Long couponId, final BigDecimal orderAmount) {
        Coupon coupon = getCouponById(couponId);
        CouponTemplate template = coupon.getTemplate();
        BigDecimal discount;
        if ("FIXED".equals(template.getType())) {
            discount = template.getValue();
        } else if ("PERCENTAGE".equals(template.getType())) {
            discount = orderAmount.multiply(template.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (template.getMaxDiscount() != null
                    && discount.compareTo(template.getMaxDiscount()) > 0) {
                discount = template.getMaxDiscount();
            }
        } else {
            discount = template.getValue();
        }
        return discount.min(orderAmount);
    }

    @Override
    @Transactional
    public void expireCoupons() {
        log.info("Expiring overdue coupons");
    }

    private String generateCouponCode() {
        return "CPN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
