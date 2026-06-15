package com.inventory.mallservice.controller;

import com.inventory.mallservice.entity.FlashSale;
import com.inventory.mallservice.entity.FlashSaleProduct;
import com.inventory.mallservice.service.IFlashSaleService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

/**
 * 秒杀Controller.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/mall/flash-sales")
@Tag(name = "Flash Sale", description = "秒杀活动管理接口")
@RequiredArgsConstructor
@Slf4j
@Validated
@SuppressWarnings("null")
public class FlashSaleController {

    private final IFlashSaleService flashSaleService;

    /**
     * 创建秒杀活动
     *
     * @param flashSale 秒杀活动数据
     * @return 创建的秒杀活动信息
     */
    @PostMapping
    public ResponseEntity<FlashSale> createFlashSale(@Valid @RequestBody final FlashSale flashSale) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(flashSaleService.createFlashSale(flashSale));
    }

    /**
     * 添加秒杀商品
     *
     * @param flashSaleId 秒杀活动ID
     * @param product 秒杀商品数据
     * @return 添加的秒杀商品信息
     */
    @PostMapping("/{flashSaleId}/products")
    public ResponseEntity<FlashSaleProduct> addFlashSaleProduct(
            @PathVariable final Long flashSaleId,
            @Valid @RequestBody final FlashSaleProduct product) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(flashSaleService.addFlashSaleProduct(flashSaleId, product));
    }

    /**
     * 获取秒杀活动详情
     *
     * @param id 秒杀活动ID
     * @return 秒杀活动信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<FlashSale> getFlashSale(@PathVariable final Long id) {
        return ResponseEntity.ok(flashSaleService.getFlashSaleById(id));
    }

    /**
     * 获取正在进行的秒杀活动
     *
     * @return 正在进行的秒杀活动列表
     */
    @GetMapping("/active")
    public ResponseEntity<List<FlashSale>> getActiveFlashSales() {
        return ResponseEntity.ok(flashSaleService.getActiveFlashSales());
    }

    /**
     * 分页获取秒杀活动列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 秒杀活动分页列表
     */
    @GetMapping
    public ResponseEntity<Page<FlashSale>> getFlashSales(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());
        return ResponseEntity.ok(flashSaleService.getFlashSales(pageable));
    }

    /**
     * 获取秒杀活动的商品列表
     *
     * @param flashSaleId 秒杀活动ID
     * @return 秒杀商品列表
     */
    @GetMapping("/{flashSaleId}/products")
    public ResponseEntity<List<FlashSaleProduct>> getFlashSaleProducts(
            @PathVariable final Long flashSaleId) {
        return ResponseEntity.ok(flashSaleService.getFlashSaleProducts(flashSaleId));
    }

    /**
     * 尝试秒杀购买
     *
     * @param request 包含flashSaleId、flashSaleProductId、userId和quantity的请求数据
     * @return 购买结果
     */
    @PostMapping("/purchase")
    public ResponseEntity<Map<String, Object>> attemptFlashPurchase(
            @Valid @RequestBody final Map<String, Object> request) {
        Long flashSaleId = Long.valueOf(request.get("flashSaleId").toString());
        Long flashSaleProductId = Long.valueOf(request.get("flashSaleProductId").toString());
        Long userId = Long.valueOf(request.get("userId").toString());
        Integer quantity = Integer.valueOf(request.get("quantity").toString());
        boolean success = flashSaleService.attemptFlashPurchase(
                flashSaleId, flashSaleProductId, userId, quantity);
        return ResponseEntity.ok(Map.of("success", success,
                "message", success ? "秒杀成功" : "秒杀失败，库存不足或重复购买"));
    }
}
