package com.inventory.mallservice.service;

import com.inventory.mallservice.entity.FlashSale;
import com.inventory.mallservice.entity.FlashSaleProduct;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 秒杀服务接口.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
public interface IFlashSaleService {

    FlashSale createFlashSale(FlashSale flashSale);

    FlashSaleProduct addFlashSaleProduct(Long flashSaleId, FlashSaleProduct product);

    FlashSale getFlashSaleById(Long id);

    List<FlashSale> getActiveFlashSales();

    Page<FlashSale> getFlashSales(Pageable pageable);

    boolean attemptFlashPurchase(Long flashSaleId, Long flashSaleProductId, Long userId, Integer quantity);

    List<FlashSaleProduct> getFlashSaleProducts(Long flashSaleId);
}
