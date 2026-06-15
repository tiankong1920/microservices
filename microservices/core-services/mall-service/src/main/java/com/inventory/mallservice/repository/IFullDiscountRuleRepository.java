package com.inventory.mallservice.repository;

import com.inventory.mallservice.entity.FullDiscountRule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 满减规则Repository.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Repository
@SuppressWarnings("null")
public interface IFullDiscountRuleRepository extends JpaRepository<FullDiscountRule, Long> {

    List<FullDiscountRule> findByFullDiscountIdOrderByMinAmountAsc(Long discountId);
}
