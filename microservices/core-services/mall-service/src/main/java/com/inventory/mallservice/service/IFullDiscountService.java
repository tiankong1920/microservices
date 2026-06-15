package com.inventory.mallservice.service;

import com.inventory.mallservice.entity.FullDiscount;
import com.inventory.mallservice.entity.FullDiscountRule;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 满减服务接口.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
public interface IFullDiscountService {

    FullDiscount createFullDiscount(FullDiscount fullDiscount);

    FullDiscountRule addRule(Long discountId, FullDiscountRule rule);

    FullDiscount getFullDiscountById(Long id);

    Page<FullDiscount> getFullDiscounts(Pageable pageable);

    List<FullDiscount> getActiveFullDiscounts();

    List<FullDiscountRule> getRulesByDiscountId(Long discountId);

    BigDecimal calculateDiscount(Long discountId, BigDecimal orderAmount);

    BigDecimal calculateBestDiscount(BigDecimal orderAmount);
}
