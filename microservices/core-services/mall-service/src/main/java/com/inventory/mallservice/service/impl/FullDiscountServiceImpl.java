package com.inventory.mallservice.service.impl;

import com.inventory.common.core.exception.EntityNotFoundException;
import com.inventory.mallservice.entity.FullDiscount;
import com.inventory.mallservice.entity.FullDiscountRule;
import com.inventory.mallservice.repository.IFullDiscountRepository;
import com.inventory.mallservice.repository.IFullDiscountRuleRepository;
import com.inventory.mallservice.service.IFullDiscountService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * 满减服务实现类.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class FullDiscountServiceImpl implements IFullDiscountService {

    private final IFullDiscountRepository fullDiscountRepository;
    private final IFullDiscountRuleRepository fullDiscountRuleRepository;

    @Override
    @Transactional
    public FullDiscount createFullDiscount(final FullDiscount fullDiscount) {
        log.info("Creating full discount: {}", fullDiscount.getName());
        return fullDiscountRepository.save(fullDiscount);
    }

    @Override
    @Transactional
    public FullDiscountRule addRule(final Long discountId, final FullDiscountRule rule) {
        log.info("Adding rule to full discount {}", discountId);
        FullDiscount discount = getFullDiscountById(discountId);
        rule.setFullDiscount(discount);
        return fullDiscountRuleRepository.save(rule);
    }

    @Override
    public FullDiscount getFullDiscountById(final Long id) {
        return fullDiscountRepository.findById(id)
                .orElseThrow(() -> EntityNotFoundException.forEntity("FullDiscount", id));
    }

    @Override
    public Page<FullDiscount> getFullDiscounts(final Pageable pageable) {
        return fullDiscountRepository.findAll(pageable);
    }

    @Override
    public List<FullDiscount> getActiveFullDiscounts() {
        LocalDateTime now = LocalDateTime.now();
        return fullDiscountRepository.findByStatus("ACTIVE").stream()
                .filter(fd -> fd.getStartTime().isBefore(now) && fd.getEndTime().isAfter(now))
                .toList();
    }

    @Override
    public List<FullDiscountRule> getRulesByDiscountId(final Long discountId) {
        return fullDiscountRuleRepository.findByFullDiscountIdOrderByMinAmountAsc(discountId);
    }

    @Override
    public BigDecimal calculateDiscount(final Long discountId, final BigDecimal orderAmount) {
        List<FullDiscountRule> rules = getRulesByDiscountId(discountId);
        return rules.stream()
                .filter(rule -> orderAmount.compareTo(rule.getMinAmount()) >= 0)
                .max(Comparator.comparing(FullDiscountRule::getMinAmount))
                .map(FullDiscountRule::getDiscountAmount)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal calculateBestDiscount(final BigDecimal orderAmount) {
        List<FullDiscount> activeDiscounts = getActiveFullDiscounts();
        BigDecimal bestDiscount = BigDecimal.ZERO;
        for (FullDiscount discount : activeDiscounts) {
            BigDecimal currentDiscount = calculateDiscount(discount.getId(), orderAmount);
            if (currentDiscount.compareTo(bestDiscount) > 0) {
                bestDiscount = currentDiscount;
            }
        }
        return bestDiscount;
    }
}
