package com.inventory.mallservice.controller;

import com.inventory.mallservice.entity.ShoppingCart;
import com.inventory.mallservice.service.IShoppingCartService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

/**
 * 购物车Controller.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/mall/cart")
@Tag(name = "Shopping Cart", description = "购物车管理接口")
@RequiredArgsConstructor
@Slf4j
@Validated
@SuppressWarnings("null")
public class ShoppingCartController {

    private final IShoppingCartService cartService;

    /**
     * 添加商品到购物车
     *
     * @param request 包含userId、skuId和quantity的请求数据
     * @return 添加的购物车项信息
     */
    @PostMapping("/add")
    public ResponseEntity<ShoppingCart> addToCart(@Valid @RequestBody final Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        Long skuId = Long.valueOf(request.get("skuId").toString());
        Integer quantity = Integer.valueOf(request.get("quantity").toString());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.addToCart(userId, skuId, quantity));
    }

    /**
     * 更新购物车商品数量
     *
     * @param id 购物车项ID
     * @param request 包含quantity的请求数据
     * @return 更新后的购物车项信息
     */
    @PutMapping("/{id}/quantity")
    public ResponseEntity<ShoppingCart> updateQuantity(
            @PathVariable final Long id, @Valid @RequestBody final Map<String, Integer> request) {
        return ResponseEntity.ok(cartService.updateCartItemQuantity(id, request.get("quantity")));
    }

    /**
     * 从购物车移除商品
     *
     * @param id 购物车项ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFromCart(@PathVariable final Long id) {
        cartService.removeFromCart(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 清空用户购物车
     *
     * @param userId 用户ID
     * @return 无内容响应
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable final Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 获取用户购物车
     *
     * @param userId 用户ID
     * @return 购物车列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ShoppingCart>> getCart(@PathVariable final Long userId) {
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    /**
     * 选择/取消选择购物车项
     *
     * @param id 购物车项ID
     * @param request 包含checked的请求数据
     * @return 无内容响应
     */
    @PutMapping("/{id}/check")
    public ResponseEntity<Void> checkCartItem(
            @PathVariable final Long id, @Valid @RequestBody final Map<String, Boolean> request) {
        cartService.checkCartItem(id, request.get("checked"));
        return ResponseEntity.ok().build();
    }

    /**
     * 全选/取消全选用户购物车项
     *
     * @param userId 用户ID
     * @param request 包含checked的请求数据
     * @return 无内容响应
     */
    @PutMapping("/user/{userId}/check-all")
    public ResponseEntity<Void> checkAllCartItems(
            @PathVariable final Long userId, @Valid @RequestBody final Map<String, Boolean> request) {
        cartService.checkAllCartItems(userId, request.get("checked"));
        return ResponseEntity.ok().build();
    }

    /**
     * 获取购物车商品数量
     *
     * @param userId 用户ID
     * @return 购物车商品数量
     */
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Integer> getCartItemCount(@PathVariable final Long userId) {
        return ResponseEntity.ok(cartService.getCartItemCount(userId));
    }
}
