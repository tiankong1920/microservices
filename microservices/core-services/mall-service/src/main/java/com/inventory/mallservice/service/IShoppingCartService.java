package com.inventory.mallservice.service;

import com.inventory.mallservice.entity.ShoppingCart;

import java.util.List;

/**
 * 购物车服务接口.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
public interface IShoppingCartService {

    ShoppingCart addToCart(Long userId, Long skuId, Integer quantity);

    ShoppingCart updateCartItemQuantity(Long cartItemId, Integer quantity);

    void removeFromCart(Long cartItemId);

    void clearCart(Long userId);

    List<ShoppingCart> getCartByUserId(Long userId);

    List<ShoppingCart> getCheckedCartItems(Long userId);

    void checkCartItem(Long cartItemId, Boolean checked);

    void checkAllCartItems(Long userId, Boolean checked);

    Integer getCartItemCount(Long userId);
}
