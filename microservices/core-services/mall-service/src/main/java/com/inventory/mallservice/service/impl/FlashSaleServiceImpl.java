package com.inventory.mallservice.service.impl;

import com.inventory.common.core.exception.EntityNotFoundException;
import com.inventory.mallservice.entity.FlashSale;
import com.inventory.mallservice.entity.FlashSaleProduct;
import com.inventory.mallservice.repository.IFlashSaleProductRepository;
import com.inventory.mallservice.repository.IFlashSaleRepository;
import com.inventory.mallservice.service.IFlashSaleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 秒杀服务实现类.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class FlashSaleServiceImpl implements IFlashSaleService {

    private final IFlashSaleRepository flashSaleRepository;
    private final IFlashSaleProductRepository flashSaleProductRepository;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public FlashSale createFlashSale(final FlashSale flashSale) {
        log.info("Creating flash sale: {}", flashSale.getName());
        return flashSaleRepository.save(flashSale);
    }

    @Override
    @Transactional
    public FlashSaleProduct addFlashSaleProduct(final Long flashSaleId,
                                                  final FlashSaleProduct product) {
        log.info("Adding product to flash sale {}", flashSaleId);
        FlashSale flashSale = flashSaleRepository.findById(flashSaleId)
                .orElseThrow(() -> EntityNotFoundException.forEntity("FlashSale", flashSaleId));
        product.setFlashSale(flashSale);
        return flashSaleProductRepository.save(product);
    }

    @Override
    public FlashSale getFlashSaleById(final Long id) {
        return flashSaleRepository.findById(id)
                .orElseThrow(() -> EntityNotFoundException.forEntity("FlashSale", id));
    }

    @Override
    public List<FlashSale> getActiveFlashSales() {
        return flashSaleRepository.findByStatus("ACTIVE");
    }

    @Override
    public Page<FlashSale> getFlashSales(final Pageable pageable) {
        return flashSaleRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public boolean attemptFlashPurchase(final Long flashSaleId, final Long flashSaleProductId,
                                         final Long userId, final Integer quantity) {
        log.info("User {} attempting flash purchase for product {}", userId, flashSaleProductId);

        String dedupKey = "flash:dedup:" + flashSaleProductId + ":" + userId;
        Boolean isFirst = redisTemplate.opsForValue().setIfAbsent(dedupKey, "1");
        if (Boolean.FALSE.equals(isFirst)) {
            log.warn("Duplicate flash purchase attempt by user {}", userId);
            return false;
        }

        String stockKey = "flash:stock:" + flashSaleProductId;
        Long remaining = redisTemplate.opsForValue().decrement(stockKey, quantity);
        if (remaining != null && remaining < 0) {
            redisTemplate.opsForValue().increment(stockKey, quantity);
            redisTemplate.delete(dedupKey);
            log.warn("Flash sale stock exhausted for product {}", flashSaleProductId);
            return false;
        }

        try {
            FlashSaleProduct product = flashSaleProductRepository.findById(flashSaleProductId)
                    .orElseThrow(() -> EntityNotFoundException.forEntity("FlashSaleProduct", flashSaleProductId));
            product.setSoldCount(product.getSoldCount() + quantity);
            flashSaleProductRepository.save(product);
            return true;
        } catch (Exception e) {
            redisTemplate.opsForValue().increment(stockKey, quantity);
            redisTemplate.delete(dedupKey);
            log.error("Flash purchase failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<FlashSaleProduct> getFlashSaleProducts(final Long flashSaleId) {
        return flashSaleProductRepository.findByFlashSaleId(flashSaleId);
    }
}
