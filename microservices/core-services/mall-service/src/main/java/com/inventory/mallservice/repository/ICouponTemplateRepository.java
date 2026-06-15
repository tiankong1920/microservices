package com.inventory.mallservice.repository;

import com.inventory.mallservice.entity.CouponTemplate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 优惠券模板Repository.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Repository
@SuppressWarnings("null")
public interface ICouponTemplateRepository extends JpaRepository<CouponTemplate, Long> {
}
