package com.inventory.mallservice.repository;

import com.inventory.mallservice.entity.Coupon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 优惠券Repository.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Repository
@SuppressWarnings("null")
public interface ICouponRepository extends JpaRepository<Coupon, Long> {

    Coupon findByCouponCode(String couponCode);

    List<Coupon> findByUserIdAndStatus(Long userId, String status);

    List<Coupon> findByTemplateIdAndStatus(Long templateId, String status);

    Integer countByTemplateIdAndStatus(Long templateId, String status);
}
